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

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.theme.Vtx
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Badan widget: dirender di dalam kartu grid beranda (yang bisa digeser dan diubah ukurannya).
 * Isinya bisa digulir bila kartu dikecilkan.
 */
@Composable
internal fun VtxWidgetBody(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        content = content
    )
}

@Composable
fun VtxClockContent() {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000L)
        }
    }

    val locale = Locale.getDefault()
    val time = now.format(DateTimeFormatter.ofPattern("HH:mm", locale))
    val seconds = now.format(DateTimeFormatter.ofPattern("ss", locale))
    val date = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale))

    VtxWidgetBody {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = time,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF3ECFF)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                modifier = Modifier.padding(bottom = 6.dp),
                text = seconds,
                fontSize = 16.sp,
                color = Vtx.Violet
            )
        }
        Text(
            text = date,
            fontSize = 12.sp,
            color = Vtx.TextSoft
        )
    }
}

@Composable
fun VtxSystemContent() {
    val context = LocalContext.current
    var ram by remember { mutableStateOf(readRam(context)) }
    var storage by remember { mutableStateOf(readStorage()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            ram = readRam(context)
            storage = readStorage()
        }
    }

    VtxWidgetBody {
        VtxUsageRow(
            label = stringResource(R.string.vx_system_ram),
            used = ram.first,
            total = ram.second
        )
        Spacer(modifier = Modifier.height(10.dp))
        VtxUsageRow(
            label = stringResource(R.string.vx_system_storage),
            used = storage.first,
            total = storage.second
        )
    }
}

@Composable
private fun VtxUsageRow(label: String, used: Long, total: Long) {
    val fraction = if (total > 0L) used.toFloat() / total.toFloat() else 0f
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.weight(1f),
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Vtx.TextMain
            )
            Text(
                text = "${formatGb(used)} / ${formatGb(total)} GB",
                fontSize = 11.sp,
                color = Vtx.TextSoft
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0x22FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(Vtx.VioletDeep, Vtx.VioletLight)))
            )
        }
    }
}

@Composable
fun VtxNotesContent() {
    val text = AllSettings.widgetNotesText.state
    VtxWidgetBody {
        BasicTextField(
            value = text,
            onValueChange = { AllSettings.widgetNotesText.save(it) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            textStyle = TextStyle(color = Vtx.TextMain, fontSize = 13.sp, lineHeight = 18.sp),
            cursorBrush = SolidColor(Vtx.Violet),
            decorationBox = { inner ->
                Box {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.vx_notes_hint),
                            fontSize = 13.sp,
                            color = Vtx.TextSoft.copy(alpha = 0.6f)
                        )
                    }
                    inner()
                }
            }
        )
    }
}

private fun readRam(context: Context): Pair<Long, Long> {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val info = ActivityManager.MemoryInfo()
    manager.getMemoryInfo(info)
    return (info.totalMem - info.availMem) to info.totalMem
}

private fun readStorage(): Pair<Long, Long> {
    val stat = StatFs(Environment.getDataDirectory().path)
    val total = stat.totalBytes
    return (total - stat.availableBytes) to total
}

private fun formatGb(bytes: Long): String =
    String.format(Locale.US, "%.1f", bytes / 1_073_741_824.0)
