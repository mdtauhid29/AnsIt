package ltd.tgwgroup.ansit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

@Composable
internal fun AnsItScreen(model: MainViewModel) {
    val state by model.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(model) {
        model.events.collect { notice ->
            if (notice.saved) { focus.clearFocus(); keyboard?.hide() }
            snackbar.showSnackbar(notice.text)
        }
    }
    if (state.showAdjustment) {
        OverlayEditor(state.overlay, state.overlaySaving, snackbar, model::saveOverlay, model::cancelAdjustment)
    } else {
        AnsItContent(state, snackbar, model::edit, model::save, model::toggle, model::check,
            model::showGuide, model::dismissGuide, model::restartSystemUi, model::showAdjustment,
            model::editSearch, model::saveSearch, model::cancelValidation, model::editTrigger, model::saveTrigger,
            model::editTriggerSwap)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnsItContent(
    state: MainState,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    onEdit: (ApiForm) -> Unit = {},
    onSave: () -> Unit = {},
    onToggle: () -> Unit = {},
    onCheck: () -> Unit = {},
    onShowGuide: () -> Unit = {},
    onDismissGuide: () -> Unit = {},
    onRestart: () -> Unit = {},
    onAdjustment: () -> Unit = {},
    onSearchEdit: (SearchForm) -> Unit = {},
    onSearchSave: () -> Unit = {},
    onCancelValidation: () -> Unit = {},
    onTriggerEdit: (String) -> Unit = {},
    onTriggerSave: () -> Unit = {},
    onTriggerSwap: (Boolean) -> Unit = {},
) {
    var confirmRestart by remember { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    if (showAbout) {
        AboutScreen(onBack = { showAbout = false })
        return
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { insets ->
        Box(Modifier.fillMaxSize().testTag("main_screen").padding(insets).imePadding(), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 600.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BrandMark(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF111A35)))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("AnsIt", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Questions on screen, answered anytime", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showAbout = true }, modifier = Modifier.testTag("open_about")) {
                        Icon(Icons.Default.Info, "About AnsIt")
                    }
                }

                ServiceCard(state, onToggle, onCheck, onAdjustment)

                if (!state.hookCurrent || state.root != RootAccess.GRANTED) {
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(if (state.root != RootAccess.GRANTED) "Complete initial setup" else "SystemUI module is not ready",
                                    style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(4.dp))
                                Text(if (state.snapshot?.hook == RootServiceStarter.Hook.LOADED) "The module version changed. Restart SystemUI."
                                    else "Grant Root and enable the SystemUI scope in LSPosed.",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = onShowGuide) { Text("Go to setup") }
                        }
                    }
                }

                TriggerSettingsCard(state, onTriggerEdit, onTriggerSave, onTriggerSwap)
                ConfigurationCard(state, onEdit, onSave, onCancelValidation)
                SearchConfigurationCard(state, onSearchEdit, onSearchSave, onCancelValidation)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Long-press the status bar for continuous answers", style = MaterialTheme.typography.titleMedium)
                    Text("Hold near the status-bar battery until the saved threshold (" + state.triggerHoldMillis +
                        " ms) is reached, feel the vibration, and release to start continuous answering. Single-tap the battery: next question (" +
                        (if (state.triggerSavedSwap) "keep context" else "clear context") + "); double-tap: next question (" +
                        (if (state.triggerSavedSwap) "clear context" else "keep context") + "); long-press again to exit. If the battery layout cannot be detected, long-press the far right of the status bar.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("When context is full, AnsIt shows “Context limit reached.” Single-tap to clear context and continue, or long-press to exit. The service keeps running after the app closes; it must be restarted after reboot.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = onShowGuide) { Text("Setup guide") }
                    TextButton(onClick = { showAbout = true }) { Text("About AnsIt · " + BuildConfig.VERSION_NAME) }
                }
            }
        }
    }

    if (state.showGuide) {
        ModalBottomSheet(onDismissRequest = onDismissGuide, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            SetupGuide(state, onCheck, { confirmRestart = true }, onDismissGuide)
        }
    }
    if (confirmRestart) {
        AlertDialog(
            onDismissRequest = { confirmRestart = false },
            title = { Text("Restart SystemUI?") },
            text = { Text("The status and navigation bars will briefly disappear while SystemUI restarts. Enable AnsIt in LSPosed first and select only “SystemUI (com.android.systemui).”") },
            confirmButton = { TextButton(onClick = { confirmRestart = false; onRestart() }) { Text("Restart") } },
            dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun TriggerSettingsCard(state: MainState, onEdit: (String) -> Unit, onSave: () -> Unit,
                                onSwap: (Boolean) -> Unit) {
    val millis = state.triggerText.toIntOrNull()
    val valid = millis != null && millis in TriggerConfig.MIN_MS..TriggerConfig.MAX_MS
    val enabled = !state.triggerLoading && !state.triggerSaving
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Trigger Settings", style = MaterialTheme.typography.titleLarge)
            Text("Adjust how long to hold the battery before starting continuous answering. It responds only while the service is running and ready; it does not handle holds while stopped.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(state.triggerText, onEdit, enabled = enabled, singleLine = true,
                label = { Text("Trigger threshold (milliseconds)") }, supportingText = { Text("200–3000 ms; default 650 ms") },
                isError = !valid, modifier = Modifier.fillMaxWidth().testTag("trigger_millis"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done))
            Slider(value = (millis ?: TriggerConfig.DEFAULT_MS).coerceIn(TriggerConfig.MIN_MS, TriggerConfig.MAX_MS).toFloat(),
                onValueChange = { onEdit(((it / 50).roundToInt() * 50).toString()) }, enabled = enabled,
                valueRange = TriggerConfig.MIN_MS.toFloat()..TriggerConfig.MAX_MS.toFloat(), steps = 55,
                modifier = Modifier.testTag("trigger_slider"))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Swap single-tap and double-tap", style = MaterialTheme.typography.bodyLarge)
                        Text(if (state.triggerSwap) "Single-tap keeps context; double-tap clears it" else "Single-tap clears context; double-tap keeps it",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = state.triggerSwap, onCheckedChange = onSwap, enabled = enabled,
                    modifier = Modifier.testTag("swap_taps"))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (state.triggerDirty) "Unsaved changes" else "Long-press to enter; long-press again to exit",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f))
                TextButton(onClick = { onEdit(TriggerConfig.DEFAULT_MS.toString()) }, enabled = enabled,
                    modifier = Modifier.testTag("reset_trigger")) { Text("Reset") }
            }
            FilledTonalButton(onClick = onSave, enabled = enabled && valid,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("save_trigger")) {
                if (state.triggerSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (state.triggerSaving) "Saving…" else "Save Trigger Settings")
            }
            state.triggerNote?.let { Text(it, style = MaterialTheme.typography.bodySmall,
                color = if (state.triggerError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServiceCard(state: MainState, onToggle: () -> Unit, onCheck: () -> Unit, onAdjustment: () -> Unit) {
    val running = state.service == RootServiceStarter.State.RUNNING
    val scheme = MaterialTheme.colorScheme
    val container = scheme.surfaceContainer
    val content = scheme.onSurface
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = container, contentColor = content)) {
        Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Screen Analysis Service", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(when (state.service) {
                        RootServiceStarter.State.RUNNING -> "Running"
                        RootServiceStarter.State.STOPPED -> "Stopped"
                        RootServiceStarter.State.UNKNOWN -> if (state.root == RootAccess.UNAVAILABLE) "Status temporarily unavailable" else "Ready to start"
                    }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                }
                IconButton(onClick = onCheck, enabled = !state.busy, modifier = Modifier.testTag("service_refresh")) {
                    if (state.operation == Operation.CHECK || state.operation == Operation.UPDATE)
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = content)
                    else Icon(Icons.Default.Refresh, "Recheck service and module")
                }
            }
            Text(when (state.operation) {
                Operation.UPDATE -> "Updating the running Root service to the current version…"
                Operation.RESTART -> "Waiting for SystemUI to reload the module…"
                Operation.START -> "Starting the service from the current APK…"
                Operation.STOP -> "Canceling the task and stopping the service…"
                Operation.ADJUST -> "Hiding the current answer and entering adjustment mode…"
                else -> if (running) "Long-press the status-bar battery area for continuous answering; single- or double-tap the battery to continue."
                    else "Save the model configuration, then start the service."
            }, style = MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusPill(if (state.root == RootAccess.GRANTED) "Root granted" else "Root pending", content)
                StatusPill(if (state.hookCurrent) "Module active" else "Module pending", content)
            }
            Button(onClick = onToggle, enabled = !state.busy && (running || (!state.saving && !state.searchSaving)),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("service_control")) {
                if (running) Box(Modifier.size(16.dp).clip(RoundedCornerShape(3.dp)).background(LocalContentColor.current))
                else Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(when (state.operation) {
                    Operation.START -> "Starting…"
                    Operation.STOP -> "Stopping…"
                    Operation.UPDATE -> "Updating…"
                    else -> when (state.service) {
                        RootServiceStarter.State.RUNNING -> "Stop"
                        RootServiceStarter.State.STOPPED -> "Start"
                        RootServiceStarter.State.UNKNOWN -> "Grant and check"
                    }
                })
            }
            state.error?.let { Text(it, color = scheme.error, style = MaterialTheme.typography.bodySmall) }
            FilledTonalButton(onClick = onAdjustment, enabled = !state.overlayLoading && !state.busy && !state.saving && !state.searchSaving,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("adjustment_mode")) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Adjustment Mode")
            }
            Text(if (state.checkedAt == null) "First use requires Root and LSPosed setup"
                else "Last checked " + android.text.format.DateFormat.format("HH:mm:ss", state.checkedAt) +
                    if (running && state.serviceCurrent) " · Service is current" else " · Tap refresh at upper right",
                style = MaterialTheme.typography.labelSmall, color = content.copy(alpha = .7f))
        }
    }
}

