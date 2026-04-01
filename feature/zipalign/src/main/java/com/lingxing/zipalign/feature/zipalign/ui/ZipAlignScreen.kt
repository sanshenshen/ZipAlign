package com.lingxing.zipalign.feature.zipalign.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lingxing.zipalign.core.zipalign.api.PageAlignment
import com.lingxing.zipalign.feature.zipalign.R
import com.lingxing.zipalign.feature.zipalign.data.ApkBrowserEntry
import com.lingxing.zipalign.feature.zipalign.data.ApkBrowserEntryType
import com.lingxing.zipalign.feature.zipalign.presentation.BrowserTransitionDirection
import com.lingxing.zipalign.feature.zipalign.presentation.ZipAlignUiState
import java.text.DecimalFormat

@Composable
fun ZipAlignScreen(
    state: ZipAlignUiState,
    onRequestStorageAccess: () -> Unit,
    onOpenStorageRoot: () -> Unit,
    onNavigateUp: () -> Unit,
    onOpenDirectory: (ApkBrowserEntry) -> Unit,
    onSelectFile: (ApkBrowserEntry) -> Unit,
    onVerify: () -> Unit,
    onAlign: () -> Unit,
    onDefaultAlignmentChanged: (String) -> Unit,
    onAlignSharedLibrariesChanged: (Boolean) -> Unit,
    onSharedLibraryPageAlignmentChanged: (PageAlignment) -> Unit,
    onDismissError: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) {
        val error = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error)
        onDismissError()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HeaderSection()
            }
            if (!state.hasStorageAccess) {
                item {
                    PermissionSection(onRequestStorageAccess = onRequestStorageAccess)
                }
            } else {
                item {
                    BrowserPaneSection(
                        state = state,
                        onOpenStorageRoot = onOpenStorageRoot,
                        onNavigateUp = onNavigateUp,
                        onOpenDirectory = onOpenDirectory,
                        onSelectFile = onSelectFile,
                    )
                }
                item {
                    SelectedFileSection(state = state)
                }
                item {
                    AlignmentOptionsSection(
                        state = state,
                        onDefaultAlignmentChanged = onDefaultAlignmentChanged,
                        onAlignSharedLibrariesChanged = onAlignSharedLibrariesChanged,
                        onSharedLibraryPageAlignmentChanged = onSharedLibraryPageAlignmentChanged,
                    )
                }
                item {
                    ActionSection(
                        state = state,
                        onVerify = onVerify,
                        onAlign = onAlign,
                    )
                }
                item {
                    ResultSection(state = state)
                }
                item {
                    ConsoleSection(state = state)
                }
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.zipalign_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = stringResource(R.string.zipalign_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PermissionSection(
    onRequestStorageAccess: () -> Unit,
) {
    CardSection {
        Text(
            text = stringResource(R.string.zipalign_permission_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.zipalign_permission_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRequestStorageAccess,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.zipalign_permission_action))
        }
    }
}

@Composable
private fun BrowserPaneSection(
    state: ZipAlignUiState,
    onOpenStorageRoot: () -> Unit,
    onNavigateUp: () -> Unit,
    onOpenDirectory: (ApkBrowserEntry) -> Unit,
    onSelectFile: (ApkBrowserEntry) -> Unit,
) {
    CardSection {
        Text(
            text = stringResource(R.string.zipalign_browser_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.zipalign_browser_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "${stringResource(R.string.zipalign_browse_path)}: ${formatPath(state)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onOpenStorageRoot,
            enabled = !state.isWorking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.Home,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.zipalign_storage_root))
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
            onClick = onNavigateUp,
            enabled = !state.isWorking && state.browser.canNavigateUp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowUp,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = stringResource(R.string.zipalign_go_up))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.zipalign_browser_count, state.browser.entries.size),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(10.dp))
        BrowserContentHost(
            state = state,
            onOpenDirectory = onOpenDirectory,
            onSelectFile = onSelectFile,
        )
    }
}

