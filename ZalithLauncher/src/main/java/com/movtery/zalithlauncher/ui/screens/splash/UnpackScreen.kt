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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.components.InstallableItem
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedDirection
import com.movtery.zalithlauncher.ui.components.MarqueeText
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
private val VertexVioletDeep = Color(0xFF7C3AED)
private val VertexMint = Color(0xFF6EF3C5)
private val VertexRose = Color(0xFFFF6B8B)
private val VertexTextMain = Color(0xFFEFE9FA)
private val VertexTextSoft = Color(0xFFD3C8EA)

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
                        .padding(all = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UnpackTaskList(
                        modifier = Modifier
                            .weight(7f)
                            .fillMaxHeight(),
                        isVisible = isVisible,
                        items = items,
                    )

                    ActionMenu(
                        modifier = Modifier
                            .weight(3f)
                            .fillMaxHeight(),
                        isVisible = isVisible,
                        items = items,
                        onAgreeClick = onAgreeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionMenu(
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
    val finishedCount = states.count { it == InstallableItem.State.FINISHED }
    val ctx = rememberAnimatedContext(isVisible = isVisible)

    Column(
        modifier = modifier.animated(ctx, from = AnimatedDirection.Right),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(state = rememberScrollState())
                .weight(1f)
        ) {
            // Penghitung progres bergaya terminal
            Text(
                text = "[ $finishedCount / ${items.size} ]",
                style = MaterialTheme.typography.labelLarge,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = VertexViolet
            )
            Spacer(modifier = Modifier.height(8.dp))
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

        ScalingActionButton(
            enabled = !installing && states.any {
                it == InstallableItem.State.NOT_STARTED ||
                it == InstallableItem.State.PENDING ||
                it == InstallableItem.State.FAILED
            },
            modifier = Modifier.fillMaxWidth(),
            shape = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VertexVioletDeep,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF2A2338),
                disabledContentColor = Color(0xFF8C82A3)
            ),
            border = BorderStroke(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFC4A1FF), Color(0xFF6D3BE0))
                )
            ),
            onClick = onAgreeClick
        ) {
            MarqueeText(
                text = stringResource(
                    if (hasFailed) R.string.generic_retry else R.string.splash_screen_agree
                )
            )
        }
    }
}

@Composable
private fun UnpackTaskList(
    isVisible: Boolean,
    items: List<InstallableItem>,
    modifier: Modifier = Modifier,
) {
    val ctx = rememberAnimatedContext(isVisible = isVisible)

    // Panel semi-transparan dengan sudut terpotong ala antarmuka futuristik
    Surface(
        modifier = modifier.animated(ctx),
        shape = CutCornerShape(topStart = 28.dp, bottomEnd = 28.dp),
        color = Color(0xFF0B0813).copy(alpha = 0.55f),
        contentColor = VertexTextMain,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xCC8B5CF6), Color(0x228B5CF6), Color(0xCC6D3BE0))
            )
        )
    ) {
        val scrollState = rememberLazyListState()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nonInteractiveScrollbar(
                    state = scrollState.scrollIndicatorState!!,
                    orientation = Orientation.Vertical,
                ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            state = scrollState,
        ) {
            items(items) { item ->
                TaskItem(
                    item = item,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
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
        modifier = modifier,
        shape = CutCornerShape(topEnd = 14.dp, bottomStart = 14.dp),
        color = Color(0xFF8B5CF6).copy(alpha = 0.12f), // semi-transparan
        contentColor = VertexTextMain,
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(accent.copy(alpha = 0.65f), Color(0xFF8B5CF6).copy(alpha = 0.12f))
            )
        )
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Garis aksen vertikal di sisi kiri
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(accent)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                    .animateContentSize(animationSpec = getAnimateTween())
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                item.summary?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = VertexTextSoft.copy(alpha = 0.8f)
                    )
                }
                if (state == InstallableItem.State.RUNNING || state == InstallableItem.State.FAILED) {
                    message?.let { taskMessage ->
                        Text(
                            text = taskMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            maxLines = 1
                        )
                    }
                }
            }

            val iconModifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
                .size(18.dp)
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
            background = Color(0xFF1B1626),
            onBackground = Color(0xFFEFE9FA),
            surface = Color(0xFF211B2E),
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

/**
 * Latar abu-abu kehitaman (tidak terlalu gelap) dengan bintang berkelip
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
                colors = listOf(Color(0xFF241E32), Color(0xFF17131F))
            )
        )
        .drawBehind {
            val t = time.value
            val twoPi = 2f * PI.toFloat()

            // Bintang kecil yang berkelip
            twinkles.forEach { star ->
                val wave = (sin((t * star.speed + star.phase) * twoPi) + 1f) / 2f
                drawCircle(
                    color = Color(0xFFD9C8FF).copy(alpha = 0.12f + 0.5f * wave),
                    radius = star.size.dp.toPx() * 0.6f,
                    center = Offset(this.size.width * star.x, this.size.height * star.y)
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
                    val travel = this.size.width * 0.55f
                    val head = Offset(
                        x = this.size.width * star.startX + dirX * travel * q,
                        y = this.size.height * star.startY + dirY * travel * q
                    )
                    val tailLength = this.size.width * star.length
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
