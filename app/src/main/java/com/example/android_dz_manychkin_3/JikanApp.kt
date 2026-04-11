package com.example.android_dz_manychkin_3

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_dz_manychkin_3.model.MediaType
import com.example.android_dz_manychkin_3.ui.detail.MediaDetailScreen
import com.example.android_dz_manychkin_3.ui.detail.MediaDetailViewModel
import com.example.android_dz_manychkin_3.ui.list.MediaListScreen
import com.example.android_dz_manychkin_3.ui.list.MediaListViewModel

private const val LIST_ROUTE = "list"
private const val DETAIL_ROUTE = "detail"
private const val DETAIL_MEDIA_TYPE_ARGUMENT = "mediaType"
private const val DETAIL_ID_ARGUMENT = "id"

@Composable
fun JikanApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = LIST_ROUTE,
    ) {
        composable(LIST_ROUTE) {
            val viewModel: MediaListViewModel = viewModel()

            MediaListScreen(
                uiState = viewModel.uiState,
                onEvent = viewModel::onEvent,
                onOpenDetail = { mediaType, id ->
                    navController.navigate("$DETAIL_ROUTE/${mediaType.routeValue}/$id")
                }
            )
        }

        composable(
            route = "$DETAIL_ROUTE/{$DETAIL_MEDIA_TYPE_ARGUMENT}/{$DETAIL_ID_ARGUMENT}",
            arguments = listOf(
                navArgument(DETAIL_MEDIA_TYPE_ARGUMENT) { type = NavType.StringType },
                navArgument(DETAIL_ID_ARGUMENT) { type = NavType.IntType },
            )
        ) { backStackEntry ->
            val mediaType = MediaType.fromRouteValue(
                backStackEntry.arguments?.getString(DETAIL_MEDIA_TYPE_ARGUMENT)
            )
            val id = backStackEntry.arguments?.getInt(DETAIL_ID_ARGUMENT) ?: 0
            val viewModel: MediaDetailViewModel = viewModel()

            MediaDetailScreen(
                uiState = viewModel.uiState,
                mediaType = mediaType,
                mediaId = id,
                onBackClick = { navController.popBackStack() },
                onRetry = { viewModel.load(mediaType, id) },
                onFirstLoad = { viewModel.load(mediaType, id) }
            )
        }
    }
}