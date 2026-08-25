package com.kthakare.aiscanner

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kthakare.aiscanner.ui.detail.DetailViewModel
import com.kthakare.aiscanner.ui.detail.ScanDetailScreen
import com.kthakare.aiscanner.ui.library.LibraryScreen
import com.kthakare.aiscanner.ui.library.LibraryViewModel

@Composable
fun AiScannerApp() {
    val container = (LocalContext.current.applicationContext as AiScannerApplication).container
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            val viewModel: LibraryViewModel = viewModel(factory = LibraryViewModel.factory(container.repository))
            LibraryScreen(
                viewModel = viewModel,
                onOpenScan = { id -> navController.navigate("detail/$id") },
            )
        }
        composable(
            route = "detail/{scanId}",
            arguments = listOf(navArgument("scanId") { type = NavType.StringType }),
        ) { entry ->
            val scanId = entry.arguments?.getString("scanId").orEmpty()
            val viewModel: DetailViewModel = viewModel(
                factory = DetailViewModel.factory(scanId, container.repository, container.ocrAnalyzer),
            )
            ScanDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
