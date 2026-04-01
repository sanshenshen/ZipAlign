package com.lingxing.zipalign.feature.zipalign.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingxing.zipalign.feature.zipalign.presentation.ZipAlignViewModel

@Composable
fun ZipAlignRoute(
    viewModel: ZipAlignViewModel,
) {
    val context = LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val manageFilesLauncher = rememberLauncherForActivityResult(
        contract = StartActivityForResult(),
        onResult = {
            viewModel.refreshStorageAccess(StorageAccessPermission.hasStorageAccess(context))
        },
    )
    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = RequestMultiplePermissions(),
        onResult = {
            viewModel.refreshStorageAccess(StorageAccessPermission.hasStorageAccess(context))
        },
    )

    LaunchedEffect(Unit) {
        viewModel.refreshStorageAccess(StorageAccessPermission.hasStorageAccess(context))
    }

    ZipAlignScreen(
        state = uiState,
        onRequestStorageAccess = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                manageFilesLauncher.launch(StorageAccessPermission.createManageFilesIntent(context))
            } else {
                legacyPermissionLauncher.launch(StorageAccessPermission.legacyPermissions())
            }
        },
        onOpenStorageRoot = viewModel::loadRootDirectory,
        onNavigateUp = viewModel::navigateUp,
        onOpenDirectory = viewModel::openDirectory,
        onSelectFile = viewModel::selectFile,
        onVerify = viewModel::verify,
        onAlign = viewModel::align,
        onDefaultAlignmentChanged = viewModel::onDefaultAlignmentChanged,
        onAlignSharedLibrariesChanged = viewModel::onAlignSharedLibrariesChanged,
        onSharedLibraryPageAlignmentChanged = viewModel::onSharedLibraryPageAlignmentChanged,
        onDismissError = viewModel::clearError,
    )
}
