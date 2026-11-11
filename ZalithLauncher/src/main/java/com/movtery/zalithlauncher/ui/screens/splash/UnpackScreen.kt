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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.splash

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.nonInteractiveScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.animated
import com.movtery.zalithlauncher.ui.components.rememberAnimatedContext
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.viewmodel.SplashBackStackViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ---------- Palet warna Vertex (hitam & ungu) ----------
private val VertexViolet = Color(0xFFA77BFF)
private val VertexMint = Color(0xFF6EF3C5)
private val VertexRose = Color(0xFFFF6B8B)
private val VertexTitle = Color(0xFFF3ECFF)
private val VertexTitleAccent = Color(0xFFB07CFF)
private val VertexTextSoft = Color(0xFFB9AECF)
private val VertexTextDim = Color(0xFFA99FC0)

@Composable
fun UnpackScreen(
    items: List<InstallableItem>,
    screenViewModel: SplashBackStackViewModel,
    onAgreeClick: () -> Unit = {}
) {
    VertexUnpackTheme {
        BaseScreen(
            screenKey = SplashOperation.Unpacking,
            currentKey = screenViewModel.splashScreen.currentKey
        ) { isVisible ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .vertexSkyBackground()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    InfoPanel(
                        modifier = Modifier
                            .weight(4f)
                            .fillMaxHeight(),
                        isVisible = isVisible,
                        items = items,
                        onAgreeClick = onAgreeClick
                    )

                    UnpackTaskGrid(
                        modifier = Modifier
                            .weight(6f)
                            .fillMaxHeight(),
                        isVisible = isVisible,
                        items = items
                    )
                }
            }
        }
    }
}

/** Panel kiri: label, judul besar, keterangan, dan tombol pasang */
@Composable
private fun InfoPanel(
    isVisible: Boolean,
    items: List<InstallableItem>,
    modifier: Modifier = Modifier,
    onAgreeClick: () -> Unit = {}
) {
    val states = items.map { item ->
        item.state.collectAsStateWithLifecycle().value
    }
    val installing = states.any { it == InstallableItem.State.RUNNING }
    val hasFailed = states.any { it == InstallableItem.State.FAILED }
    val buttonEnabled = !installing && states.any {
        it == InstallableItem.State.NOT_STARTED ||
        it == InstallableItem.State.PENDING ||
        it == InstallableItem.State.FAILED
    }
    val ctx = rememberAnimatedContext(isVisible = isVisible)

    // Judul mengikuti bahasa sistem: Indonesia atau Inggris
    val language = LocalConfiguration.current.locales[0]?.language
    val isIndonesian = language == "in" || language == "id"
    val label = if (isIndonesian) "KOMPONEN" else "COMPONENTS"
    val titleFirst = if (isIndonesian) "Pasang" else "Install"
    val titleSecond = if (isIndonesian) "komponen." else "components."

    Column(
        modifier = modifier.animated(ctx),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                color = VertexViolet
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = titleFirst,
                fontSize = 32.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
                color = VertexTitle
            )
            Text(
                text = titleSecond,
                fontSize = 32.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp,
                color = VertexTitleAccent
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = if (installing) {
                    stringResource(R.string.splash_screen_installing)
                } else {
                    stringResource(R.string.splash_screen_unpack_desc)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = VertexTextSoft
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tombol gradien ungu dengan sudut terpotong
        val buttonBrush = if (buttonEnabled) {
            Brush.horizontalGradient(listOf(Color(0xFF8B3DFF), Color(0xFFB58CFF)))
        } else {
            Brush.horizontalGradient(listOf(Color(0xFF2A2338), Color(0xFF332B45)))
        }
        val buttonShape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp)
        ScalingActionButton(
            enabled = buttonEnabled,
            modifier = Modifier.fillMaxWidth(),
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFF0B0813),
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color(0xFF8C82A3)
            ),
            elevation = null,
            contentPadding = PaddingValues(0.dp),
            onClick = onAgreeClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(buttonBrush, buttonShape)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(
                        if (hasFailed) R.string.generic_retry else R.string.splash_screen_agree
                    ).uppercase(),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    maxLines = 1,
                    color = if (buttonEnabled) Color(0xFF0B0813) else Color(0xFF8C82A3)
                )
            }
        }
    }
}

