package com.joseDuarte.firebase

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.joseDuarte.firebase.firebase.FirebaseDB
import com.joseDuarte.firebase.logins.GithubSingInManager
import com.joseDuarte.firebase.logins.GoogleSingInManager
import kotlinx.android.synthetic.main.activity_main.*


class LoginActivity : AppCompatActivity() {

    private var hasSignIn: Boolean = false

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSingInManager: GoogleSingInManager
    private lateinit var githubSingInManager: GithubSingInManager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        auth = Firebase.auth
        googleSingInManager = GoogleSingInManager(this)
        githubSingInManager = GithubSingInManager(this)
        sessionManager = SessionManager(this)

        val analytics:FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle=Bundle()
        bundle.putString("message", "Entro")
        analytics.logEvent("PantallaInicial", bundle)

        if(sessionManager.getSessionState()) {
            val prefs : SharedPreferences = getSharedPreferences(getString(R.string.app_preferences), MODE_PRIVATE)
            mail.setText(prefs.getString("mail", "").toString())
            password.setText(prefs.getString("pass", "").toString())
        }

        setup()
    }

    private fun setup() {

        registrar.setOnClickListener {
            if (mail.text.isNotEmpty() && password.text.isNotEmpty()) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                        mail.text.toString(),
                        password.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful) {
                        val db : FirebaseDatabase = FirebaseDatabase.getInstance()
                        val userPath : String = FirebaseDB.makeFirebaseURLPath(mail.text.toString())
                        val newUser : DatabaseReference = db.getReference(userPath)
                        newUser.child("id").setValue(mail.text)

                        showOK("Registrado")
                    }
                    else { showError("Registrarse") }
                }
            }
        }

        logearse.setOnClickListener {
            if (mail.text.isNotEmpty() && password.text.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        mail.text.toString(),
                        password.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful) {
                        sessionManager.saveSession()
                        continueApp()
                    }
                    else { showError("Logearse") }
                }
            }
        }

        cerrar_sesion.setOnClickListener{
            signOut()
        }

        googleSingInManager.addListener(GoogleSingButton)
        githubSingInManager.addListener(GithubSingButton, auth)
    }

    private fun signOut() {
        mail.setText("")
        password.setText("")
        auth.signOut()
        FirebaseAuth.getInstance().signOut()
        sessionManager.deleteSession()
    }

    fun continueApp() {
        mail.setText("")
        password.setText("")
        hasSignIn = true
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun getSessionManager() : SessionManager {
        return sessionManager
    }

    override fun onResume() {
        super.onResume()
        if (hasSignIn) {
            signOut()
        }
    }

    /*
        Mensajes
     */
    private fun showOK(operation: String) {
        Toast.makeText(this, "$operation correctamente", Toast.LENGTH_LONG).show()
    }

    private fun showError(operation: String) {
        Toast.makeText(this, "Error al $operation", Toast.LENGTH_LONG).show()
    }

    /*
       Logearse con otra plataforma
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GoogleSingInManager.RC_SIGN_IN) {
            if(resultCode == RESULT_OK) {
                googleSingInManager.firebaseAuthWithGoogle(
                        GoogleSignIn.getSignedInAccountFromIntent(data),
                        auth
                )
            }
        }

    }

}