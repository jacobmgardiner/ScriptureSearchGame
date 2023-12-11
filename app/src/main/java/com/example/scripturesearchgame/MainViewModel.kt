package com.example.scripturesearchgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainViewModelFactory(private val bom: BookOfMormon) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(bom) as T
    }
}

class MainViewModel(private val bom: BookOfMormon) : ViewModel() {
    enum class PromptState {
        Unstarted,
        Playing,
        Guessed,
    }
    private val _promptState = MutableStateFlow(PromptState.Unstarted)
    val promptState = _promptState.asStateFlow()
    private var answerBook: Book? = null
    private var answerChapter: Chapter? = null
    private var answerVerse: Verse? = null
    private var elapsedTime: Long? = null
    private var promptWords = listOf<String>()
    private val _promptText = MutableStateFlow("")
    val promptText = _promptText.asStateFlow()
    private var hintWords = listOf<String>()
    private val _hintText = MutableStateFlow("")
    val hintText = _hintText.asStateFlow()
    private var wordCount = 0
    private var hintCount = 0
    private var updateTextJob: Job? = null
    private fun chooseNextPrompt() {
        answerBook = bom.books[Random.nextInt(bom.books.size)]
        answerChapter = answerBook?.chapters?.get(Random.nextInt(answerBook?.chapters?.size ?: 0))
        answerVerse = answerChapter?.verses?.get(Random.nextInt(answerChapter?.verses?.size ?: 0))
    }
    fun onStartPrompt() {
        chooseNextPrompt()
        _promptState.value = PromptState.Playing
        promptWords = answerVerse?.text?.split(" ") ?: listOf()
//        hintWords = answerVerse?.reference?.split(" ") ?: listOf()
        hintWords = listOf("${answerBook?.book}", " ${answerChapter?.chapter}", ":${answerVerse?.verse}")
        hintCount = 0
        _hintText.value = ""

        updateTextJob?.cancel()
        updateTextJob = viewModelScope.launch {
            wordCount = 0
            _promptText.value = ""
            while (promptState.value == PromptState.Playing) {
                onPromptTextUpdate()
                delay(1000)
            }
        }
    }
    private fun onPromptTextUpdate() {
        if (wordCount >= promptWords.size) {
//            promptWords = nextVerse(promptVerse).text.split(" ")
            return
        }
        _promptText.value += " ${promptWords[wordCount++]}"
    }
//    private fun nextVerse(verse: Verse): Verse {
//
//    }
    fun onContinuePrompt(onNav: (String) -> Unit) {
        _promptState.value = PromptState.Unstarted
        onNav("books")
    }
    fun onHint() {
        if (hintCount >= hintWords.size) {
            return
        }
        _hintText.value += hintWords[hintCount++]
    }
    fun onSkip(onNav: (String) -> Unit) {
        onContinuePrompt(onNav)
    }


    var selectedBook: Book? = null
    var selectedChapter: Chapter? = null
    private val _selectedVerse = MutableStateFlow<Verse?>(null)
    val selectedVerse = _selectedVerse.asStateFlow()

    var correctGuess: Boolean = false

    fun onBookSelected(book: Book, onNav: (String) -> Unit) {
        selectedBook = book
        _selectedVerse.value = null
        if ((selectedBook?.chapters?.size ?: 0) < 2) {
            selectedChapter = selectedBook?.chapters?.get(0)
            onNav("books/${book.book}/chapters/${selectedChapter?.chapter}")
        } else {
            onNav("books/${book.book}/chapters")
        }
    }

    fun onChapterSelected(chapter: Chapter, onNav: (String) -> Unit) {
        selectedChapter = chapter
        _selectedVerse.value = null
        onNav("books/${selectedBook?.book}/chapters/${selectedChapter?.chapter}")
    }

    fun onVerseSelected(verse: Verse) {
        _selectedVerse.value = verse
        correctGuess = answerVerse?.text == verse.text
        if (correctGuess) {
            _promptState.value = PromptState.Guessed
        }
    }
}