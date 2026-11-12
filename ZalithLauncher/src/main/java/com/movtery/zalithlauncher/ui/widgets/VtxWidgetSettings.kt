/*
 * Zalith Launcher 2
 * Copyright (C) 2025-2026 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.TextInputSettingsCard

/** Bagian pengaturan widget (ditaruh di halaman Pengaturan Launcher) */
@Composable
fun VtxWidgetSettings(modifier: Modifier = Modifier) {
    SettingsCardColumn(modifier = modifier) {
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Top,
            unit = AllSettings.widgetClock,
            title = stringResource(R.string.vx_widget_clock_title),
            summary = stringResource(R.string.vx_widget_clock_summary)
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.widgetSystem,
            title = stringResource(R.string.vx_widget_system_title),
            summary = stringResource(R.string.vx_widget_system_summary)
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.widgetNotes,
            title = stringResource(R.string.vx_widget_notes_title),
            summary = stringResource(R.string.vx_widget_notes_summary)
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.widgetMusic,
            title = stringResource(R.string.vx_widget_music_title),
            summary = stringResource(R.string.vx_widget_music_summary)
        )
        TextInputSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Bottom,
            unit = AllSettings.widgetJamendoClientId,
            title = stringResource(R.string.vx_widget_jamendo_title),
            summary = stringResource(R.string.vx_widget_jamendo_summary),
            label = stringResource(R.string.vx_widget_jamendo_label)
        )
    }
}
