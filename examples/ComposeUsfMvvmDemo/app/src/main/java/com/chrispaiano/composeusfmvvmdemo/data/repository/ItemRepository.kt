package com.chrispaiano.composeusfmvvmdemo.data.repository

import com.chrispaiano.composeusfmvvmdemo.data.model.Item
import com.chrispaiano.composeusfmvvmdemo.data.source.RemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ItemRepository(private val remoteDataSource: RemoteDataSource) {
    fun getItems(): Flow<List<Item>> = flow {
        // In a real app, you'd handle caching, local database access here
        // and decide whether to fetch from network or database.
        emit(remoteDataSource.fetchItems())
    }
}
