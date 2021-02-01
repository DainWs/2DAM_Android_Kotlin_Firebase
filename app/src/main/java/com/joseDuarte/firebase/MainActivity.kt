
package com.joseDuarte.firebase

import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.method.TransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.joseDuarte.firebase.firebase.FirebaseDB
import com.joseDuarte.firebase.firebase.FirebaseDB.Companion.addPasswordObjectToFirebase
import com.joseDuarte.firebase.objects.PasswordObject
import com.joseDuarte.firebase.view.ListFragment
import kotlinx.android.synthetic.main.add_dialog.*
import kotlinx.android.synthetic.main.add_dialog.view.*
import kotlinx.android.synthetic.main.main_layout.*


class MainActivity : AppCompatActivity() {

    companion object {
        val USER_DATA_LIST : MutableCollection<PasswordObject> = mutableListOf()
        lateinit var firebaseDB: FirebaseDB
    }

    lateinit var USER_ID: String
    lateinit var list: ListFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        supportActionBar?.show()

        list = ListFragment.newInstance(1);

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, list)
                .commitNow()
        }

        var prefs : SharedPreferences = getSharedPreferences(getString(R.string.app_preferences), MODE_PRIVATE)
        USER_ID = prefs.getString("mail", "").toString()

        firebaseDB = FirebaseDB(this)

        setup()
    }

    private fun setup() {
        logout_button.setOnClickListener {
            USER_DATA_LIST.clear()
            list.update()
            finish()
        }

        add_button.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = this.layoutInflater
            val view = inflater.inflate(R.layout.add_dialog, null)

            view.show_toggle_btn.setOnClickListener {
                var passTextField = view.password_password_textfield
                if(passTextField.transformationMethod is PasswordTransformationMethod) {
                    passTextField.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                } else {
                    passTextField.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                }

            }

            builder.setView(view)
                .setPositiveButton(R.string.guardar) { dialog, _ ->
                    val idText: String = view.password_name_textfield.text.toString()
                    val passText: String = view.password_password_textfield.text.toString()
                    if(idText.isNotEmpty()) {
                        if(passText.isNotEmpty()) {
                            addPasswordObjectToFirebase( PasswordObject(idText, passText) )
                            dialog.dismiss()
                        }
                        else {
                            Toast.makeText(
                                this,
                                R.string.password_rellenar_campo_password,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    else {
                        Toast.makeText(
                            this,
                            R.string.password_rellenar_campo_nombre,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton(R.string.cancelar) { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create().show()
        }
    }

    override fun onPause() {
        super.onPause()
        firebaseDB.stopListeners()
    }

    override fun onResume() {
        super.onResume()
        firebaseDB.restartListeners()
    }

    fun update() {
        list.update()
    }

}