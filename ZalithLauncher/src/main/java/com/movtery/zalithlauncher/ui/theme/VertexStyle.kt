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

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Palet dan bentuk dasar tema Vertex (hitam & ungu) */
object Vtx {
    val Violet = Color(0xFFA77BFF)
    val VioletDeep = Color(0xFF8B3DFF)
    val VioletLight = Color(0xFFB58CFF)
    val Ink = Color(0xFF0B0813)
    val TextMain = Color(0xFFEFE9FA)
    val TextSoft = Color(0xFFB9AECF)

    /** Sudut terpotong di kanan atas dan kiri bawah */
    val CardShape = CutCornerShape(topEnd = 18.dp, bottomStart = 18.dp)
}

/** Skema warna hitam-ungu untuk seluruh halaman utama */
@Composable
fun VtxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFA77BFF),
            onPrimary = Color(0xFF1A0B3A),
            primaryContainer = Color(0xFF2A1F45),
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

/** Warna kartu semi-transparan */
@Composable
fun vtxCardColors(): CardColors = CardDefaults.cardColors(
    containerColor = Color(0xFF110D1C).copy(alpha = 0.72f),
    contentColor = Vtx.TextMain,
    disabledContainerColor = Color(0xFF110D1C).copy(alpha = 0.72f),
    disabledContentColor = Vtx.TextMain.copy(alpha = 0.5f)
)

/** Bingkai ungu tipis dengan gradien memudar */
fun vtxCardBorder(): BorderStroke = BorderStroke(
    width = 1.dp,
    brush = Brush.linearGradient(
        colors = listOf(Vtx.Violet.copy(alpha = 0.7f), Color(0xFF6D3BE0).copy(alpha = 0.18f))
    )
)

/** Berlian kecil sebagai penanda */
@Composable
fun VtxDiamond(size: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .rotate(45f)
            .background(color)
    )
}

/** Bingkai putus-putus (dashed) */
fun Modifier.vtxDashedBorder(color: Color): Modifier = this.drawBehind {
    val strokeWidth = 1.dp.toPx()
    drawRect(
        color = color,
        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))
        )
    )
}

/** Tombol ikon di rel kiri (sudut terpotong, semi-transparan) */
@Composable
fun VtxRailButton(
    selected: Boolean,
    painter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = CutCornerShape(topEnd = 10.dp, bottomStart = 10.dp)
    val fill = if (selected) Vtx.Violet.copy(alpha = 0.22f) else Color(0xFF110D1C).copy(alpha = 0.72f)
    val line = if (selected) Vtx.Violet.copy(alpha = 0.85f) else Vtx.Violet.copy(alpha = 0.22f)
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(shape)
            .background(fill, shape)
            .border(1.dp, line, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painter,
            contentDescription = contentDescription,
            tint = if (selected) Vtx.Violet else Vtx.TextMain
        )
    }
}

/** Tombol utama bergradien ungu (mis. LUNCURKAN) */
@Composable
fun VtxGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = CutCornerShape(topEnd = 16.dp, bottomStart = 16.dp)
    val brush = if (enabled) {
        Brush.horizontalGradient(listOf(Vtx.VioletDeep, Vtx.VioletLight))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF2A2338), Color(0xFF332B45)))
    }
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontWeight = FontWeight.Bold,
            letterSpacing = 3.sp,
            maxLines = 1,
            color = if (enabled) Vtx.Ink else Color(0xFF8C82A3)
        )
    }
}

/**
 * Tulisan sambutan di kiri atas ("LAUNCHER / Siap bermain.").
 * Bahasa mengikuti bahasa sistem: Indonesia atau Inggris.
 */
@Composable
fun VtxHeading(modifier: Modifier = Modifier) {
    val language = LocalConfiguration.current.locales[0]?.language
    val isIndonesian = language == "in" || language == "id"
    val titleFirst = if (isIndonesian) "Siap " else "Ready to "
    val titleSecond = if (isIndonesian) "bermain." else "play."
    val subtitle = if (isIndonesian) {
        "Tambahkan akun dan pasang versi untuk memulai."
    } else {
        "Add an account and install a version to get started."
    }

    Column(modifier = modifier) {
        Text(
            text = "LAUNCHER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 5.sp,
            color = Vtx.Violet
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFFF3ECFF))) { append(titleFirst) }
                withStyle(SpanStyle(color = Color(0xFFB07CFF))) { append(titleSecond) }
            },
            fontSize = 34.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = Vtx.TextSoft
        )
    }
}

private class VtxTwinkle(val x: Float, val y: Float, val size: Float, val phase: Float, val speed: Float)

private class VtxStreak(
    val startX: Float,
    val startY: Float,
    val length: Float,
    val phase: Float,
    val angle: Float,
    val big: Boolean
)

private fun DrawScope.drawVtxBand(
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
 * dan bintang jatuh (sebagian berukuran lebih besar).
 */
@Composable
fun Modifier.vtxSky(): Modifier {
    val transition = rememberInfiniteTransition(label = "VtxSky")
    val time = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VtxSkyTime"
    )
    val twinkles = remember {
        val random = Random(5)
        List(44) {
            VtxTwinkle(
                x = random.nextFloat(),
                y = random.nextFloat(),
                size = 0.6f + random.nextFloat() * 1.4f,
                phase = random.nextFloat(),
                speed = (1 + random.nextInt(3)).toFloat()
            )
        }
    }
    val streaks = remember {
        val random = Random(44)
        List(8) { index ->
            VtxStreak(
                startX = 0.35f + random.nextFloat() * 0.65f,
                startY = -0.05f + random.nextFloat() * 0.45f,
                length = 0.16f + random.nextFloat() * 0.12f,
                phase = (index + random.nextFloat() * 0.6f) / 8f,
                angle = 28f + random.nextFloat() * 14f,
                big = index == 2 || index == 5
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
            drawVtxBand(-0.03f * w, 0.50f * h, 0.85f * h, 0.11f * w, listOf(Color(0x883B1B8F), Color(0x00000000)))
            drawVtxBand(0.05f * w, 0.46f * h, 0.55f * h, 0.022f * w, listOf(Color(0x553B1B8F), Color(0x00000000)))
            drawVtxBand(0.72f * w, -0.10f * h, 0.90f * h, 0.10f * w, listOf(Color(0x663B1B8F), Color(0x223B1B8F)))
            drawVtxBand(0.66f * w, -0.10f * h, 0.40f * h, 0.02f * w, listOf(Color(0x44552AB0), Color(0x00000000)))

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
            val window = 0.2f
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
                    val tailLength = w * star.length * (if (star.big) 1.5f else 1f)
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
                        strokeWidth = (if (star.big) 3.5f else 2f).dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    if (star.big) {
                        drawCircle(
                            color = Color(0xFFB07CFF).copy(alpha = 0.25f * fade),
                            radius = 9.dp.toPx(),
                            center = head
                        )
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = fade),
                        radius = (if (star.big) 3f else 1.6f).dp.toPx(),
                        center = head
                    )
                }
            }
        }
}
