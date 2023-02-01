package jp.ac.jec.cm0119.mamoru.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import jp.ac.jec.cm0119.mamoru.MyApplication

class ListeningToActivityCallbacks : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        MyApplication.isNotActive = false
    }

    override fun onActivityPaused(activity: Activity) {
        MyApplication.isNotActive = true
    }

    override fun onActivityStopped(activity: Activity) {
        MyApplication.isNotActive = true
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

}