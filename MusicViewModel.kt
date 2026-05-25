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
