package com.joseDuarte.firebase.logins

import android.app.AlertDialog
import android.widget.Button
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.joseDuarte.firebase.LoginActivity
import com.joseDuarte.firebase.R
import kotlinx.android.synthetic.main.activity_main.*

class GoogleSingInManager(var activity: LoginActivity) {

    companion object {
        const val RC_SIGN_IN = 9001
    }

    private var googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    fun addListener(btn : Button) {
        btn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            activity.startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    fun firebaseAuthWithGoogle(task: Task<GoogleSignInAccount>, auth: FirebaseAuth) {
        val account = task.getResult(ApiException::class.java)!!

        val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser
                        if (user != null) {
                            var signInWithUser = true
                            if (user.email == null) {
                                val builder = AlertDialog.Builder(activity)
                                builder.setMessage("¡¡Has Iniciado sesión con Google!!, pero no hemos podido recoger tu correo, es posible que este en el modo \"Privado\".\n\nTus datos se Guardaran como : ${user.displayName}\n\n¿Desea continuar?")
                                    .setPositiveButton("Vale") { dialog, _ ->
                                        dialog.dismiss()
                                    }
                                    .setNegativeButton("Cancelar") { dialog, _ ->
                                        signInWithUser = false
                                        dialog.dismiss()
                                    }
                                builder.create().show()
                            }

                            if (signInWithUser) {
                                activity.mail.setText(
                                    if(user.email == null)
                                        user.displayName
                                    else
                                        user.email
                                )
                                activity.getSessionManager().saveSession()
                                activity.continueApp()
                            }
                        }
                    }
                }
    }
}