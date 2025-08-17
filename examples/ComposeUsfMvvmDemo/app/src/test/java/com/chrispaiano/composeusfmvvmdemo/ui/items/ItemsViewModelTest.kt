package com.chrispaiano.composeusfmvvmdemo.ui.items

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.chrispaiano.composeusfmvvmdemo.data.model.ImageItem
import com.chrispaiano.composeusfmvvmdemo.data.model.Item
import com.chrispaiano.composeusfmvvmdemo.data.model.TextItem
import com.chrispaiano.composeusfmvvmdemo.data.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlinx.coroutines.flow.catch

@OptIn(ExperimentalCoroutinesApi::class)
class ItemsViewModelTest {

    // Rule to make Architecture Components work synchronously
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for controlling coroutine execution
    private val testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    // Mock the ItemRepository dependency
    private lateinit var mockRepository: ItemRepository

    // ViewModel instance
    private lateinit var viewModel: ItemsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and then Success with items`() = runTest {
        // Arrange
        val items = listOf(
            TextItem("1", "Test Item 1", "Description 1"),
            ImageItem("2", "Test Item 2", "Description 2", "url2")
        )
        coEvery { mockRepository.getItems() } returns flowOf(items)

        // Act
        viewModel = ItemsViewModel(mockRepository)

        // Assert initial loading state
        assertEquals(ItemsViewState.Loading, viewModel.viewState.value)

        advanceUntilIdle()

        // Assert success state
        assertTrue(viewModel.viewState.value is ItemsViewState.Success)
        val successState = viewModel.viewState.value as ItemsViewState.Success
        assertEquals(items, successState.items)
        assertEquals("Press the button for random text", successState.randomText)
    }

    @Test
    fun `refreshItems sets Loading and then Success state`() = runTest {
        // Arrange: Initial load data
        val initialItems = listOf(TextItem("initial", "Initial Item", "Initial Desc"))
        val refreshedItems = listOf(ImageItem("refreshed", "Refreshed Item", "Refreshed Desc", "url"))

        // Configure mock for the initial call during ViewModel init
        coEvery { mockRepository.getItems() } returns flowOf(initialItems)
        viewModel = ItemsViewModel(mockRepository)
        advanceUntilIdle()

        // Assert initial success state
        assertTrue(viewModel.viewState.value is ItemsViewState.Success)
        val initialSuccessState = viewModel.viewState.value as ItemsViewState.Success
        assertEquals(initialItems, initialSuccessState.items)
        assertEquals("Press the button for random text", initialSuccessState.randomText)

        // Configure mock for the refresh call
        coEvery { mockRepository.getItems() } returns flowOf(refreshedItems)

        // Trigger refresh
        viewModel.onEvent(ItemsViewEvent.RefreshItems)
        // Only assert final state after advancing
        advanceUntilIdle()

        // Assert refreshed success state
        assertTrue(viewModel.viewState.value is ItemsViewState.Success)
        val refreshedSuccessState = viewModel.viewState.value as ItemsViewState.Success
        assertEquals(refreshedItems, refreshedSuccessState.items)
        assertEquals("Press the button for random text", refreshedSuccessState.randomText)

        // Assert snackbar event for refresh
        val snackbarEffect = viewModel.singleEvents.take(1).toList().first() as ItemsViewEffect.ShowSnackbar
        assertEquals("Items refreshed!", snackbarEffect.message)
    }

    @Test
    fun `fetchItems handles error state correctly`() = runTest {
        // Arrange: Mock repository to throw an exception
        val errorMessage = "Network Error!"
        coEvery { mockRepository.getItems() } returns kotlinx.coroutines.flow.flow { throw RuntimeException(errorMessage) }

        // Trigger fetch
        viewModel = ItemsViewModel(mockRepository)
        // Only assert final state after advancing
        advanceUntilIdle()

        // Print actual state for diagnosis
        println("Actual state: " + viewModel.viewState.value)

        // Assert error state
        assertTrue(viewModel.viewState.value is ItemsViewState.Error)
        val errorState = viewModel.viewState.value as ItemsViewState.Error
        assertEquals(errorMessage, errorState.message)
        assertEquals("Press the button for random text", errorState.randomText)

        // Assert snackbar event for error
        val snackbarEffect = viewModel.singleEvents.take(1).toList().first() as ItemsViewEffect.ShowSnackbar
        assertEquals("Error: $errorMessage", snackbarEffect.message)
    }

    @Test
    fun `itemClicked sends ShowSnackbar event`() = runTest {
        // Arrange: Set up initial success state for the ViewModel
        val item = TextItem("clicked", "Clicked Item", "Clicked Desc")
        coEvery { mockRepository.getItems() } returns flowOf(listOf(item))
        viewModel = ItemsViewModel(mockRepository)
        advanceUntilIdle()
        assertTrue(viewModel.viewState.value is ItemsViewState.Success)

        // Drain the initial snackbar event from fetchItems
        viewModel.singleEvents.take(1).toList()

        // Act: Simulate item click
        viewModel.onEvent(ItemsViewEvent.ItemClicked(item))

        // Assert snackbar event
        val snackbarEffect = viewModel.singleEvents.take(1).toList().first() as ItemsViewEffect.ShowSnackbar
        assertEquals("Item clicked: ${item.name}", snackbarEffect.message)
    }

    @Test
    fun `generateRandomText updates randomText in view state`() = runTest {
        // Arrange: Start with a success state with some initial items
        val items = listOf(TextItem("1", "Test Item", "Desc"))
        coEvery { mockRepository.getItems() } returns flowOf(items)
        viewModel = ItemsViewModel(mockRepository)
        advanceUntilIdle()
        assertTrue(viewModel.viewState.value is ItemsViewState.Success)

        // Capture initial randomText from the view state
        val initialRandomText = (viewModel.viewState.value as ItemsViewState.Success).randomText

        // Act: Trigger generate random text event
        viewModel.onEvent(ItemsViewEvent.GenerateRandomText)
        advanceUntilIdle() // Allow coroutines to complete

        // Assert: Verify randomText is updated and is different from initial
        val updatedState = viewModel.viewState.value
        assertTrue(updatedState is ItemsViewState.Success)
        val newRandomText = (updatedState as ItemsViewState.Success).randomText
        assertTrue(newRandomText.startsWith("Generated:"))
        assertTrue(newRandomText != initialRandomText) // Should be different
    }
}
