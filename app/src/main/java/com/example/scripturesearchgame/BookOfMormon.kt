package com.example.scripturesearchgame

import java.lang.ref.PhantomReference

data class BookOfMormon(
    val books: List<Book>
)

data class Book(
    val book: String,
    val chapters: List<Chapter>
)

data class Chapter(
    val chapter: Int,
    val reference: String,
    val verses: List<Verse>
)

data class Verse(
    val reference: String,
    val text: String,
    val verse: Int,
)
