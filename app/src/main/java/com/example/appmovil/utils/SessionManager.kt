package com.example.appmovil.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "AppPeruSession"
    private const val KEY_USER_ID = "user_id"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserSession(context: Context, userId: Int) {
        val editor = getPreferences(context).edit()
        editor.putInt(KEY_USER_ID, userId)
        editor.apply()
    }

    fun getUserId(context: Context): Int {
        return getPreferences(context).getInt(KEY_USER_ID, -1)
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getUserId(context) != -1
    }
}