/*
 * Smart Island (2026)
 * Copyright Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 */

package com.agupta07505.smartisland.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.agupta07505.smartisland.R
import com.agupta07505.smartisland.data.SmartIslandSettings
import com.agupta07505.smartisland.data.SmartIslandSettingsRepository
import com.agupta07505.smartisland.ui.Dimens
import com.agupta07505.smartisland.ui.SpringPreset
import com.agupta07505.smartisland.ui.components.CollapsibleGroup
import com.agupta07505.smartisland.ui.components.GroupedRow
import com.agupta07505.smartisland.ui.components.GroupedSection
import com.agupta07505.smartisland.ui.components.SwitchRow
import com.agupta07505.smartisland.ui.search.searchAnchor
import kotlinx.coroutines.launch

/**
 * How the island's size springs, and whether it glows while it moves fast (Rule_Book 11.3 Motion Physics): the
 * presets are one grouped list (stiffness k and damping d as values, an accent check on the active one); the kinetic
 * glow and backdrop blur switches each sit in their own group. Titles only, no explanatory text.
 */
@Composable
fun MotionSettings(
    settings: SmartIslandSettings,
    repository: SmartIslandSettingsRepository
) {
    val scope = rememberCoroutineScope()
    val selected = SpringPreset.fromId(settings.springPreset)
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)) {
        CollapsibleGroup(
            title = stringResource(LayoutGroup.MotionPhysics.titleRes),
            stateKey = "pos_motion",
            value = stringResource(presetTitle(selected)),
            searchKey = LayoutGroup.MotionPhysics.searchKey
        ) {
            SpringPreset.entries.forEach { preset ->
                GroupedRow(
                    title = stringResource(presetTitle(preset)),
                    showChevron = false,
                    onClick = { scope.launch { repository.setSpringPreset(preset.id) } },
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (preset.isPhysical) {
                                Text(
                                    text = stringResource(
                                        R.string.motion_preset_values_short,
                                        preset.stiffness.toInt(),
                                        formatDamping(preset.damping)
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (preset == selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = stringResource(R.string.motion_selected),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
        GroupedSection(modifier = Modifier.searchAnchor(LayoutGroup.KineticGlow.searchKey)) {
            SwitchRow(
                title = stringResource(R.string.motion_glow_title),
                checked = settings.kineticGlow,
                onCheckedChange = { scope.launch { repository.setKineticGlow(it) } }
            )
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            GroupedSection(modifier = Modifier.searchAnchor(LayoutGroup.BackdropBlur.searchKey)) {
                SwitchRow(
                    title = stringResource(R.string.motion_blur_title),
                    checked = settings.backdropBlur,
                    onCheckedChange = { scope.launch { repository.setBackdropBlur(it) } }
                )
            }
        }
    }
}

private fun presetTitle(preset: SpringPreset): Int = when (preset) {
    SpringPreset.Standard -> R.string.motion_standard
    SpringPreset.Bouncy -> R.string.motion_bouncy
    SpringPreset.Snappy -> R.string.motion_snappy
    SpringPreset.Critical -> R.string.motion_critical
    SpringPreset.Loose -> R.string.motion_loose
}

/** 12.0 -> "12", 27.5 -> "27.5". */
private fun formatDamping(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else value.toString()
