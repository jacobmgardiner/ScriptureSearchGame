package com.example.scripturesearchgame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    private var promptVerse: Verse? = null
    private var elapsedTime: Long? = null
    private var promptWords = listOf<String>()
    private val _promptText = MutableStateFlow("")
    val promptText = _promptText.asStateFlow()
    private var wordCount = 0
    private fun chooseNextPrompt() {
        val randBook = bom.books[Random.nextInt(bom.books.size)]
        val randChapter = randBook.chapters[Random.nextInt(randBook.chapters.size)]
        promptVerse = randChapter.verses[Random.nextInt(randChapter.verses.size)]
    }
    fun onStartPrompt() {
        chooseNextPrompt()
        _promptState.value = PromptState.Playing
        promptWords = promptVerse?.text?.split(" ") ?: listOf()
        val updateText = viewModelScope.launch {
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
        correctGuess = promptVerse?.text == verse.text
        if (correctGuess) {
            _promptState.value = PromptState.Guessed
        }
    }
}