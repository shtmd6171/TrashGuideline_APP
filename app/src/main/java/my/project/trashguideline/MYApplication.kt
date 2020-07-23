package my.project.trashguideline

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import my.project.trashguideline.utils.L


class MYApplication : Application() {
    companion object {
        lateinit var auth: FirebaseAuth
        const val RC_SIGN_IN = 9001
    }

    override fun onCreate() {
        super.onCreate()
        L.initialize("young", false)
    }
}