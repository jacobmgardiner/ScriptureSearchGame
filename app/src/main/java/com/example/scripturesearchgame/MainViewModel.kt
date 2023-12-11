package com.example.scripturesearchgame

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {
    enum class SelectorState {
        BOOKS,
        CHAPTERS,
        VERSES,
    }
    private val _selectorState = MutableStateFlow(SelectorState.BOOKS)
    val selectorState = _selectorState.asStateFlow()

    var selectedBook: Book? = null
    var selectedChapter: Chapter? = null
    private val _selectedVerse = MutableStateFlow<Verse?>(null)
    val selectedVerse = _selectedVerse.asStateFlow()

    var correctGuess: Boolean = false

    fun onBookSelected(book: Book, onNav: (String) -> Unit) {
        _selectorState.value = SelectorState.CHAPTERS
        selectedBook = book

        onNav("books/${book.book}/chapters")
    }

    fun onChapterSelected(chapter: Chapter, onNav: (String) -> Unit) {
        _selectorState.value = SelectorState.VERSES
        selectedChapter = chapter

        onNav("books/${selectedBook?.book}/chapters/${selectedChapter?.chapter}")
    }

    fun onVerseSelected(verse: Verse) {
        _selectedVerse.value = verse
        //TODO check guess
        correctGuess = false
    }
}