/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui.sections

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.agupta07505.smartisland.R
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.data.SmartIslandSettingsRepository
import com.agupta07505.smartisland.ui.Dimens
import com.agupta07505.smartisland.ui.SliderSettingItem
import com.agupta07505.smartisland.ui.components.CollapsibleGroup
import com.agupta07505.smartisland.ui.components.GroupedRow
import com.agupta07505.smartisland.ui.components.GroupedSection
import com.agupta07505.smartisland.ui.components.SegmentedChoiceRow
import com.agupta07505.smartisland.ui.components.SwitchRow
import com.agupta07505.smartisland.ui.search.searchAnchor
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun PositionsSection(
    settings: SmartIslandSettings,
    repository: SmartIslandSettingsRepository,
    onNavigateToBackup: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    var localWidth by remember(settings.width) { mutableFloatStateOf(settings.width) }
    var localContentPercent by remember(settings.contentPillWidthPercent) { mutableFloatStateOf(settings.contentPillWidthPercent) }
    var localHeight by remember(settings.height) { mutableFloatStateOf(settings.height) }
    var localXOffset by remember(settings.xOffset) { mutableFloatStateOf(settings.xOffset) }
    var localYOffset by remember(settings.yOffset) { mutableFloatStateOf(settings.yOffset) }
    var localCutoutX by remember(settings.cameraCutoutX) { mutableFloatStateOf(settings.cameraCutoutX) }
    var localCutoutY by remember(settings.cameraCutoutY) { mutableFloatStateOf(settings.cameraCutoutY) }
    var localCutoutWidth by remember(settings.cameraCutoutWidth) { mutableFloatStateOf(settings.cameraCutoutWidth) }
    var localCutoutHeight by remember(settings.cameraCutoutHeight) { mutableFloatStateOf(settings.cameraCutoutHeight) }
    var localCornerRadius by remember(settings.cornerRadius) { mutableFloatStateOf(settings.cornerRadius) }
    var localShadowElevation by remember(settings.shadowElevation) { mutableFloatStateOf(settings.shadowElevation) }

    val screenWidthDp = configuration.screenWidthDp.toFloat()
    val calculatedLeftX = (-(screenWidthDp / 2f - 40f)).coerceIn(
        SmartIslandSettings.MIN_X_OFFSET,
        -30f
    )
    val calculatedRightX = ((screenWidthDp / 2f - 40f)).coerceIn(
        30f,
        SmartIslandSettings.MAX_X_OFFSET
    )
    val selectedLabel = stringResource(R.string.motion_selected)
    val modeKey = if (settings.enableNotchMode) "notch" else "pill"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        // Form factor: Compact Pill or iPhone Notch Mode.
        GroupedSection(modifier = Modifier.searchAnchor("layout.notch_mode")) {
            SegmentedChoiceRow(
                options = listOf(false to stringResource(R.string.preset_compact_pill), true to stringResource(R.string.notch_mode_card_title)),
                selected = settings.enableNotchMode,
                onSelect = { notch ->
                    if (notch == settings.enableNotchMode) return@SegmentedChoiceRow
                    scope.launch {
                        repository.setEnableNotchMode(notch)
                        if (notch) {
                            repository.setPosition(width = 175f, height = 35f, xOffset = 0f, yOffset = 0f)
                        } else {
                            repository.setPosition(width = 112f, height = 34f, xOffset = 0f, yOffset = 10f)
                        }
                        repository.setCornerRadius(20f)
                    }
                    Toast.makeText(
                        context,
                        context.getString(if (notch) R.string.toast_notch_mode_enabled else R.string.toast_notch_mode_disabled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        CollapsibleGroup(
            title = stringResource(LayoutGroup.QuickPresets.titleRes),
            stateKey = "pos_presets_$modeKey",
            value = "${screenWidthDp.toInt()} dp",
            searchKey = LayoutGroup.QuickPresets.searchKey
        ) {
            val isCenterSelected = abs(settings.xOffset) < 5f && abs(settings.width - 112f) < 8f
            PresetRow(
                title = stringResource(R.string.preset_center_hole),
                isSelected = isCenterSelected,
                selectedLabel = selectedLabel,
                onClick = {
                    scope.launch {
                        repository.setPosition(width = 112f, height = 34f, xOffset = 0f, yOffset = 10f)
                        repository.setCornerRadius(20f)
                    }
                    Toast.makeText(context, context.getString(R.string.toast_applied_center_preset), Toast.LENGTH_SHORT).show()
                }
            )
            val isWideSelected = abs(settings.xOffset) < 5f && abs(settings.width - 150f) < 8f
            PresetRow(
                title = stringResource(R.string.preset_wide_island),
                isSelected = isWideSelected,
                selectedLabel = selectedLabel,
                onClick = {
                    scope.launch {
                        repository.setPosition(width = 150f, height = 38f, xOffset = 0f, yOffset = 12f)
                        repository.setCornerRadius(22f)
                    }
                    Toast.makeText(context, context.getString(R.string.toast_applied_wide_preset), Toast.LENGTH_SHORT).show()
                }
            )
            PresetRow(
                title = stringResource(R.string.preset_left_corner),
                isSelected = settings.xOffset < -30f,
                selectedLabel = selectedLabel,
                onClick = {
                    scope.launch {
                        repository.setPosition(width = 105f, height = 34f, xOffset = calculatedLeftX, yOffset = 10f)
                        repository.setCornerRadius(20f)
                    }
                    Toast.makeText(context, context.getString(R.string.toast_applied_left_preset, calculatedLeftX.toInt()), Toast.LENGTH_SHORT).show()
                }
            )
            PresetRow(
                title = stringResource(R.string.preset_right_corner),
                isSelected = settings.xOffset > 30f,
                selectedLabel = selectedLabel,
                onClick = {
                    scope.launch {
                        repository.setPosition(width = 105f, height = 34f, xOffset = calculatedRightX, yOffset = 10f)
                        repository.setCornerRadius(20f)
                    }
                    Toast.makeText(context, context.getString(R.string.toast_applied_right_preset, calculatedRightX.toInt()), Toast.LENGTH_SHORT).show()
                }
            )
            if (!settings.enableNotchMode) {
                SegmentedChoiceRow(
                    title = stringResource(R.string.circle_position_card_title),
                    options = listOf(
                        SmartIslandSettings.CIRCLE_POSITION_LEFT to stringResource(R.string.circle_position_left),
                        SmartIslandSettings.CIRCLE_POSITION_RIGHT to stringResource(R.string.circle_position_right)
                    ),
                    selected = settings.circlePosition,
                    onSelect = { scope.launch { repository.setCirclePosition(it) } },
                    modifier = Modifier.searchAnchor("layout.circle_position")
                )
            }
        }

        CollapsibleGroup(
            title = stringResource(LayoutGroup.ExemptApps.titleRes),
            stateKey = "pos_exempt_apps",
            searchKey = LayoutGroup.ExemptApps.searchKey
        ) {
            ExemptAppsRows(settings = settings, repository = repository, landscape = false)
        }

        CollapsibleGroup(
            title = stringResource(LayoutGroup.SizeAndPosition.titleRes),
            stateKey = "pos_tuning_$modeKey",
            searchKey = LayoutGroup.SizeAndPosition.searchKey
        ) {
            SliderSettingItem(
                label = stringResource(R.string.slider_island_width),
                value = localWidth,
                range = SmartIslandSettings.MIN_WIDTH..SmartIslandSettings.MAX_WIDTH,
                onValueChange = { localWidth = it },
                onValueChangeFinished = { scope.launch { repository.setWidth(localWidth) } }
            )
            SliderSettingItem(
                label = stringResource(R.string.slider_island_height),
                value = localHeight,
                range = SmartIslandSettings.MIN_HEIGHT..SmartIslandSettings.MAX_HEIGHT,
                onValueChange = { localHeight = it },
                onValueChangeFinished = { scope.launch { repository.setHeight(localHeight) } }
            )
            // Collapsed cutout with content: how wide the pill stretches to when a card is on it.
            SwitchRow(
                title = stringResource(R.string.content_pill_same_title),
                checked = settings.contentPillSameAsIdle,
                onCheckedChange = { scope.launch { repository.setContentPillSameAsIdle(false, it) } }
            )
            if (!settings.contentPillSameAsIdle) {
                SliderSettingItem(
                    label = stringResource(R.string.slider_content_pill_width),
                    value = localContentPercent,
                    range = SmartIslandSettings.MIN_CONTENT_PILL_PERCENT..SmartIslandSettings.MAX_CONTENT_PILL_PERCENT,
                    suffix = "%",
                    step = 5f,
                    onValueChange = { localContentPercent = it },
                    onValueChangeFinished = { scope.launch { repository.setContentPillWidthPercent(false, localContentPercent) } }
                )
            }
            SliderSettingItem(
                label = stringResource(R.string.slider_horizontal_offset),
                value = localXOffset,
                range = SmartIslandSettings.MIN_X_OFFSET..SmartIslandSettings.MAX_X_OFFSET,
                onValueChange = { localXOffset = it },
                onValueChangeFinished = { scope.launch { repository.setXOffset(localXOffset) } }
            )
            if (settings.enableNotchMode) {
                // Locked at 0 dp in Notch mode; the value stays, the explanation does not.
                GroupedRow(
                    title = stringResource(R.string.slider_vertical_offset),
                    modifier = Modifier.semantics(mergeDescendants = true) { },
                    value = "0 dp",
                    showChevron = false
                )
            } else {
                SliderSettingItem(
                    label = stringResource(R.string.slider_vertical_offset),
                    value = localYOffset,
                    range = SmartIslandSettings.MIN_Y_OFFSET..SmartIslandSettings.MAX_Y_OFFSET,
                    onValueChange = { localYOffset = it },
                    onValueChangeFinished = { scope.launch { repository.setYOffset(localYOffset) } }
                )
            }
            SliderSettingItem(
                label = stringResource(R.string.slider_corner_radius),
                value = localCornerRadius,
                range = SmartIslandSettings.MIN_CORNER_RADIUS..SmartIslandSettings.MAX_CORNER_RADIUS,
                onValueChange = { localCornerRadius = it },
                onValueChangeFinished = { scope.launch { repository.setCornerRadius(localCornerRadius) } }
            )
        }

        // Manual camera cutout area (portrait), for phones where the hardware detection fails.
        if (!settings.enableNotchMode) {
            CollapsibleGroup(
                title = stringResource(LayoutGroup.CameraCutout.titleRes),
                stateKey = "pos_camera_cutout",
                searchKey = LayoutGroup.CameraCutout.searchKey
            ) {
                SwitchRow(
                    title = stringResource(R.string.camera_cutout_manual_title),
                    checked = settings.cameraCutoutManual,
                    onCheckedChange = { checked -> scope.launch { repository.setCameraCutoutManual(checked) } }
                )
                if (settings.cameraCutoutManual) {
                    val saveArea = { scope.launch { repository.setCameraCutoutArea(localCutoutX, localCutoutY, localCutoutWidth, localCutoutHeight); Unit } }
                    SliderSettingItem(
                        label = stringResource(R.string.camera_cutout_x),
                        value = localCutoutX,
                        range = SmartIslandSettings.MIN_X_OFFSET..SmartIslandSettings.MAX_X_OFFSET,
                        onValueChange = { localCutoutX = it },
                        onValueChangeFinished = { saveArea() }
                    )
                    SliderSettingItem(
                        label = stringResource(R.string.camera_cutout_y),
                        value = localCutoutY,
                        range = SmartIslandSettings.MIN_Y_OFFSET..SmartIslandSettings.MAX_Y_OFFSET,
                        onValueChange = { localCutoutY = it },
                        onValueChangeFinished = { saveArea() }
                    )
                    SliderSettingItem(
                        label = stringResource(R.string.camera_cutout_width),
                        value = localCutoutWidth,
                        range = SmartIslandSettings.MIN_CUTOUT_WIDTH..SmartIslandSettings.MAX_CUTOUT_WIDTH,
                        onValueChange = { localCutoutWidth = it },
                        onValueChangeFinished = { saveArea() }
                    )
                    SliderSettingItem(
                        label = stringResource(R.string.camera_cutout_height),
                        value = localCutoutHeight,
                        range = SmartIslandSettings.MIN_CUTOUT_HEIGHT..SmartIslandSettings.MAX_CUTOUT_HEIGHT,
                        onValueChange = { localCutoutHeight = it },
                        onValueChangeFinished = { saveArea() }
                    )
                }
            }
        }

        CollapsibleGroup(
            title = stringResource(LayoutGroup.Shadow.titleRes),
            stateKey = "pos_look_$modeKey",
            searchKey = LayoutGroup.Shadow.searchKey
        ) {
            SwitchRow(
                title = stringResource(R.string.toggle_drop_shadow_title),
                checked = settings.enableShadow,
                onCheckedChange = { checked -> scope.launch { repository.setEnableShadow(checked) } }
            )
            if (settings.enableShadow) {
                SliderSettingItem(
                    label = stringResource(R.string.slider_shadow_elevation),
                    value = localShadowElevation,
                    range = SmartIslandSettings.MIN_SHADOW_ELEVATION..SmartIslandSettings.MAX_SHADOW_ELEVATION,
                    suffix = " dp",
                    step = 1f,
                    onValueChange = { localShadowElevation = it },
                    onValueChangeFinished = { scope.launch { repository.setShadowElevation(localShadowElevation) } }
                )
                SegmentedChoiceRow(
                    options = SHADOW_STEPS.map { it to "${it.toInt()}dp" },
                    selected = SHADOW_STEPS.firstOrNull { abs(localShadowElevation - it) < 0.5f },
                    onSelect = { target ->
                        localShadowElevation = target
                        scope.launch { repository.setShadowElevation(target) }
                    }
                )
            }
        }

        // Motion physics, kinetic glow and backdrop blur (Rule_Book 2.2, 2.3).
        MotionSettings(settings = settings, repository = repository)
    }
}

/** The shadow depths offered as one-tap steps under the elevation slider (subtle, medium, standard, deep). */
private val SHADOW_STEPS = listOf(6f, 10f, 14f, 22f)

/** One quick preset: a titled row that shows an accent check mark when it matches the current layout. */
@Composable
private fun PresetRow(
    title: String,
    isSelected: Boolean,
    selectedLabel: String,
    onClick: () -> Unit
) {
    GroupedRow(
        title = title,
        showChevron = false,
        onClick = onClick,
        trailing = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = selectedLabel,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else null
    )
}
