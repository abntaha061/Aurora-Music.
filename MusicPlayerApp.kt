package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.viewmodel.MusicViewModel
import com.example.model.Song

@Composable
fun MusicPlayerApp(viewModel: MusicViewModel) {
    val songs by viewModel.songsList.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Aurora Music", style = MaterialTheme.typography.headlineLarge)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Player Control
        currentSong?.let { song ->
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(song.title, style = MaterialTheme.typography.titleLarge)
                    Text(song.artist, style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { viewModel.togglePlay() }) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Songs List
        LazyColumn {
            items(songs) { song ->
                ListItem(
                    headlineContent = { Text(song.title) },
                    supportingContent = { Text(song.artist) },
                    modifier = Modifier.clickable { viewModel.playSong(context, song) }
                )
            }
        }
    }
}
