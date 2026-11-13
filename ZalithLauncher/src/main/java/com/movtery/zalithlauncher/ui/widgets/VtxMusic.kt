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

package com.movtery.zalithlauncher.ui.widgets

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.theme.Vtx
import com.movtery.zalithlauncher.ui.theme.VtxDiamond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

/** Satu lagu (dari ponsel atau dari Jamendo) */
class VtxTrack(
    val id: String,
    val title: String,
    val artist: String,
    val uri: String,
    val artwork: String?
)

/**
 * Pemutar musik tunggal (ExoPlayer) yang dipakai widget musik.
 * Lagu terus diputar selama proses launcher masih hidup.
 */
object VtxMusicPlayer {
    private var player: ExoPlayer? = null

    var queue by mutableStateOf<List<VtxTrack>>(emptyList())
        private set
    var currentIndex by mutableIntStateOf(-1)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var positionMs by mutableLongStateOf(0L)
        private set
    var durationMs by mutableLongStateOf(0L)
        private set

    val current: VtxTrack? get() = queue.getOrNull(currentIndex)

    private fun ensure(context: Context): ExoPlayer {
        player?.let { return it }
        val created = ExoPlayer.Builder(context.applicationContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .build()
        created.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                this@VtxMusicPlayer.isPlaying = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentIndex = created.currentMediaItemIndex
                positionMs = 0L
                durationMs = created.duration.coerceAtLeast(0L)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    durationMs = created.duration.coerceAtLeast(0L)
                }
            }
        })
        player = created
        return created
    }

    fun play(context: Context, tracks: List<VtxTrack>, index: Int) {
        if (tracks.isEmpty() || index !in tracks.indices) return
        val exo = ensure(context)
        queue = tracks
        currentIndex = index
        positionMs = 0L
        durationMs = 0L
        exo.setMediaItems(
            tracks.map { MediaItem.Builder().setMediaId(it.id).setUri(it.uri).build() },
            index,
            0L
        )
        exo.prepare()
        exo.play()
    }

    fun toggle() {
        player?.let { if (it.isPlaying) it.pause() else it.play() }
    }

    fun next() {
        player?.seekToNextMediaItem()
    }

    fun previous() {
        player?.seekToPreviousMediaItem()
    }

    fun seekToFraction(fraction: Float) {
        val exo = player ?: return
        if (durationMs <= 0L) return
        val target = (durationMs * fraction.coerceIn(0f, 1f)).toLong()
        exo.seekTo(target)
        positionMs = target
    }

    fun refreshPosition() {
        player?.let { positionMs = it.currentPosition.coerceAtLeast(0L) }
    }
}

/** Mengambil daftar lagu dari penyimpanan ponsel */
private object VtxLocalMusic {
    suspend fun load(context: Context): List<VtxTrack> = withContext(Dispatchers.IO) {
        val result = mutableListOf<VtxTrack>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
        )
        runCatching {
            context.contentResolver.query(
                collection,
                projection,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val artBase = Uri.parse("content://media/external/audio/albumart")
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val artist = cursor.getString(artistColumn)
                        ?.takeIf { it != "<unknown>" }
                        .orEmpty()
                    result += VtxTrack(
                        id = "local_$id",
                        title = cursor.getString(titleColumn) ?: "Unknown",
                        artist = artist,
                        uri = ContentUris.withAppendedId(collection, id).toString(),
                        artwork = ContentUris.withAppendedId(artBase, cursor.getLong(albumColumn)).toString()
                    )
                }
            }
        }
        result
    }
}

/** Pencarian musik online lewat API Jamendo (musik berlisensi Creative Commons) */
private object VtxJamendo {
    private val client by lazy { OkHttpClient() }

