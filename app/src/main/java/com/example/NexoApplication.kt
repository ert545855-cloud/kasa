package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class NexoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeFirebaseSafely(this)
    }

    companion object {
        fun initializeFirebaseSafely(context: Context): Boolean {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                return true
            }
            try {
                val app = FirebaseApp.initializeApp(context)
                if (app != null && FirebaseApp.getApps(context).isNotEmpty()) {
                    return true
                }
            } catch (e: Throwable) {
                Log.w("NexoApplication", "Default FirebaseApp from resources unavailable: ${e.message}")
            }

            return try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:799580216351:web:2efb9067b503d696a1c92a")
                    .setProjectId("kasa-a8f52")
                    .setApiKey("AIzaSyCfL6r0nunDtdCnUH3Lfy5w9y0A7ZR_y-U")
                    .setDatabaseUrl("https://kasa-a8f52-default-rtdb.europe-west1.firebasedatabase.app")
                    .setStorageBucket("kasa-a8f52.firebasestorage.app")
                    .setGcmSenderId("799580216351")
                    .build()
                FirebaseApp.initializeApp(context, options)
                true
            } catch (ex: Throwable) {
                Log.e("NexoApplication", "FirebaseApp initialization failed", ex)
                false
            }
        }
    }
}
