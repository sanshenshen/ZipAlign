package com.lingxing.zipalign.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.lingxing.zipalign.core.designsystem.theme.ZipAlignTheme
import com.lingxing.zipalign.core.zipalign.ZipAlignServiceImpl
import com.lingxing.zipalign.feature.zipalign.data.FileSystemApkDocumentGateway
import com.lingxing.zipalign.feature.zipalign.domain.ZipAlignCoordinator
import com.lingxing.zipalign.feature.zipalign.presentation.ZipAlignViewModel
import com.lingxing.zipalign.feature.zipalign.presentation.ZipAlignViewModelFactory
import com.lingxing.zipalign.feature.zipalign.ui.ZipAlignRoute

class MainActivity : ComponentActivity() {

    private val viewModel: ZipAlignViewModel by viewModels {
        ZipAlignViewModelFactory(
            coordinator = ZipAlignCoordinator(
                documentGateway = FileSystemApkDocumentGateway(applicationContext),
                zipAlignService = ZipAlignServiceImpl(),
            ),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZipAlignTheme {
                ZipAlignRoute(viewModel = viewModel)
            }
        }
    }
}
