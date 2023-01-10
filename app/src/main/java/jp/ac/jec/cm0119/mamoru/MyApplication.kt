package jp.ac.jec.cm0119.mamoru

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO: オフラインキャッシュについて 
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}