@Composable
private fun StatusPill(text: String, color: Color) {
    Surface(shape = CircleShape, color = color.copy(alpha = .08f), contentColor = color) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun ConfigurationCard(state: MainState, onEdit: (ApiForm) -> Unit, onSave: () -> Unit, onCancel: () -> Unit) {
    val form = state.form
    var showKey by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val enabled = !state.saving && !state.searchSaving
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Model Configuration", style = MaterialTheme.typography.titleLarge)
                Text("Use an OpenAI-compatible endpoint that supports image input. During continuous answering, double-tap can retain the previous context.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedTextField(form.address, { onEdit(form.copy(address = it)) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("API Endpoint") }, placeholder = { Text("https://api.example.com/v1") }, singleLine = true,
                enabled = enabled, supportingText = { Text("Supports a base URL or a complete /chat/completions URL") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next))
            OutlinedTextField(form.key, { onEdit(form.copy(key = it)) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("API Key") }, singleLine = true, enabled = enabled,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { TextButton(onClick = { showKey = !showKey }) { Text(if (showKey) "Hide" else "Show") } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next))
            OutlinedTextField(form.model, { onEdit(form.copy(model = it)) }, modifier = Modifier.fillMaxWidth(),
                label = { Text("Model Name") }, placeholder = { Text("Enter a model ID with vision support") }, singleLine = true, enabled = enabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); keyboard?.hide() }))
            if (state.dirty) Text("Unsaved changes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text("The check sends a local test image. If search is enabled, it also verifies tool calls.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilledTonalButton(onClick = onSave, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("model_save_check")) {
                if (state.saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (state.saving) "Checking…" else "Check and Save")
            }
            state.modelCheck?.let { Text(it, style = MaterialTheme.typography.bodySmall,
                color = if (state.modelCheckPassed == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
            if (state.saving) TextButton(onClick = onCancel, modifier = Modifier.testTag("cancel_model_check")) { Text("Cancel Check") }
        }
    }
}

@Composable
private fun SearchConfigurationCard(state: MainState, onEdit: (SearchForm) -> Unit, onSave: () -> Unit, onCancel: () -> Unit) {
    val form = state.search
    val enabled = !state.saving && !state.searchSaving
    var showKey by remember { mutableStateOf(false) }
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("DeepSeek Web Search", style = MaterialTheme.typography.titleLarge)
            Text("Let the main model search public web pages when needed. When enabled, the main model must support both image input and tool calls.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Allow AI to search when needed", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = form.enabled, onCheckedChange = { onEdit(form.copy(enabled = it)) }, enabled = enabled,
                    modifier = Modifier.testTag("search_enabled"))
            }
            OutlinedTextField(form.key, { onEdit(form.copy(key = it)) }, modifier = Modifier.fillMaxWidth().testTag("search_key"),
                label = { Text("DeepSeek API Key") }, singleLine = true, enabled = enabled,
                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { TextButton(onClick = { showKey = !showKey }) { Text(if (showKey) "Hide" else "Show") } },
                supportingText = { Text("Uses a DeepSeek platform key. Disabling search preserves the key.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done))
            if (state.searchDirty) Text("Unsaved changes", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text("When enabled, AnsIt performs a test search and saves only after the check succeeds. The provider may charge for the check.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilledTonalButton(onClick = onSave, enabled = enabled,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("search_save_check")) {
                if (state.searchSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (state.searchSaving) { if (form.enabled) "Checking…" else "Saving…" }
                    else if (form.enabled) "Check and Save" else "Save Disabled State")
            }
            state.searchCheck?.let { Text(it, style = MaterialTheme.typography.bodySmall,
                color = if (state.searchCheckPassed == false) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
            if (state.searchSaving) TextButton(onClick = onCancel, modifier = Modifier.testTag("cancel_search_check")) { Text("Cancel Check") }
        }
    }
}

@Composable
private fun SetupGuide(state: MainState, onCheck: () -> Unit, onRestart: () -> Unit, onDismiss: () -> Unit) {
    Column(Modifier.fillMaxWidth().testTag("setup_guide").verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Get AnsIt Ready", style = MaterialTheme.typography.headlineSmall)
                Text("Check setup after the first install or an upgrade.", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "Close setup guide") }
        }
        SetupStep("1", "Grant Root Permission", if (state.root == RootAccess.GRANTED) "Root permission confirmed."
            else "Tap below and allow AnsIt in the Root manager. If you previously denied it, change the setting to allow.", state.root == RootAccess.GRANTED) {
            FilledTonalButton(onClick = onCheck, enabled = !state.busy) {
                Text(if (state.operation == Operation.CHECK) "Waiting for authorization and check…" else if (state.root == RootAccess.GRANTED) "Check again" else "Grant and check")
            }
        }
        HorizontalDivider()
        SetupStep("2", "Activate the LSPosed Module", "Open LSPosed → Modules → AnsIt and enable the module. Select only “SystemUI (com.android.systemui),” then return here.", state.hookCurrent) {
            Text(when {
                state.hookCurrent -> "Confirmed: SystemUI is loading this module version"
                state.snapshot?.hook == RootServiceStarter.Hook.LOADED -> "An older module was detected; restart SystemUI"
                state.snapshot?.hook == RootServiceStarter.Hook.ABSENT -> "This module version was not detected; verify the scope and restart SystemUI"
                else -> "Module status pending; enabling the switch alone is not sufficient"
            }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        HorizontalDivider()
        SetupStep("3", "Restart SystemUI to Apply the Module", "Restart SystemUI after first activation or after a module upgrade. The status and navigation bars will briefly disappear, then recover.", state.hookCurrent) {
            FilledTonalButton(onClick = onRestart, enabled = state.root == RootAccess.GRANTED && !state.busy) {
                Text(if (state.operation == Operation.RESTART) "Restarting and checking…" else "Restart SystemUI")
            }
        }
        Text("When complete, enter and save the model configuration, then tap “Start.”",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(if (state.hookCurrent) "Done; configure model" else "OK") }
    }
}

@Composable
private fun SetupStep(number: String, title: String, detail: String, done: Boolean, action: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(shape = CircleShape, color = if (done) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                if (done) Icon(Icons.Default.Check, contentDescription = "Completed", modifier = Modifier.size(18.dp))
                else Text(number, style = MaterialTheme.typography.labelLarge)
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            action()
        }
    }
}

@Preview(name = "Stopped · Light", widthDp = 393, heightDp = 900, showBackground = true)
@Composable
private fun StoppedPreview() = AnsItTheme(dark = false) {
    AnsItContent(MainState(root = RootAccess.GRANTED, hookCurrent = true, overlayLoading = false,
        snapshot = RootServiceStarter.Snapshot(RootServiceStarter.State.STOPPED, "", "", RootServiceStarter.Hook.LOADED, "", "")))
}

@Preview(name = "Running · Dark", widthDp = 393, heightDp = 900, showBackground = true)
@Composable
private fun RunningPreview() = AnsItTheme(dark = true) {
    AnsItContent(MainState(root = RootAccess.GRANTED, hookCurrent = true, serviceCurrent = true, overlayLoading = false,
        snapshot = RootServiceStarter.Snapshot(RootServiceStarter.State.RUNNING, "", "", RootServiceStarter.Hook.LOADED, "", "")))
}
