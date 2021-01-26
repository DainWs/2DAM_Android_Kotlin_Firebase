package com.joseDuarte.firebase

import android.app.Activity
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.android.synthetic.main.activity_main.*

class SessionManager {

    lateinit var activity: Activity

    constructor(activity: Activity) {
        this.activity = activity
    }

    fun getSessionState(): Boolean {
        var prefs : SharedPreferences = activity.getSharedPreferences(
                activity.getString(R.string.app_preferences),
                AppCompatActivity.MODE_PRIVATE
        )
        var connected: Boolean = prefs.getString("mail", "").toString().isNotEmpty() &&
                prefs.getString("pass", "").toString().isNotEmpty()
        return connected
    }

    fun saveSession() {
        var prefs : SharedPreferences = activity.getSharedPreferences(
                activity.getString(R.string.app_preferences),
                AppCompatActivity.MODE_PRIVATE
        )
        var editPrefs : SharedPreferences.Editor = prefs.edit()

        editPrefs.putString("mail", activity.mail.text.toString())
        editPrefs.putString("pass", activity.password.text.toString())
        editPrefs.commit()
    }

    fun deleteSession(){
        var prefs : SharedPreferences = activity.getSharedPreferences(
                activity.getString(R.string.app_preferences),
                AppCompatActivity.MODE_PRIVATE
        )
        var editPrefs : SharedPreferences.Editor = prefs.edit()

        editPrefs.putString("mail", "")
        editPrefs.putString("pass", "")
        editPrefs.commit()
    }
}