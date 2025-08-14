package com.chrispaiano.composeusfmvvmdemo.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chrispaiano.composeusfmvvmdemo.data.model.Item
import kotlinx.coroutines.flow.collectLatest // Used for single events

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(viewModel: ItemsViewModel = viewModel()) {
    val viewState by viewModel.viewState.collectAsState() // Observe ViewState
    val snackbarHostState = remember { SnackbarHostState() } // For Snackbar M3
    val coroutineScope = rememberCoroutineScope()

    // Handle One-Time Events (Side Effects)
    LaunchedEffect(Unit) { // Key 'Unit' ensures this runs once
        viewModel.singleEvents.collectLatest { effect -> // Collect latest event
            when (effect) {
                is ItemsViewEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                ItemsViewEffect.NavigateToDetailScreen -> {
                    // Perform navigation here (e.g., using a navController)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Use SnackbarHost with M3 state
        topBar = {
            TopAppBar(
                title = { Text("My Awesome App") },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ItemsViewEvent.RefreshItems) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh Items")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val currentViewState = viewState) { // Store viewState in a local variable
                ItemsViewState.Loading -> {
                    CircularProgressIndicator()
                    Text("Loading items...", modifier = Modifier.padding(top = 16.dp))
                }
                is ItemsViewState.Success -> {
                    val successState = currentViewState // Use the local variable for smart casting
                    // Display Random Text
                    Text(
                        text = successState.randomText,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Button to generate random text
                    Button(onClick = { viewModel.onEvent(ItemsViewEvent.GenerateRandomText) }) {
                        Text("Generate Random Text")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (successState.items.isEmpty()) {
                        Text("No items found. Tap refresh to fetch some.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(successState.items) { item -> // Use smart-casted successState
                                ItemCard(item = item) {
                                    viewModel.onEvent(ItemsViewEvent.ItemClicked(it))
                                }
                            }
                        }
                    }
                }
                is ItemsViewState.Error -> {
                    val errorState = currentViewState // Use the local variable for smart casting
                    Text(
                        text = "Error: ${errorState.message}", // Use smart-casted errorState
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )

                    // Display Random Text even in error state
                    Text(
                        text = errorState.randomText, // Use smart-casted errorState
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Button(onClick = { viewModel.onEvent(ItemsViewEvent.RefreshItems) }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCard(item: Item, onItemClick: (Item) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onItemClick(item) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
