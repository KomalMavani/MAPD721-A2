package com.example.lab3

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserStore(private val context: Context) {
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("userToken")
        private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val STUDENT_ID_KEY = stringPreferencesKey("student_id")
    }

    val getAccessToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_TOKEN_KEY] ?: ""
    }

    val getUserName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "Welcome"
    }

    val getStudentID: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[STUDENT_ID_KEY] ?: ""
    }

    suspend fun saveData(token: String, userName: String, studentId:String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = token
            preferences[USER_NAME_KEY] = userName
            preferences[STUDENT_ID_KEY] = studentId
        }
    }
}