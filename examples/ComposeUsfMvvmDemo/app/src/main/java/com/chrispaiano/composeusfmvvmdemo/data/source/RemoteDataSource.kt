package com.chrispaiano.composeusfmvvmdemo.data.source

import com.chrispaiano.composeusfmvvmdemo.data.model.Item
import kotlinx.coroutines.delay
import java.util.UUID

// Simulates fetching data from a remote API
class RemoteDataSource {
    suspend fun fetchItems(): List<Item> {
        delay(2000) // Simulate network delay
        return List(10) { index ->
            Item(
                id = UUID.randomUUID().toString(),
                name = "Item ${index + 1}",
                description = "Description for item ${index + 1}"
            )
        }
    }
}
