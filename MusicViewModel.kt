package com.example.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class MusicViewModel : ViewModel() {
    private val _songsList = MutableStateFlow<List<Song>>(emptyList())
    val songsList = _songsList.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var job: Job? = null

    fun scanDeviceMusic(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = mutableListOf<Song>()
                val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION
                )

                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

                    while (cursor.moveToNext()) {
                        // قراءة البيانات بشكل آمن لمنع قفل التطبيق
                        val id = if (idCol != -1) cursor.getString(idCol) ?: "" else ""
                        val title = if (titleCol != -1) cursor.getString(titleCol) ?: "Unknown Title" else "Unknown Title"
                        val artist = if (artistCol != -1) cursor.getString(artistCol) ?: "Unknown Artist" else "Unknown Artist"
                        val path = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                        val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L

                        // إضافة الملفات السليمة فقط اللي مدتها أكبر من صفر
                        if (path.isNotEmpty() && duration > 0) {
                            list.add(Song(id, title, artist, path, duration))
                        }
                    }
                }
                _songsList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playSong(context: Context, song: Song) {
        try {
            mediaPlayer?.release()
            _currentSong.value = song
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(File(song.path)))
                prepare()
                start()
            }
            _isPlaying.value = true
            startTracking()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlay() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            } else {
                it.start()
                _isPlaying.value = true
                startTracking()
            }
        }
    }

    private fun startTracking() {
        job?.cancel()
        job = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                _currentPosition.value = mediaPlayer?.currentPosition?.toLong() ?: 0L
                delay(500)
            }
        }
    }
}
                MediaStore.Audio.Media.TITLE, 
                MediaStore.Audio.Media.ARTIST, 
                MediaStore.Audio.Media.DATA, 
                MediaStore.Audio.Media.DURATION
            )
            
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    list.add(Song(
                        id = cursor.getString(0),
                        title = cursor.getString(1),
                        artist = cursor.getString(2),
                        path = cursor.getString(3),
                        duration = cursor.getLong(4)
                    ))
                }
            }
            _songsList.value = list
        }
    }

    fun playSong(context: Context, song: Song) {
        mediaPlayer?.release()
        _currentSong.value = song
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, Uri.fromFile(File(song.path)))
            prepare()
            start()
        }
        _isPlaying.value = true
        startTracking()
    }

    fun togglePlay() {
        mediaPlayer?.let {
            if (it.isPlaying) { 
                it.pause()
                _isPlaying.value = false 
            } else { 
                it.start()
                _isPlaying.value = true
                startTracking() 
            }
        }
    }

    private fun startTracking() {
        job?.cancel()
        job = viewModelScope.launch {
            while (isActive && _isPlaying.value) {
                _currentPosition.value = mediaPlayer?.currentPosition?.toLong() ?: 0L
                delay(500)
            }
        }
    }
}
