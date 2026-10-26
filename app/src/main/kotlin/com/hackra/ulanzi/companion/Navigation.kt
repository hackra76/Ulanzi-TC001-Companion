package com.hackra.ulanzi.companion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.hackra.ulanzi.companion.data.DefaultUlanziRepository
import com.hackra.ulanzi.companion.ui.main.MainScreen
import com.hackra.ulanzi.companion.ui.main.MainScreenViewModel

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Main)
    val context = LocalContext.current
    val repository = remember(context) { DefaultUlanziRepository(context) }
    val viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(repository) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                MainScreen(
                    viewModel = viewModel,
                    onItemClick = { navKey -> backStack.add(navKey) }
                )
            }
        }
    )
}
