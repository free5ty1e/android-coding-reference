package com.chrispaiano.composeusfmvvmdemo.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrispaiano.composeusfmvvmdemo.data.model.Item
import com.chrispaiano.composeusfmvvmdemo.data.model.ImageItem
import com.chrispaiano.composeusfmvvmdemo.data.model.TextItem
import com.chrispaiano.composeusfmvvmdemo.data.repository.ItemRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents the different states the UI can be in
sealed class ItemsViewState {
    object Loading : ItemsViewState()
    data class Success(val items: List<Item>, val randomText: String = "Press the button for random text") : ItemsViewState()
    data class Error(val message: String, val randomText: String = "Press the button for random text") : ItemsViewState()
}

// Represents one-time UI actions
sealed class ItemsViewEffect {
    data class ShowSnackbar(val message: String) : ItemsViewEffect()
    object NavigateToDetailScreen : ItemsViewEffect()
}

// Represents user interactions/events from the UI
sealed class ItemsViewEvent {
    object RefreshItems : ItemsViewEvent()
    data class ItemClicked(val item: Item) : ItemsViewEvent()
    object GenerateRandomText : ItemsViewEvent() // New event for generating random text
}

class ItemsViewModel(private val repository: ItemRepository) : ViewModel() {

    // ViewState: Exposed to the UI as an immutable StateFlow
    private val _viewState = MutableStateFlow<ItemsViewState>(ItemsViewState.Loading)
    val viewState: StateFlow<ItemsViewState> = _viewState.asStateFlow()

    // SingleEvents: Used for one-time UI actions, exposed as a Flow
    private val _singleEvents = Channel<ItemsViewEffect>()
    val singleEvents = _singleEvents.receiveAsFlow()

    init {
        fetchItems()
    }

    // Handles incoming UI events
    fun onEvent(event: ItemsViewEvent) {
        when (event) {
            ItemsViewEvent.RefreshItems -> fetchItems()
            is ItemsViewEvent.ItemClicked -> {
                viewModelScope.launch {
                    val itemName = when (event.item) {
                        is TextItem -> event.item.name
                        is ImageItem -> event.item.name
                    }
                    _singleEvents.send(ItemsViewEffect.ShowSnackbar("Item clicked: $itemName"))
                    // In a real app, this would trigger navigation:
                    // _singleEvents.send(ItemsViewEffect.NavigateToDetailScreen(event.item.id))
                }
            }
            ItemsViewEvent.GenerateRandomText -> generateRandomText() // Handle new event
        }
    }

    private fun fetchItems() {
        viewModelScope.launch {
            _viewState.update { ItemsViewState.Loading } // Update state to loading

            repository.getItems()
                .onEach { items ->
                    _viewState.update { currentState ->
                        when (currentState) {
                            is ItemsViewState.Success -> currentState.copy(items = items)
                            is ItemsViewState.Error -> ItemsViewState.Success(items = items, randomText = currentState.randomText)
                            ItemsViewState.Loading -> ItemsViewState.Success(items = items)
                        }
                    }
                    _singleEvents.send(ItemsViewEffect.ShowSnackbar("Items refreshed!"))
                }
                .catch { error ->
                    _viewState.update { currentState ->
                        when (currentState) {
                            is ItemsViewState.Success -> ItemsViewState.Error(message = error.message ?: "Unknown error", randomText = currentState.randomText)
                            is ItemsViewState.Error -> currentState.copy(message = error.message ?: "Unknown error")
                            ItemsViewState.Loading -> ItemsViewState.Error(message = error.message ?: "Unknown error")
                        }
                    }
                    _singleEvents.send(ItemsViewEffect.ShowSnackbar("Error: ${error.message}"))
                }
                .launchIn(viewModelScope) // Launch collection in viewModelScope
        }
    }

    private fun generateRandomText() {
        viewModelScope.launch {
            val randomString = (1..10).map { ('a'..'z').random() }.joinToString("")
            _viewState.update { currentState ->
                when (currentState) {
                    is ItemsViewState.Success -> currentState.copy(randomText = "Generated: $randomString")
                    is ItemsViewState.Error -> currentState.copy(randomText = "Generated: $randomString")
                    ItemsViewState.Loading -> ItemsViewState.Success(items = emptyList(), randomText = "Generated: $randomString")
                }
            }
        }
    }
}
