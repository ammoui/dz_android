package com.example.android_dz_manychkin_3

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            val viewModel: MediaListViewModel = hiltViewModel()

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
            val mediaTypeArg = backStackEntry.arguments?.getString(DETAIL_MEDIA_TYPE_ARGUMENT)
            val mediaId = backStackEntry.arguments?.getInt(DETAIL_ID_ARGUMENT)
            val mediaType = MediaType.fromRouteValueOrNull(mediaTypeArg)

            if (mediaType == null || mediaId == null) {
                MissingArgumentsScreen(onBackClick = { navController.popBackStack() })
            } else {
                val viewModel: MediaDetailViewModel = hiltViewModel(backStackEntry)

                MediaDetailScreen(
                    uiState = viewModel.uiState,
                    mediaType = mediaType,
                    onBackClick = { navController.popBackStack() },
                    onRetry = { viewModel.retry() },
                    onToggleFavourite = { viewModel.toggleFavourite() },
                )
            }
        }
    }
}

@Composable
private fun MissingArgumentsScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Ошибка аргументов навигации",
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedButton(onClick = onBackClick, modifier = Modifier.padding(top = 12.dp)) {
            Text("Back")
        }
    }
}