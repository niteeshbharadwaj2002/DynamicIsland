/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.agupta07505.smartisland.R
import com.agupta07505.smartisland.ui.Dimens
import com.agupta07505.smartisland.ui.components.CollapsibleGroup
import com.agupta07505.smartisland.ui.components.GroupedRow
import com.agupta07505.smartisland.ui.components.GroupedSection
import com.agupta07505.smartisland.ui.components.SegmentedChoiceRow
import com.agupta07505.smartisland.ui.components.SwitchRow
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.data.SmartIslandSettingsRepository
import com.agupta07505.smartisland.ui.SliderSettingItem
import com.agupta07505.smartisland.ui.search.searchAnchor
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Landscape profile editor: shape, size, position and opacity are independent of portrait. Writes only the
 * `landscape*` settings; portrait fields are never touched here.
 */
@Composable
fun LandscapeSection(
    settings: SmartIslandSettings,
    repository: SmartIslandSettingsRepository
) {
    val scope = rememberCoroutineScope()
    // Height of the screen in landscape is its short side, whichever way the phone is held now.
    val configuration = LocalConfiguration.current
    val landscapeHeightDp = minOf(configuration.screenWidthDp, configuration.screenHeightDp).toFloat()
    // Half the landscape width (the long side): the offsets can reach either edge of the screen.
    val landscapeHalfWidthDp = maxOf(configuration.screenWidthDp, configuration.screenHeightDp) / 2f

    var circleSize by remember(settings.landscapeCircleSize) { mutableFloatStateOf(settings.landscapeCircleSize) }
    var circleOpacity by remember(settings.landscapeCircleOpacity) { mutableFloatStateOf(settings.landscapeCircleOpacity) }
    var circleInset by remember(settings.landscapeCircleEdgeInset) { mutableFloatStateOf(settings.landscapeCircleEdgeInset) }
    var circleY by remember(settings.landscapeCircleYOffset) { mutableFloatStateOf(settings.landscapeCircleYOffset) }

    var circleXAdjust by remember(settings.landscapeCircleXAdjust) { mutableFloatStateOf(settings.landscapeCircleXAdjust) }
    var circleYAdjust by remember(settings.landscapeCircleYAdjust) { mutableFloatStateOf(settings.landscapeCircleYAdjust) }

    var pillWidth by remember(settings.landscapeWidth) { mutableFloatStateOf(settings.landscapeWidth) }
    var contentPercent by remember(settings.contentPillWidthPercentLandscape) { mutableFloatStateOf(settings.contentPillWidthPercentLandscape) }
    var pillHeight by remember(settings.landscapeHeight) { mutableFloatStateOf(settings.landscapeHeight) }
    var pillX by remember(settings.landscapeXOffset) { mutableFloatStateOf(settings.landscapeXOffset) }
    var pillY by remember(settings.landscapeYOffset) { mutableFloatStateOf(settings.landscapeYOffset) }
    var pillOpacity by remember(settings.landscapeOpacity) { mutableFloatStateOf(settings.landscapeOpacity) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        // The only "show in landscape" switch in the app.
        GroupedSection(modifier = Modifier.searchAnchor("layout.show_landscape")) {
            SwitchRow(
                title = stringResource(R.string.toggle_show_in_landscape_title),
                checked = settings.showInLandscape,
                onCheckedChange = { scope.launch { repository.setShowInLandscape(it) } }
            )
        }

        // Distance from the screen's side edge: one number shared by the idle circle and the bar.
        GroupedSection(modifier = Modifier.searchAnchor("layout.edge_inset")) {
            SliderSettingItem(
                label = stringResource(R.string.landscape_edge_inset_title),
                value = circleInset,
                range = SmartIslandSettings.MIN_CIRCLE_EDGE_INSET..SmartIslandSettings.MAX_CIRCLE_EDGE_INSET,
                onValueChange = { circleInset = it },
                onValueChangeFinished = { scope.launch { repository.setLandscapeCircleEdgeInset(circleInset) } }
            )
        }

        CollapsibleGroup(title = "Idle cutout", stateKey = "land_idle", summary = stringResource(R.string.layout_summary_land_idle)) {
            SegmentedChoiceRow(
                title = "Position",
                options = listOf(
                    SmartIslandSettings.LANDSCAPE_POSITION_AUTO to "Camera",
                    SmartIslandSettings.LANDSCAPE_POSITION_LEFT to "Left",
                    SmartIslandSettings.LANDSCAPE_POSITION_MIDDLE to "Middle",
                    SmartIslandSettings.LANDSCAPE_POSITION_RIGHT to "Right"
                ),
                selected = settings.landscapeCirclePosition,
                onSelect = { scope.launch { repository.setLandscapeCirclePosition(it) } }
            )
            SliderSettingItem(
                label = "Size",
                value = circleSize,
                range = SmartIslandSettings.MIN_CIRCLE_SIZE..SmartIslandSettings.MAX_CIRCLE_SIZE,
                onValueChange = { circleSize = it },
                onValueChangeFinished = { scope.launch { repository.setLandscapeCircleSize(circleSize) } }
            )
            SliderSettingItem(
                label = "Opacity",
                value = circleOpacity * 100f,
                range = (SmartIslandSettings.MIN_CIRCLE_OPACITY * 100f)..(SmartIslandSettings.MAX_OPACITY * 100f),
                suffix = "%",
                step = 5f,
                onValueChange = {
                    circleOpacity = (it / 100f).coerceIn(SmartIslandSettings.MIN_CIRCLE_OPACITY, SmartIslandSettings.MAX_OPACITY)
                },
                onValueChangeFinished = { scope.launch { repository.setLandscapeCircleOpacity(circleOpacity) } }
            )
            SliderSettingItem(
                label = "Horizontal offset",
                value = circleXAdjust.coerceIn(-landscapeHalfWidthDp, landscapeHalfWidthDp),
                range = -landscapeHalfWidthDp..landscapeHalfWidthDp,
                onValueChange = { circleXAdjust = it },
                onValueChangeFinished = { scope.launch { repository.setLandscapeCircleXAdjust(circleXAdjust) } }
            )
            if (settings.landscapeCirclePosition == SmartIslandSettings.LANDSCAPE_POSITION_AUTO) {
                // On the camera the height comes from the cutout itself; this only nudges it.
                SliderSettingItem(
                    label = "Vertical fine-tune (from camera)",
                    value = circleYAdjust,
                    range = -VERTICAL_TUNE_RANGE_DP..VERTICAL_TUNE_RANGE_DP,
                    onValueChange = { circleYAdjust = it },
                    onValueChangeFinished = { scope.launch { repository.setLandscapeCircleYAdjust(circleYAdjust) } }
                )
            } else {
                SliderSettingItem(
                    label = "Vertical offset",
                    value = circleY,
                    range = SmartIslandSettings.MIN_Y_OFFSET..SmartIslandSettings.MAX_LANDSCAPE_Y_OFFSET,
                    onValueChange = { circleY = it },
                    onValueChangeFinished = { scope.launch { repository.setLandscapeCircleYOffset(circleY) } }
                )
            }
            if (settings.landscapeCirclePosition == SmartIslandSettings.LANDSCAPE_POSITION_LEFT ||
                settings.landscapeCirclePosition == SmartIslandSettings.LANDSCAPE_POSITION_RIGHT
            ) {
                GroupedRow(
                    title = "Centre on the side edge",
                    showChevron = false,
                    onClick = {
                        circleY = (landscapeHeightDp / 2f - circleSize / 2f)
                            .coerceIn(SmartIslandSettings.MIN_Y_OFFSET, SmartIslandSettings.MAX_LANDSCAPE_Y_OFFSET)
                        scope.launch { repository.setLandscapeCircleYOffset(circleY) }
                    }
                )
            }
        }

        CollapsibleGroup(title = "Cutout with content", stateKey = "land_content", summary = stringResource(R.string.layout_summary_land_content)) {
            SegmentedChoiceRow(
                title = "Position",
                options = listOf(
                    SmartIslandSettings.LANDSCAPE_POSITION_LEFT to "Left",
                    SmartIslandSettings.LANDSCAPE_POSITION_MIDDLE to "Top middle",
                    SmartIslandSettings.LANDSCAPE_POSITION_RIGHT to "Right"
                ),
                // An older "camera" choice behaves as top middle.
                selected = settings.landscapePillPosition.takeIf {
                    it == SmartIslandSettings.LANDSCAPE_POSITION_LEFT || it == SmartIslandSettings.LANDSCAPE_POSITION_RIGHT
                } ?: SmartIslandSettings.LANDSCAPE_POSITION_MIDDLE,
                onSelect = { scope.launch { repository.setLandscapePillPosition(it) } }
            )
            SliderSettingItem(
                label = "Width",
                value = pillWidth,
                range = SmartIslandSettings.MIN_WIDTH..SmartIslandSettings.MAX_WIDTH,
                onValueChange = { pillWidth = it },
                onValueChangeFinished = { scope.launch { repository.setLandscapePillGeometry(pillWidth, settings.landscapeHeight, settings.landscapeXOffset, settings.landscapeYOffset) } }
            )
            SliderSettingItem(
                label = "Height",
                value = pillHeight,
                range = SmartIslandSettings.MIN_HEIGHT..SmartIslandSettings.MAX_HEIGHT,
                onValueChange = { pillHeight = it },
                onValueChangeFinished = { scope.launch { repository.setLandscapePillGeometry(settings.landscapeWidth, pillHeight, settings.landscapeXOffset, settings.landscapeYOffset) } }
            )
            SwitchRow(
                title = "Same size as idle cutout",
                checked = settings.contentPillSameAsIdleLandscape,
                onCheckedChange = { scope.launch { repository.setContentPillSameAsIdle(true, it) } }
            )
            if (!settings.contentPillSameAsIdleLandscape) {
                SliderSettingItem(
                    label = "Cutout width with content",
                    value = contentPercent,
                    range = SmartIslandSettings.MIN_CONTENT_PILL_PERCENT..SmartIslandSettings.MAX_CONTENT_PILL_PERCENT,
                    suffix = "%",
                    step = 5f,
                    onValueChange = { contentPercent = it },
                    onValueChangeFinished = { scope.launch { repository.setContentPillWidthPercent(true, contentPercent) } }
                )
            }
            SliderSettingItem(
                label = "Horizontal offset",
                value = pillX.coerceIn(-landscapeHalfWidthDp, landscapeHalfWidthDp),
                range = -landscapeHalfWidthDp..landscapeHalfWidthDp,
                onValueChange = { pillX = it },
                onValueChangeFinished = { scope.launch { repository.setLandscapePillGeometry(settings.landscapeWidth, settings.landscapeHeight, pillX, settings.landscapeYOffset) } }
            )
            SliderSettingItem(
                label = "Vertical offset",
                value = pillY,
                range = SmartIslandSettings.MIN_Y_OFFSET..SmartIslandSettings.MAX_LANDSCAPE_Y_OFFSET,
                onValueChange = { pillY = it },
                onValueChangeFinished = { scope.launch { repository.setLandscapePillGeometry(settings.landscapeWidth, settings.landscapeHeight, settings.landscapeXOffset, pillY) } }
            )
            if (settings.landscapePillPosition == SmartIslandSettings.LANDSCAPE_POSITION_LEFT ||
                settings.landscapePillPosition == SmartIslandSettings.LANDSCAPE_POSITION_RIGHT
            ) {
                GroupedRow(
                    title = "Centre on the side edge",
                    showChevron = false,
                    onClick = {
                        pillY = (landscapeHeightDp / 2f - settings.landscapeHeight / 2f)
                            .coerceIn(SmartIslandSettings.MIN_Y_OFFSET, SmartIslandSettings.MAX_LANDSCAPE_Y_OFFSET)
                        scope.launch { repository.setLandscapePillGeometry(settings.landscapeWidth, settings.landscapeHeight, settings.landscapeXOffset, pillY) }
                    }
                )
            }
            SliderSettingItem(
                label = "Opacity",
                value = pillOpacity * 100f,
                range = (SmartIslandSettings.MIN_OPACITY * 100f)..(SmartIslandSettings.MAX_OPACITY * 100f),
                suffix = "%",
                step = 5f,
                onValueChange = {
                    pillOpacity = (it / 100f).coerceIn(SmartIslandSettings.MIN_OPACITY, SmartIslandSettings.MAX_OPACITY)
                },
                onValueChangeFinished = { scope.launch { repository.setLandscapeOpacity(pillOpacity) } }
            )
        }

        CollapsibleGroup(
            title = stringResource(LayoutGroup.ExemptApps.titleRes),
            summary = stringResource(LayoutGroup.ExemptApps.summaryRes),
            stateKey = "land_exempt_apps",
            searchKey = LayoutGroup.ExemptApps.searchKey
        ) {
            ExemptAppsRows(settings = settings, repository = repository, landscape = true)
        }
    }
}

private const val VERTICAL_TUNE_RANGE_DP = 60f
