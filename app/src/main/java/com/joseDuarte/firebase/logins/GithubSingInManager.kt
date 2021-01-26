package com.joseDuarte.firebase.logins

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.joseDuarte.firebase.LoginActivity
import com.joseDuarte.firebase.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONTokener
import java.io.OutputStreamWriter
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection


class GithubSingInManager//Para la url, es necesario el tiempo actual en milisegundos

//URL a donde nos vamos a conectar
    (private var activity: LoginActivity) {

    object GithubConstants {
        const val CLIENT_ID = "65f9493e683d7e446abd"
        const val CLIENT_SECRET = "0c7cdc1819584ccf2a1874619395dcc233e09962"
        const val REDIRECT_URI = "https://firstfirebase-d95c6.firebaseapp.com/__/auth/handler"
        const val SCOPE = "read:user"
        const val AUTH_URL = "https://github.com/login/oauth/authorize"
        const val TOKEN_URL = "https://github.com/login/oauth/access_token"
    }
    /*
    La manera que encontre de acceder a Github, es la siguiente: De forma manual, la forma automatica
    parece no funcionar ya, y a que me refiero con forma manual?
    simple, hay que crearse un dialog con el display de la url de inicio de sesion de github, de esta
    manera es seguro para los usuarios y nos permite controlar el REDIRECT que ahora no funciona
    con la manera automatica.

    eso si, para recoger los datos, hay que hacer peticiones GET/POST
    y pedir permisos para la recoleccion de dichos datos.

    Muchas gracias a https://github.com/johncodeos por su ayuda en la solución de este problema
    */

    private var githubAuthURLFull: String //URL DE CONEXIÓN

    lateinit var dialogGithubSignIn: Dialog //EL dialogo sign in manual

    private lateinit var auth: FirebaseAuth

    /* Datos */
    private lateinit var username: String
    private lateinit var usermail: String
    private var privateMailActive: Boolean = false


    init {
        val state = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        githubAuthURLFull =
            GithubConstants.AUTH_URL + "?client_id=" +
            GithubConstants.CLIENT_ID + "&scope=" +
            GithubConstants.SCOPE + "&redirect_uri=" +
            GithubConstants.REDIRECT_URI + "&state=" +
            state
    }

    fun addListener(btn: Button, auth: FirebaseAuth) {
        this.auth = auth

        btn.setOnClickListener {
            setupGithubWebviewDialog(githubAuthURLFull)
        }
    }

    private fun signedIn() {
        var signInWithUser = false
        if(privateMailActive) {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage("¡¡Has Iniciado sesión con Github!!, pero no hemos podido recoger tu correo, es posible que este en el modo \"Privado\".\n\nTus datos se Guardaran como : $username\n\n¿Desea continuar?")
                .setPositiveButton("Vale") { dialog, _ ->
                    signInWithUser = true
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancelar) { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create().show()

            if(signInWithUser) { activity.mail.setText(username); }
            else { return; }
        }
        else { activity.mail.setText(usermail) }

        activity.getSessionManager().saveSession()
        activity.continueApp()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupGithubWebviewDialog(url: String) {
        //Creamos nuestro dialogo
        dialogGithubSignIn = Dialog(activity)
        val webView = WebView(activity)
        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        //Esta clase sera la encargada de generar la vista con la url de github
        webView.webViewClient = GithubWebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)
        dialogGithubSignIn.setContentView(webView)
        dialogGithubSignIn.show()
    }

    fun requestForAccessToken(code: String) {
        //Ahora procedemos a pedir el codigo de acceso
        val grantType = "authorization_code"
        val postParams = "grant_type=" + grantType +
                         "&code=" + code +
                         "&redirect_uri=" + GithubConstants.REDIRECT_URI +
                         "&client_id=" + GithubConstants.CLIENT_ID +
                         "&client_secret=" + GithubConstants.CLIENT_SECRET

        GlobalScope.launch(Dispatchers.Default) {
            val url = URL(GithubConstants.TOKEN_URL) //Transformamos la direccion URL del token y accedemos
            val httpsURLConnection = withContext(Dispatchers.IO) {
                url.openConnection() as HttpsURLConnection
            }

            //Procedemos a comprobar si el usuario ya se registro mediante una petición POST
            //cuando recibamos la respuesta, procedemos a la recoleccion de datos con la petición Get
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.setRequestProperty(
                "Accept",
                "application/json"
            )

            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
            withContext(Dispatchers.IO) {
                outputStreamWriter.write(postParams)
                outputStreamWriter.flush()
            }

            val response = httpsURLConnection.inputStream.bufferedReader().use { it.readText() }

            withContext(Dispatchers.Main) {
                val jsonObject = JSONTokener(response).nextValue() as JSONObject
                val accessToken = jsonObject.getString("access_token")
                fetchGithubUserProfile(accessToken)
            }
        }
    }

    private fun fetchGithubUserProfile(token: String) {
        //ya fuimos autorizados a la recoleccion de datos del usuario, y ahora mediante una peticion Get
        //consultamos los datos
        GlobalScope.launch(Dispatchers.Default) {
            val tokenURLFull = "https://api.github.com/user"

            val url = URL(tokenURLFull)
            val httpsURLConnection = withContext(Dispatchers.IO) {
                url.openConnection() as HttpsURLConnection
            }

            httpsURLConnection.requestMethod = "GET"
            httpsURLConnection.setRequestProperty("Authorization", "Bearer $token")
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = false

            val response = httpsURLConnection.inputStream.bufferedReader().use { it.readText() }

            //Los datos los recibimos como un objeto JSON, asique simplemente recogemos dichos datos de este
            val jsonObject = JSONTokener(response).nextValue() as JSONObject
            println("Json recivido : $jsonObject")
            usermail = jsonObject.getString("email")
            username = jsonObject.getString("login")

            if(usermail == null) {privateMailActive = true}

            //y por fin, nos logeamos en la app
            signedIn()
        }
    }


    inner class GithubWebViewClient : WebViewClient() {
        //Para moviles con Version LOLLIPOP
        //Comprobamos la redireccion!!, si se ha hecho una redireccion, entonces se completo la
        //peticion, y en dicho caso cerramos el dialog
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading( view: WebView?,request: WebResourceRequest?): Boolean {
            //comprobamos si se realizo una redirección
            if (request!!.url.toString().startsWith(GithubConstants.REDIRECT_URI)) {
                handleUrl(request.url.toString())
                //comprobamos que si los datos se han recibido correctamente, si es asi cerramos la vista
                if (request.url.toString().contains("code=")) {
                    dialogGithubSignIn.dismiss()
                }
                return true
            }
            return false
        }

        //Comprobamos la redireccion!!, si se ha hecho una redireccion, entonces se completo la
        //peticion, y en dicho caso cerramos el dialog
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            //comprobamos si se realizo una redirección
            if (url.startsWith(GithubConstants.REDIRECT_URI)) {
                handleUrl(url)
                //comprobamos que si los datos se han recibido correctamente, si es asi cerramos la vista
                if (url.contains("?code=")) {
                    dialogGithubSignIn.dismiss()
                }
                return true
            }
            return false
        }

        // Comprobamos que el token no sea erroneo
        private fun handleUrl(url: String) {
            val uri = Uri.parse(url)
            if (url.contains("code")) {
                val githubCode = uri.getQueryParameter("code") ?: ""
                requestForAccessToken(githubCode)
            }
        }
    }
}