    suspend fun search(clientId: String, query: String): Result<List<VtxTrack>> = withContext(Dispatchers.IO) {
        runCatching {
            val url = HttpUrl.Builder()
                .scheme("https")
                .host("api.jamendo.com")
                .addPathSegments("v3.0/tracks/")
                .addQueryParameter("client_id", clientId)
                .addQueryParameter("format", "json")
                .addQueryParameter("limit", "25")
                .addQueryParameter("audioformat", "mp32")
                .addQueryParameter("imagesize", "200")
                .apply {
                    if (query.isBlank()) {
                        addQueryParameter("order", "popularity_week")
                    } else {
                        addQueryParameter("search", query.trim())
                    }
                }
                .build()

            client.newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                val body = response.body?.string().orEmpty()
                val root = JSONObject(body)
                val headers = root.optJSONObject("headers")
                if (headers != null && headers.optString("status") != "success") {
                    error(headers.optString("error_message", "error"))
                }
                val array = root.optJSONArray("results") ?: JSONArray()
                buildList {
                    for (i in 0 until array.length()) {
                        val item = array.getJSONObject(i)
                        val audio = item.optString("audio")
                        if (audio.isBlank()) continue
                        add(
                            VtxTrack(
                                id = "jamendo_${item.optString("id")}",
                                title = item.optString("name"),
                                artist = item.optString("artist_name"),
                                uri = audio,
                                artwork = item.optString("image").takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VtxMusicContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tab by remember { mutableIntStateOf(0) } // 0 = ponsel, 1 = online
    var query by remember { mutableStateOf("") }
    var localTracks by remember { mutableStateOf<List<VtxTrack>>(emptyList()) }
    var onlineTracks by remember { mutableStateOf<List<VtxTrack>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val noKeyText = stringResource(R.string.vx_music_no_key)
    val emptyText = stringResource(R.string.vx_music_empty)
    val errorText = stringResource(R.string.vx_music_error)

    val permission = remember {
        if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted = it }

    fun searchOnline() {
        val key = AllSettings.widgetJamendoClientId.state.trim()
        if (key.isEmpty()) {
            message = noKeyText
            return
        }
        scope.launch {
            loading = true
            message = null
            VtxJamendo.search(key, query)
                .onSuccess {
                    onlineTracks = it
                    if (it.isEmpty()) message = emptyText
                }
                .onFailure { message = errorText }
            loading = false
        }
    }

    LaunchedEffect(granted, tab) {
        if (tab == 0 && granted && localTracks.isEmpty()) {
            loading = true
            localTracks = VtxLocalMusic.load(context)
            loading = false
        }
    }
    LaunchedEffect(tab) {
        if (tab == 1 && onlineTracks.isEmpty()) searchOnline()
    }

    VtxWidgetBody {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VtxTab(
                text = stringResource(R.string.vx_music_tab_local),
                selected = tab == 0
            ) { tab = 0; message = null }
            VtxTab(
                text = stringResource(R.string.vx_music_tab_online),
                selected = tab == 1
            ) { tab = 1; message = null }
        }

        Spacer(modifier = Modifier.height(10.dp))
        VtxNowPlaying()
        Spacer(modifier = Modifier.height(10.dp))

        VtxSearchField(
            value = query,
            onValueChange = { query = it },
            hint = stringResource(R.string.vx_music_search_hint),
            onSearch = { if (tab == 1) searchOnline() }
        )
        Spacer(modifier = Modifier.height(6.dp))

        val shown = if (tab == 0) {
            localTracks.filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
            }
        } else {
            onlineTracks
        }

        when {
            tab == 0 && !granted -> {
                Text(
                    modifier = Modifier
                        .clickable(role = Role.Button) { permissionLauncher.launch(permission) }
                        .padding(vertical = 8.dp),
                    text = stringResource(R.string.vx_music_grant),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Vtx.Violet
                )
            }
            loading -> {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = stringResource(R.string.vx_music_loading),
                    fontSize = 12.sp,
                    color = Vtx.TextSoft
                )
            }
            tab == 1 && message != null -> {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = message.orEmpty(),
                    fontSize = 12.sp,
                    color = Vtx.TextSoft
                )
            }
            shown.isEmpty() -> {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp),
                    text = emptyText,
                    fontSize = 12.sp,
                    color = Vtx.TextSoft
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    itemsIndexed(shown) { index, track ->
                        VtxTrackRow(
                            track = track,
                            current = VtxMusicPlayer.current?.id == track.id
                        ) {
                            VtxMusicPlayer.play(context, shown, index)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VtxNowPlaying() {
    val track = VtxMusicPlayer.current
    val playing = VtxMusicPlayer.isPlaying
    val context = LocalContext.current

    LaunchedEffect(playing) {
        while (playing) {
            VtxMusicPlayer.refreshPosition()
            delay(500L)
        }
    }

    val artShape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(artShape)
                .background(Vtx.Violet.copy(alpha = 0.18f), artShape)
        ) {
            track?.artwork?.let { art ->
                AsyncImage(
                    model = art,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track?.title ?: stringResource(R.string.vx_music_nothing),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Vtx.TextMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track?.artist.orEmpty(),
                fontSize = 11.sp,
                color = Vtx.TextSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Bilah progres: ketuk untuk melompat ke posisi tertentu
    val fraction = if (VtxMusicPlayer.durationMs > 0L) {
        VtxMusicPlayer.positionMs.toFloat() / VtxMusicPlayer.durationMs.toFloat()
    } else 0f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    VtxMusicPlayer.seekToFraction(offset.x / size.width)
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
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

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VtxControlButton(kind = VtxControl.Previous, big = false) { VtxMusicPlayer.previous() }
        Spacer(modifier = Modifier.width(16.dp))
        VtxControlButton(
            kind = if (playing) VtxControl.Pause else VtxControl.Play,
            big = true
        ) {
            // Tidak melakukan apa pun bila belum ada lagu yang dipilih
            track?.let { VtxMusicPlayer.toggle() }
        }
        Spacer(modifier = Modifier.width(16.dp))
        VtxControlButton(kind = VtxControl.Next, big = false) { VtxMusicPlayer.next() }
    }
}

private enum class VtxControl { Previous, Play, Pause, Next }

@Composable
private fun VtxControlButton(kind: VtxControl, big: Boolean, onClick: () -> Unit) {
    val shape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
    val fill: Brush = if (big) {
        Brush.horizontalGradient(listOf(Vtx.VioletDeep, Vtx.VioletLight))
    } else {
        SolidColor(Color(0x33A77BFF))
    }
    val tint = if (big) Vtx.Ink else Vtx.TextMain
    Box(
        modifier = Modifier
            .size(if (big) 40.dp else 32.dp)
            .clip(shape)
            .background(fill, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(if (big) 18.dp else 14.dp)) {
            val w = this.size.width
            val h = this.size.height
            when (kind) {
                VtxControl.Play -> drawPath(
                    path = Path().apply {
                        moveTo(w * 0.2f, 0f)
                        lineTo(w, h / 2f)
                        lineTo(w * 0.2f, h)
                        close()
                    },
                    color = tint
                )
                VtxControl.Pause -> {
                    drawRect(tint, Offset(w * 0.15f, 0f), Size(w * 0.25f, h))
                    drawRect(tint, Offset(w * 0.60f, 0f), Size(w * 0.25f, h))
                }
                VtxControl.Next -> {
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(w * 0.78f, h / 2f)
                            lineTo(0f, h)
                            close()
                        },
                        color = tint
                    )
                    drawRect(tint, Offset(w * 0.85f, 0f), Size(w * 0.15f, h))
                }
                VtxControl.Previous -> {
                    drawRect(tint, Offset(0f, 0f), Size(w * 0.15f, h))
                    drawPath(
                        path = Path().apply {
                            moveTo(w, 0f)
                            lineTo(w * 0.22f, h / 2f)
                            lineTo(w, h)
                            close()
                        },
                        color = tint
                    )
                }
            }
        }
    }
}

@Composable
private fun VtxTab(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (selected) Vtx.Violet.copy(alpha = 0.25f) else Color(0x14FFFFFF),
                shape
            )
            .border(
                1.dp,
                if (selected) Vtx.Violet.copy(alpha = 0.8f) else Color(0x22FFFFFF),
                shape
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Vtx.Violet else Vtx.TextSoft
        )
    }
}

@Composable
private fun VtxSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    onSearch: () -> Unit
) {
    val shape = CutCornerShape(topEnd = 8.dp, bottomStart = 8.dp)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(color = Vtx.TextMain, fontSize = 13.sp),
        cursorBrush = SolidColor(Vtx.Violet),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Color(0x22A77BFF), shape)
                    .border(1.dp, Color(0x33A77BFF), shape)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = Vtx.TextSoft
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = hint,
                            fontSize = 13.sp,
                            color = Vtx.TextSoft.copy(alpha = 0.6f)
                        )
                    }
                    inner()
                }
            }
        }
    )
}

@Composable
private fun VtxTrackRow(track: VtxTrack, current: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VtxDiamond(
            size = 6.dp,
            color = if (current) Vtx.Violet else Color(0x55A77BFF)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                fontSize = 12.5.sp,
                color = if (current) Vtx.Violet else Vtx.TextMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (track.artist.isNotEmpty()) {
                Text(
                    text = track.artist,
                    fontSize = 10.5.sp,
                    color = Vtx.TextSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
