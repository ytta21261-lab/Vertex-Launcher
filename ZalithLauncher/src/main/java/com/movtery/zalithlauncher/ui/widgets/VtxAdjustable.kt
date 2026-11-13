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

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange

/** Posisi dan ukuran sebuah kartu (dalam piksel), plus penanda sedang disesuaikan */
class VtxAdjustState(x: Float, y: Float, w: Float, h: Float) {
    var x by mutableFloatStateOf(x)
    var y by mutableFloatStateOf(y)
    var w by mutableFloatStateOf(w)
    var h by mutableFloatStateOf(h)
    var adjusting by mutableStateOf(false)

    /** Menjaga kartu tetap berada di dalam wadah dan tidak lebih kecil dari ukuran minimum */
    fun clampTo(containerW: Float, containerH: Float, minW: Float, minH: Float) {
        w = w.coerceIn(minOf(minW, containerW), maxOf(minOf(minW, containerW), containerW))
        h = h.coerceIn(minOf(minH, containerH), maxOf(minOf(minH, containerH), containerH))
        x = x.coerceIn(0f, maxOf(0f, containerW - w))
        y = y.coerceIn(0f, maxOf(0f, containerH - h))
    }
}

/**
 * Tahan lama di bagian tengah kartu lalu seret untuk menggeser.
 * Tahan lama di sudut kartu lalu tarik (ke atas, samping, atau bawah) untuk mengubah ukuran.
 * Gestur dibaca di tahap Initial supaya tombol di dalam kartu tetap bisa diketuk seperti biasa.
 */
fun Modifier.vtxAdjustable(
    state: VtxAdjustState,
    containerWidthPx: Float,
    containerHeightPx: Float,
    minWidthPx: Float,
    minHeightPx: Float,
    cornerPx: Float,
    onCommit: () -> Unit
): Modifier = pointerInput(containerWidthPx, containerHeightPx, minWidthPx, minHeightPx, cornerPx) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)

        // Tunggu sampai ditahan cukup lama tanpa bergeser
        val timedOut = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id }
                if (change == null || !change.pressed) break
                if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) break
            }
        } == null
        if (!timedOut) return@awaitEachGesture

        // Tentukan mode: sudut = ubah ukuran, selain itu = geser
        val p = down.position
        val horizontal = when {
            p.x < cornerPx -> -1
            p.x > state.w - cornerPx -> 1
            else -> 0
        }
        val vertical = when {
            p.y < cornerPx -> -1
            p.y > state.h - cornerPx -> 1
            else -> 0
        }
        val resizing = horizontal != 0 && vertical != 0

        state.adjusting = true
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) {
                change.consume()
                break
            }
            val delta: Offset = change.positionChange()
            if (delta != Offset.Zero) {
                if (resizing) {
                    resize(state, horizontal, vertical, delta, containerWidthPx, containerHeightPx, minWidthPx, minHeightPx)
                } else {
                    state.x = (state.x + delta.x).coerceIn(0f, maxOf(0f, containerWidthPx - state.w))
                    state.y = (state.y + delta.y).coerceIn(0f, maxOf(0f, containerHeightPx - state.h))
                }
            }
            change.consume()
        }
        state.adjusting = false
        onCommit()
    }
}

private fun resize(
    state: VtxAdjustState,
    horizontal: Int,
    vertical: Int,
    delta: Offset,
    containerW: Float,
    containerH: Float,
    minW: Float,
    minH: Float
) {
    // Horizontal
    if (horizontal > 0) {
        // Sudut kanan: tepi kiri tetap
        state.w = (state.w + delta.x).coerceIn(minW, maxOf(minW, containerW - state.x))
    } else {
        // Sudut kiri: tepi kanan tetap
        val right = state.x + state.w
        val newWidth = (state.w - delta.x).coerceIn(minW, maxOf(minW, right))
        state.x = right - newWidth
        state.w = newWidth
    }
    // Vertikal
    if (vertical > 0) {
        // Sudut bawah: tepi atas tetap
        state.h = (state.h + delta.y).coerceIn(minH, maxOf(minH, containerH - state.y))
    } else {
        // Sudut atas: tepi bawah tetap
        val bottom = state.y + state.h
        val newHeight = (state.h - delta.y).coerceIn(minH, maxOf(minH, bottom))
        state.y = bottom - newHeight
        state.h = newHeight
    }
}
