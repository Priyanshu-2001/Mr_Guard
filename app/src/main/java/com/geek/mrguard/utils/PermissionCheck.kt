package com.geek.mrguard.utils

import android.Manifest
import android.app.Activity
import android.widget.Toast
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionRequest
import android.content.DialogInterface
import androidx.core.app.ActivityCompat.startActivityForResult
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle
import android.provider.Settings;

class PermissionCheck {

    fun requestPermissions(activity: Activity) {

        Dexter.withActivity(activity)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
//                        Toast.makeText(
//                            activity,
//                            "All the permissions are granted..",
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                    if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog(activity)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    p1?.continuePermissionRequest()
                }
            }).withErrorListener { // we are displaying a toast message for error message.
                Toast.makeText(
                    activity,
                    "Error occurred! ${it.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread().check()
    }

    private fun showSettingsDialog(activity: Activity) {
        // we are displaying an alert dialog for permissions
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)

        // below line is the title
        // for our alert dialog.
        builder.setTitle("Need Permissions")

        // below line is our message for our dialog
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS",
            DialogInterface.OnClickListener { dialog, which -> // this method is called on click on positive
                // button and on clicking shit button we
                // are redirecting our user from our app to the
                // settings page of our app.
                dialog.cancel()
                // below is the intent from which we
                // are redirecting our user.
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", activity.packageName, null)
                intent.data = uri
                startActivityForResult(activity,intent,101, Bundle())
            })
        builder.setNegativeButton("Cancel"
        ) { dialog, which -> // this method is called when
            // user click on negative button.
            dialog.cancel()
        }
        builder.show()
    }
}