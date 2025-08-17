package com.chrispaiano.composeusfmvvmdemo.data.source

import com.chrispaiano.composeusfmvvmdemo.data.model.ImageItem
import com.chrispaiano.composeusfmvvmdemo.data.model.Item
import com.chrispaiano.composeusfmvvmdemo.data.model.TextItem
import kotlinx.coroutines.delay
import java.util.UUID

// Simulates fetching data from a remote API
class RemoteDataSource {
    suspend fun fetchItems(): List<Item> {
        delay(2000) // Simulate network delay
        return List(10) { index ->
            // Alternate between text-only and image items
            if (index % 2 == 0) {
                TextItem(
                    id = UUID.randomUUID().toString(),
                    name = "Text Item ${index + 1}",
                    description = "This is a text-only item with description ${index + 1}"
                )
            } else {
                ImageItem(
                    id = UUID.randomUUID().toString(),
                    name = "Image Item ${index + 1}",
                    description = "This item has an image with description ${index + 1}",
                    imageUrl = "https://picsum.photos/300/200?random=${index + 1}"
                )
            }
        }
    }
}
