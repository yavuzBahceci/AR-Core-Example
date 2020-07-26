package com.yavuzbahceci.ar_core_example

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import kotlinx.android.synthetic.main.activity_main.*


@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class MainActivity : AppCompatActivity() {


    private var mUserRequestedInstall = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        maybeEnableArButton()
    }

    override fun onResume() {
        super.onResume()

        // ARCore requires camera permission to operate.
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
            return;
        }

        var mSession: Session? = null
        try {
            if (mSession == null) {
                when (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    InstallStatus.INSTALLED -> {
                        Toast.makeText(this, "Arcore is installed with session", Toast.LENGTH_LONG)
                            .show()
                        // Success, create the AR session.
                        mSession = Session(this)
                    }
                    InstallStatus.INSTALL_REQUESTED -> {
                        // Ensures next invocation of requestInstall() will either return
                        // INSTALLED or throw an exception.
                        mUserRequestedInstall = false
                        return
                    }
                }
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            // Display an appropriate message to the user and return gracefully.
            Toast.makeText(this, "TODO: handle exception $e", Toast.LENGTH_LONG)
                .show()
            return
        }


    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            )
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }


    private fun maybeEnableArButton() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // Re-query at 5Hz while compatibility is checked in the background.
            Handler().postDelayed(Runnable { maybeEnableArButton() }, 200)
        }
        if (availability.isSupported) {
            mArButton.visibility = View.VISIBLE
            mArButton.isEnabled = true
            // indicator on the button.
        } else { // Unsupported or unknown.
            mArButton.visibility = View.INVISIBLE
            mArButton.isEnabled = false
        }
    }

}