package com.chrispaiano.composeusfmvvmdemo.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chrispaiano.composeusfmvvmdemo.data.repository.ItemRepository
import com.chrispaiano.composeusfmvvmdemo.data.source.RemoteDataSource
import com.chrispaiano.composeusfmvvmdemo.ui.items.ItemsScreen
import com.chrispaiano.composeusfmvvmdemo.ui.items.ItemsViewModel
import com.chrispaiano.composeusfmvvmdemo.ui.theme.ComposeUsfMvvmDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Create ViewModel with proper factory
        val viewModelFactory = viewModelFactory {
            initializer {
                val remoteDataSource = RemoteDataSource()
                val itemRepository = ItemRepository(remoteDataSource)
                ItemsViewModel(itemRepository)
            }
        }
        val itemsViewModel: ItemsViewModel = ViewModelProvider(this, viewModelFactory)[ItemsViewModel::class.java]
        
        setContent {
            ComposeUsfMvvmDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // Pass the ViewModel to the Composable screen with padding
                    ItemsScreen(
                        viewModel = itemsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )

                }
            }
        }
    }
}

/*
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}
*/

/*
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposeUsfMvvmDemoTheme {
        Greeting("Android")
    }
}*/

@Preview(showBackground = true)
@Composable
fun ItemsScreenPreview() {
    ComposeUsfMvvmDemoTheme {
        ItemsScreen()
    }
}