/** Panel kanan: kartu komponen dalam dua kolom */
@Composable
private fun UnpackTaskGrid(
    isVisible: Boolean,
    items: List<InstallableItem>,
    modifier: Modifier = Modifier,
) {
    val ctx = rememberAnimatedContext(isVisible = isVisible)
    val gridState = rememberLazyGridState()
    val indicator = gridState.scrollIndicatorState

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier
            .animated(ctx, from = AnimatedDirection.Right)
            .then(
                if (indicator != null) {
                    Modifier.nonInteractiveScrollbar(
                        state = indicator,
                        orientation = Orientation.Vertical,
                    )
                } else {
                    Modifier
                }
            ),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { item ->
            TaskItem(item = item)
        }
    }
}

@Composable
private fun TaskItem(
    item: InstallableItem,
    modifier: Modifier = Modifier
) {
    val state by item.state.collectAsStateWithLifecycle()
    val message by item.task.taskMessage.collectAsStateWithLifecycle()

    // Warna aksen mengikuti status tugas
    val accent = when (state) {
        InstallableItem.State.FINISHED -> VertexMint
        InstallableItem.State.FAILED -> VertexRose
        else -> VertexViolet
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = CutCornerShape(topEnd = 14.dp, bottomStart = 14.dp),
        color = Color(0xFF110D1C).copy(alpha = 0.78f), // semi-transparan
        contentColor = VertexTitle,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(accent.copy(alpha = 0.7f), Color(0xFF6D3BE0).copy(alpha = 0.18f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .heightIn(min = 70.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .animateContentSize(animationSpec = getAnimateTween())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Berlian kecil sebagai penanda
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .rotate(45f)
                        .background(accent)
                )

                val iconModifier = Modifier.size(16.dp)
                when (state) {
                    InstallableItem.State.NOT_STARTED -> {
                        Icon(
                            modifier = iconModifier,
                            painter = painterResource(R.drawable.ic_folder_zip_outlined),
                            contentDescription = null,
                            tint = accent
                        )
                    }
                    InstallableItem.State.PENDING -> {
                        Icon(
                            modifier = iconModifier,
                            painter = painterResource(R.drawable.ic_update),
                            contentDescription = null,
                            tint = accent
                        )
                    }
                    InstallableItem.State.RUNNING -> {
                        CircularProgressIndicator(
                            modifier = iconModifier,
                            strokeWidth = 2.dp,
                            color = accent
                        )
                    }
                    InstallableItem.State.FINISHED -> {
                        Icon(
                            modifier = iconModifier,
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = accent
                        )
                    }
                    InstallableItem.State.FAILED -> {
                        Icon(
                            modifier = iconModifier,
                            painter = painterResource(R.drawable.ic_warning_filled),
                            contentDescription = null,
                            tint = accent
                        )
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = VertexTitle
            )
            item.summary?.let {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = VertexTextDim
                )
            }
            if (state == InstallableItem.State.RUNNING || state == InstallableItem.State.FAILED) {
                message?.let { taskMessage ->
                    Text(
                        text = taskMessage,
                        fontSize = 11.sp,
                        color = accent,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Skema warna hitam-ungu untuk layar pemasangan komponen */
@Composable
private fun VertexUnpackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFA77BFF),
            onPrimary = Color(0xFF1A0B3A),
            primaryContainer = Color(0xFF0B0813),
            onPrimaryContainer = Color(0xFFEDE4FF),
            secondary = Color(0xFF8E5CF7),
            onSecondary = Color(0xFFFFFFFF),
            tertiary = Color(0xFF6D3BE0),
            onTertiary = Color(0xFFFFFFFF),
            background = Color(0xFF0B0814),
            onBackground = Color(0xFFEFE9FA),
            surface = Color(0xFF14101E),
            onSurface = Color(0xFFEFE9FA),
            surfaceVariant = Color(0xFF2C2440),
            onSurfaceVariant = Color(0xFFD3C8EA),
            outline = Color(0xFF7A6E92)
        ),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content
    )
}

private class SkyTwinkle(val x: Float, val y: Float, val size: Float, val phase: Float, val speed: Float)

private class SkyStreak(
    val startX: Float,
    val startY: Float,
    val length: Float,
    val phase: Float,
    val angle: Float
)

/** Pita diagonal ungu (dekorasi) yang bergradien memudar */
private fun DrawScope.drawBand(
    x0: Float,
    y0: Float,
    length: Float,
    thickness: Float,
    colors: List<Color>
) {
    val path = Path().apply {
        moveTo(x0, y0)
        lineTo(x0 + thickness, y0)
        lineTo(x0 + thickness + length, y0 + length)
        lineTo(x0 + length, y0 + length)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.linearGradient(
            colors = colors,
            start = Offset(x0, y0),
            end = Offset(x0 + length, y0 + length)
        )
    )
}

/**
 * Latar hampir hitam dengan pita diagonal ungu, bintang berkelip,
 * dan bintang jatuh yang melintas secara berkala.
 */
@Composable
private fun Modifier.vertexSkyBackground(): Modifier {
    val transition = rememberInfiniteTransition(label = "UnpackSky")
    val time = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "UnpackSkyTime"
    )
    val twinkles = remember {
        val random = Random(11)
        List(40) {
            SkyTwinkle(
                x = random.nextFloat(),
                y = random.nextFloat(),
                size = 0.6f + random.nextFloat() * 1.4f,
                phase = random.nextFloat(),
                speed = (1 + random.nextInt(3)).toFloat()
            )
        }
    }
    val streaks = remember {
        val random = Random(33)
        List(7) { index ->
            SkyStreak(
                startX = 0.35f + random.nextFloat() * 0.65f,
                startY = -0.05f + random.nextFloat() * 0.45f,
                length = 0.16f + random.nextFloat() * 0.12f,
                phase = (index + random.nextFloat() * 0.6f) / 7f,
                angle = 28f + random.nextFloat() * 14f
            )
        }
    }

    return this
        .background(
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0C0916), Color(0xFF06040B))
            )
        )
        .drawBehind {
            val t = time.value
            val w = this.size.width
            val h = this.size.height
            val twoPi = 2f * PI.toFloat()

            // Pita diagonal dekoratif (kiri bawah dan kanan atas)
            drawBand(-0.03f * w, 0.50f * h, 0.85f * h, 0.11f * w, listOf(Color(0x883B1B8F), Color(0x00000000)))
            drawBand(0.05f * w, 0.46f * h, 0.55f * h, 0.022f * w, listOf(Color(0x553B1B8F), Color(0x00000000)))
            drawBand(0.72f * w, -0.10f * h, 0.90f * h, 0.10f * w, listOf(Color(0x663B1B8F), Color(0x223B1B8F)))
            drawBand(0.66f * w, -0.10f * h, 0.40f * h, 0.02f * w, listOf(Color(0x44552AB0), Color(0x00000000)))

            // Bintang kecil yang berkelip
            twinkles.forEach { star ->
                val wave = (sin((t * star.speed + star.phase) * twoPi) + 1f) / 2f
                drawCircle(
                    color = Color(0xFFD9C8FF).copy(alpha = 0.12f + 0.5f * wave),
                    radius = star.size.dp.toPx() * 0.6f,
                    center = Offset(w * star.x, h * star.y)
                )
            }

            // Bintang jatuh (miring dari kanan atas ke kiri bawah)
            val window = 0.22f
            streaks.forEach { star ->
                val p = (t + star.phase) % 1f
                if (p < window) {
                    val q = p / window
                    val rad = star.angle * PI.toFloat() / 180f
                    val dirX = -cos(rad)
                    val dirY = sin(rad)
                    val travel = w * 0.55f
                    val head = Offset(
                        x = w * star.startX + dirX * travel * q,
                        y = h * star.startY + dirY * travel * q
                    )
                    val tailLength = w * star.length
                    val tail = Offset(head.x - dirX * tailLength, head.y - dirY * tailLength)
                    val fade = sin(q * PI.toFloat())

                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFC9A8FF).copy(alpha = 0.9f * fade)
                            ),
                            start = tail,
                            end = head
                        ),
                        start = tail,
                        end = head,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = fade),
                        radius = 1.6.dp.toPx(),
                        center = head
                    )
                }
            }
        }
}