@Composable
private fun BrowserContentHost(
    state: ZipAlignUiState,
    onOpenDirectory: (ApkBrowserEntry) -> Unit,
    onSelectFile: (ApkBrowserEntry) -> Unit,
) {
    val scene = BrowserScene.from(state)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BROWSER_PANEL_HEIGHT)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                shape = RoundedCornerShape(18.dp),
            )
            .padding(6.dp),
    ) {
        AnimatedContent(
            targetState = scene,
            transitionSpec = { browserContentTransform(state.browser.transitionDirection) },
            label = "browser-content",
        ) { targetScene ->
            when {
                state.browser.isLoading && targetScene.entries.isEmpty() -> {
                    BrowserStatusState(
                        loading = true,
                        text = stringResource(R.string.zipalign_loading_files),
                    )
                }

                targetScene.directoryPath == null -> {
                    BrowserStatusState(
                        loading = false,
                        text = stringResource(R.string.zipalign_browser_no_root),
                    )
                }

                targetScene.entries.isEmpty() -> {
                    BrowserStatusState(
                        loading = false,
                        text = stringResource(R.string.zipalign_browser_empty),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = targetScene.entries,
                            key = { it.absolutePath },
                        ) { entry ->
                            BrowserRow(
                                entry = entry,
                                isSelected = state.selectedDocument?.absolutePath == entry.absolutePath,
                                onOpenDirectory = onOpenDirectory,
                                onSelectFile = onSelectFile,
                            )
                        }
                    }
                }
            }
        }
        if (state.browser.isLoading && scene.entries.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun BrowserStatusState(
    loading: Boolean,
    text: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp,
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BrowserRow(
    entry: ApkBrowserEntry,
    isSelected: Boolean,
    onOpenDirectory: (ApkBrowserEntry) -> Unit,
    onSelectFile: (ApkBrowserEntry) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        ListItem(
            modifier = Modifier.clickable {
                if (entry.type == ApkBrowserEntryType.DIRECTORY) {
                    onOpenDirectory(entry)
                } else {
                    onSelectFile(entry)
                }
            },
            leadingContent = {
                when (entry.type) {
                    ApkBrowserEntryType.DIRECTORY -> {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }

                    ApkBrowserEntryType.APK_FILE -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_android_robot),
                            contentDescription = null,
                            tint = Color.Unspecified,
                        )
                    }

                    ApkBrowserEntryType.OTHER_FILE -> {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            headlineContent = {
                Text(
                    text = entry.displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Text(
                    text = buildEntrySubtitle(entry),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailingContent = {
                when {
                    entry.type == ApkBrowserEntryType.DIRECTORY -> {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                        )
                    }

                    isSelected -> {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun SelectedFileSection(state: ZipAlignUiState) {
    CardSection {
        Text(
            text = stringResource(R.string.zipalign_selected_file_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        val selected = state.selectedDocument
        if (selected == null) {
            Text(
                text = stringResource(R.string.zipalign_selected_file_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = selected.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.zipalign_source_size)}: ${formatFileSize(selected.sizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.zipalign_source_path)}: ${selected.absolutePath}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.zipalign_export_directory)}: ${state.browser.currentDirectory?.absolutePath ?: "-"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AlignmentOptionsSection(
    state: ZipAlignUiState,
    onDefaultAlignmentChanged: (String) -> Unit,
    onAlignSharedLibrariesChanged: (Boolean) -> Unit,
    onSharedLibraryPageAlignmentChanged: (PageAlignment) -> Unit,
) {
    CardSection {
        Text(
            text = stringResource(R.string.zipalign_alignment_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.defaultAlignmentText,
            onValueChange = onDefaultAlignmentChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isWorking,
            singleLine = true,
            label = { Text(text = stringResource(R.string.zipalign_default_alignment)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Straighten,
                    contentDescription = null,
                )
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.zipalign_enable_shared_library_alignment),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.zipalign_page_alignment),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Switch(
            checked = state.alignSharedLibraries,
            onCheckedChange = onAlignSharedLibrariesChanged,
            enabled = !state.isWorking,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PageAlignment.entries.forEach { alignment ->
                FilterChip(
                    selected = state.sharedLibraryPageAlignment == alignment,
                    onClick = { onSharedLibraryPageAlignmentChanged(alignment) },
                    enabled = !state.isWorking && state.alignSharedLibraries,
                    label = { Text("${alignment.kiloBytes} KiB") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Memory,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ActionSection(
    state: ZipAlignUiState,
    onVerify: () -> Unit,
    onAlign: () -> Unit,
) {
    CardSection {
        Text(
            text = stringResource(R.string.zipalign_action_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (state.isWorking) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.zipalign_pending),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        Button(
            onClick = onVerify,
            enabled = !state.isWorking && state.selectedDocument != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.zipalign_verify))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onAlign,
            enabled = !state.isWorking && state.selectedDocument != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.zipalign_align))
        }
    }
}

@Composable
private fun ResultSection(state: ZipAlignUiState) {
    CardSection {
        Text(
            text = stringResource(R.string.zipalign_result_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        val result = state.lastOperation
        if (result == null) {
            Text(
                text = stringResource(R.string.zipalign_result_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@CardSection
        }

        Text(
            text = result.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (result.verificationReport.isSuccessful) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = result.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(
                R.string.zipalign_result_counts,
                result.verificationReport.alignedCount,
                result.verificationReport.misalignedCount,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (result.totalPaddingBytes > 0L) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.zipalign_total_padding,
                    formatFileSize(result.totalPaddingBytes),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (result.exportedFileName != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${result.exportedFileName} | ${stringResource(R.string.zipalign_output_size)}: ${formatFileSize(result.exportedFileSizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (result.exportedFilePath != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.zipalign_output_path)}: ${result.exportedFilePath}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ConsoleSection(state: ZipAlignUiState) {
    CardSection {
        Text(
            text = stringResource(R.string.zipalign_console_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        val lines = state.lastOperation?.consoleLines.orEmpty()
        if (lines.isEmpty()) {
            Text(
                text = stringResource(R.string.zipalign_console_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    lines.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardSection(
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = content,
        )
    }
}

private fun browserContentTransform(
    direction: BrowserTransitionDirection,
): ContentTransform {
    val durationMillis = 260
    return when (direction) {
        BrowserTransitionDirection.FORWARD -> {
            (fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = 40)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = durationMillis),
                    initialOffsetX = { fullWidth -> fullWidth / 5 },
                )).togetherWith(
                fadeOut(animationSpec = tween(durationMillis = 150)) +
                    slideOutHorizontally(
                        animationSpec = tween(durationMillis = durationMillis),
                        targetOffsetX = { fullWidth -> -fullWidth / 6 },
                    ),
            )
        }

        BrowserTransitionDirection.BACKWARD -> {
            (fadeIn(animationSpec = tween(durationMillis = 180, delayMillis = 40)) +
                slideInHorizontally(
                    animationSpec = tween(durationMillis = durationMillis),
                    initialOffsetX = { fullWidth -> -fullWidth / 5 },
                )).togetherWith(
                fadeOut(animationSpec = tween(durationMillis = 150)) +
                    slideOutHorizontally(
                        animationSpec = tween(durationMillis = durationMillis),
                        targetOffsetX = { fullWidth -> fullWidth / 6 },
                    ),
            )
        }

        BrowserTransitionDirection.NONE -> {
            fadeIn(animationSpec = tween(durationMillis = 180))
                .togetherWith(fadeOut(animationSpec = tween(durationMillis = 120)))
        }
    }
}

private fun formatPath(state: ZipAlignUiState): String {
    return state.browser.directoryStack
        .joinToString(" / ") { it.displayName }
        .ifBlank { "-" }
}

private fun buildEntrySubtitle(entry: ApkBrowserEntry): String {
    return when (entry.type) {
        ApkBrowserEntryType.DIRECTORY -> "Folder"
        ApkBrowserEntryType.APK_FILE -> "APK | ${formatFileSize(entry.sizeBytes)}"
        ApkBrowserEntryType.OTHER_FILE -> "File | ${formatFileSize(entry.sizeBytes)}"
    }
}

private fun formatFileSize(sizeBytes: Long?): String {
    if (sizeBytes == null || sizeBytes < 0L) {
        return "Unknown"
    }
    if (sizeBytes < 1024L) {
        return "$sizeBytes B"
    }
    val units = listOf("KB", "MB", "GB")
    var value = sizeBytes.toDouble()
    var unitIndex = -1
    while (value >= 1024.0 && unitIndex < units.lastIndex) {
        value /= 1024.0
        unitIndex++
    }
    return "${DecimalFormat("0.0").format(value)} ${units[unitIndex]}"
}

private data class BrowserScene(
    val directoryPath: String?,
    val entries: List<ApkBrowserEntry>,
) {
    companion object {
        fun from(state: ZipAlignUiState): BrowserScene {
            return BrowserScene(
                directoryPath = state.browser.currentDirectory?.absolutePath ?: state.browser.rootDirectoryPath,
                entries = state.browser.entries,
            )
        }
    }
}

private val BROWSER_PANEL_HEIGHT = 360.dp
