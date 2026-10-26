package com.hackra.ulanzi.companion

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.hackra.ulanzi.companion.ui.main.MainScreen
import com.hackra.ulanzi.companion.ui.main.MainScreenViewModel

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Main)
    val app = UlanziCompanionApp.get()
    val viewModel: MainScreenViewModel = viewModel {
        MainScreenViewModel(app.repository, app.notificationSettingsManager)
    }

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
