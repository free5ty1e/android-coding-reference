package com.chrispaiano.composeusfmvvmdemo.data.model

sealed class Item {
    abstract val id: String
    abstract val name: String
    abstract val description: String
}

data class TextItem(
    override val id: String,
    override val name: String,
    override val description: String
) : Item()

data class ImageItem(
    override val id: String,
    override val name: String,
    override val description: String,
    val imageUrl: String
) : Item()