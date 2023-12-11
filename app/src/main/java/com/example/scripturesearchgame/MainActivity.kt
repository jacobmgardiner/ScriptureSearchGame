package com.example.scripturesearchgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scripturesearchgame.ui.theme.ScriptureSearchGameTheme
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fileName = "book-of-mormon.json"
        val jsonString = application.assets.open(fileName).bufferedReader().use {
            it.readText()
        }
        val bom = Gson().fromJson(jsonString, BookOfMormon::class.java)

        setContent {
            val viewModel: MainViewModel = viewModel()

            ScriptureSearchGameTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Box(Modifier.fillMaxHeight(1/4f)) {
                            Prompt()
                        }
                        Divider(thickness = 4.dp, color = Color.Cyan)
                        Box(Modifier.fillMaxSize()) {
                            VerseSelection(viewModel, bom)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Prompt() {
    Text(text = "TEST PROMPT!!!")
}

@Composable
fun VerseSelection(viewModel: MainViewModel, bom: BookOfMormon) {
    val state = viewModel.selectorState.collectAsState()
    
    when (state.value) {
        MainViewModel.SelectorState.BOOKS -> { Books(viewModel, bom) }
        MainViewModel.SelectorState.CHAPTERS -> { Chapters(viewModel) }
        MainViewModel.SelectorState.VERSES -> { Verses(viewModel) }
    }
}

@Composable
fun Books(viewModel: MainViewModel, bom: BookOfMormon) {
    LazyColumn(Modifier.fillMaxWidth()) {
        items(bom.books) { book ->
            Button(onClick = { viewModel.onBookSelected(book) }) {
                Text(text = book.book)
            }
        }
    }
}

@Composable
fun Chapters(viewModel: MainViewModel) {
//    LazyColumn {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 64.dp)
    ) {
        items(viewModel.selectedBook?.chapters ?: listOf()) { chapter ->
            Button(onClick = { viewModel.onChapterSelected(chapter) }) {
                Text(text = chapter.chapter.toString())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Verses(viewModel: MainViewModel) {
    val selectedVerse = viewModel.selectedVerse.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(viewModel.selectedChapter?.verses ?: listOf()) { verse ->
            Surface(
                color = if (selectedVerse.value?.verse == verse.verse) {
                    if (viewModel.correctGuess) Color.Green else Color.Red
                } else Color.Transparent,
                onClick = { viewModel.onVerseSelected(verse) }
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "${verse.verse} ${verse.text}"
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScriptureSearchGameTheme {
        Greeting("Android")
    }
}