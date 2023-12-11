package com.example.scripturesearchgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            val navController = rememberNavController()
            val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(bom))

            ScriptureSearchGameTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        Surface(
                            modifier = Modifier.fillMaxHeight(1/4f),
                            tonalElevation = 4.dp,
                            shadowElevation = 4.dp,
                        ) {
                            Prompt(navController, viewModel)
                        }
//                        Divider(thickness = 4.dp, color = Color.Cyan)
                        Divider(thickness = 4.dp)
                        Box(Modifier.fillMaxSize()) {
                            VerseSelection(navController, viewModel, bom)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Prompt(navController: NavHostController, viewModel: MainViewModel) {
    val state = viewModel.promptState.collectAsState()

    when (state.value) {
        MainViewModel.PromptState.Unstarted -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button(onClick = { viewModel.onStartPrompt() }) {
                    Text(text = "Start")
                }
            }
        }
        MainViewModel.PromptState.Playing -> {
            val promptText = viewModel.promptText.collectAsState()
            val hintText = viewModel.hintText.collectAsState()
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = promptText.value)
                Spacer(modifier = Modifier)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = { viewModel.onSkip {navController.navigate(it)} }) {
                        Text(text = "Skip")
                    }

                    Row {
                        Button(onClick = { viewModel.onHint() }) {
                            Text(text = "Hint")
                        }
                        Text(text = hintText.value)
                    }
                }
            }
        }
        MainViewModel.PromptState.Guessed -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Correct!",
                    textAlign = TextAlign.Center
                )
                Button(onClick = { viewModel.onContinuePrompt { navController.navigate(it) } }) {
                    Text(text = "New Verse")
                }
            }
        }
    }
}

@Composable
fun VerseSelection(navController: NavHostController, viewModel: MainViewModel, bom: BookOfMormon) {
    NavHost(navController = navController, startDestination = "books") {
        composable("books") { Books(navController, viewModel, bom) }
        composable("books/{bookId}/chapters") { Chapters(navController, viewModel) }
        composable("books/{bookId}/chapters/{chapterId}") { Verses(navController, viewModel) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Books(navController: NavHostController, viewModel: MainViewModel, bom: BookOfMormon) {
    LazyColumn(Modifier.fillMaxWidth()) {
        items(bom.books) { book ->
            Box(Modifier.fillMaxWidth()) {
                Divider(thickness = 2.dp)
                Surface(
                    modifier = Modifier.fillMaxSize(),
//                    modifier = Modifier.fillParentMaxSize(),
                    onClick = { viewModel.onBookSelected(book) { navController.navigate(it) } }
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp, 4.dp),
                        text = book.book
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chapters(navController: NavHostController, viewModel: MainViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 64.dp)
    ) {
        items(
            viewModel.selectedBook?.chapters ?: listOf()
        ) { chapter ->
//            Button(onClick = { viewModel.onChapterSelected(chapter) { navController.navigate(it) } }) {
//                Text(text = chapter.chapter.toString())
//            }
            Box(
                modifier = Modifier
//                    .fillMaxSize()
                    .size(64.dp)
                    .padding(2.dp),
//                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    onClick = { viewModel.onChapterSelected(chapter) { navController.navigate(it) } },
                    shadowElevation = 4.dp,
                    tonalElevation = 4.dp,
//                border = BorderStroke(width = 2.dp, color = Color.LightGray),
//                    color = Color.White,
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.fillMaxSize(),
//                        .padding(16.dp, 4.dp),
                            text = chapter.chapter.toString(),
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Verses(navController: NavHostController, viewModel: MainViewModel) {
    val selectedVerse = viewModel.selectedVerse.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(viewModel.selectedChapter?.verses ?: listOf()) { verse ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
            Divider(thickness = 2.dp)
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