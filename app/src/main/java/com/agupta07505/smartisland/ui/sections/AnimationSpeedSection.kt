/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.agupta07505.smartisland.ui.sections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.agupta07505.smartisland.R
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.data.SmartIslandSettingsRepository
import com.agupta07505.smartisland.ui.components.GroupedSection
import com.agupta07505.smartisland.ui.components.SegmentedChoiceRow
import com.agupta07505.smartisland.ui.search.searchAnchor
import com.agupta07505.smartisland.util.AnimationSpeed
import com.agupta07505.smartisland.util.MergeStyle
import kotlinx.coroutines.launch

/**
 * Layout tab: how fast the island opens, closes and settles. Follows the Portrait / Landscape switch above it, so
 * each orientation keeps its own speed. One grouped row: the title and the three speeds.
 */
@Composable
fun AnimationSpeedSection(
    settings: SmartIslandSettings,
    repository: SmartIslandSettingsRepository,
    landscape: Boolean
) {
    val scope = rememberCoroutineScope()
    val selected = if (landscape) settings.animSpeedLandscape else settings.animSpeedPortrait
    val options = listOf(
        AnimationSpeed.ID_RELAXED to stringResource(R.string.anim_speed_relaxed),
        AnimationSpeed.ID_NORMAL to stringResource(R.string.anim_speed_normal),
        AnimationSpeed.ID_SNAPPY to stringResource(R.string.anim_speed_snappy)
    )
    GroupedSection(modifier = Modifier.searchAnchor("layout.anim_speed")) {
        SegmentedChoiceRow(
            title = stringResource(if (landscape) R.string.anim_speed_title_landscape else R.string.anim_speed_title_portrait),
            summary = stringResource(R.string.layout_summary_anim_speed),
            options = options,
            selected = selected,
            onSelect = { id ->
                scope.launch {
                    if (landscape) repository.setAnimSpeedLandscape(id) else repository.setAnimSpeedPortrait(id)
                }
            }
        )
    }
}

/**
 * Layout tab: how the small round cutout joins the main one when it merges. Follows the Portrait / Landscape switch
 * above it, so each orientation keeps its own choice.
 */
@Composable
fun MergeStyleSection(
    settings: SmartIslandSettings,
    repository: SmartIslandSettingsRepository,
    landscape: Boolean
) {
    val scope = rememberCoroutineScope()
    val selected = settings.mergeStyleKind(landscape).id
    val options = listOf(
        MergeStyle.ID_SLIDE to stringResource(R.string.merge_style_slide),
        MergeStyle.ID_BUBBLE to stringResource(R.string.merge_style_bubble)
    )
    GroupedSection(modifier = Modifier.searchAnchor("layout.merge_style")) {
        SegmentedChoiceRow(
            title = stringResource(if (landscape) R.string.merge_style_title_landscape else R.string.merge_style_title_portrait),
            summary = stringResource(R.string.layout_summary_merge_style),
            options = options,
            selected = selected,
            onSelect = { id ->
                scope.launch {
                    if (landscape) repository.setMergeStyleLandscape(id) else repository.setMergeStyle(id)
                }
            }
        )
    }
}
