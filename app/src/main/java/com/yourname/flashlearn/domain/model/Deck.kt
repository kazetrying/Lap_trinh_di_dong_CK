package com.yourname.flashlearn.domain.model

data class Deck(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val coverColor: String = "#4CAF50",
    val language: String = "en",
    val cardCount: Int = 0,
    val dueCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)