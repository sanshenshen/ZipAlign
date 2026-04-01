package com.lingxing.zipalign.feature.zipalign.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

internal object StorageAccessPermission {

    fun hasStorageAccess(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun createManageFilesIntent(context: Context): Intent {
        val packageUri = Uri.parse("package:${context.packageName}")
        val appSpecificIntent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = packageUri
        }
        return if (appSpecificIntent.resolveActivity(context.packageManager) != null) {
            appSpecificIntent
        } else {
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        }
    }

    fun legacyPermissions(): Array<String> {
        return arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE).let { permissions ->
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissions + Manifest.permission.WRITE_EXTERNAL_STORAGE
            } else {
                permissions
            }
        }
    }
}
