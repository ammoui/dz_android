package com.example.android_dz_manychkin_2.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android_dz_manychkin_2.data.Recipe
import com.example.android_dz_manychkin_2.data.RecipeDetails
import com.example.android_dz_manychkin_2.data.RecipeStatus
import com.example.android_dz_manychkin_2.data.RecipeStats
import com.example.android_dz_manychkin_2.navigation.RecipeRoutes
import com.example.android_dz_manychkin_2.viewmodel.RecipeViewModel

@Composable
fun RecipeApp() {
    val vm: RecipeViewModel = viewModel()
    val ui = vm.uiState
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = RecipeRoutes.LIST) {
        composable(RecipeRoutes.LIST) {
            RecipeListScreen(
                recipes = ui.recipes,
                search = ui.search,
                filter = ui.filter,
                stats = ui.stats,
                onSearchChange = vm::onSearchChange,
                onFilterChange = vm::onFilterChange,
                onRecipeClick = { id -> navController.navigate(RecipeRoutes.details(id)) }
            )
        }

        composable(
            route = RecipeRoutes.DETAILS_PATTERN,
            arguments = listOf(navArgument(RecipeRoutes.ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getInt(RecipeRoutes.ID)
            RecipeDetailsScreen(
                details = recipeId?.let(vm::getDetails),
                onBack = { navController.navigateUp() },
                onStatusChange = { status -> recipeId?.let { vm.updateStatus(it, status) } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    search: String,
    filter: RecipeStatus?,
    stats: RecipeStats,
    onSearchChange: (String) -> Unit,
    onFilterChange: (RecipeStatus?) -> Unit,
    onRecipeClick: (Int) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Книга рецептов") }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = search,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Поиск по названию или категории") },
                singleLine = true,
            )

            StatusFilter(filter = filter, onFilterChange = onFilterChange)

            Text("Всего: ${stats.total} | Хочу: ${stats.want} | Готовлю: ${stats.cooking} | Готово: ${stats.done}")

            if (recipes.isEmpty()) {
                Text("Ничего не найдено")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recipes, key = { it.id }) { recipe ->
                        RecipeRow(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun StatusFilter(filter: RecipeStatus?, onFilterChange: (RecipeStatus?) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterButton(text = "Все", selected = filter == null) { onFilterChange(null) }
        FilterButton(text = RecipeStatus.WANT_TO_COOK.displayName(), selected = filter == RecipeStatus.WANT_TO_COOK) {
            onFilterChange(RecipeStatus.WANT_TO_COOK)
        }
        FilterButton(text = RecipeStatus.COOKING.displayName(), selected = filter == RecipeStatus.COOKING) {
            onFilterChange(RecipeStatus.COOKING)
        }
        FilterButton(text = RecipeStatus.COOKED.displayName(), selected = filter == RecipeStatus.COOKED) {
            onFilterChange(RecipeStatus.COOKED)
        }
    }
}

@Composable
fun FilterButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !selected) {
        Text(text)
    }
}

@Composable
fun RecipeRow(recipe: Recipe, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(recipe.title, fontWeight = FontWeight.Bold)
            Text("${recipe.category} • ${recipe.time} мин • ${recipe.difficulty}")
        }
        Text(recipe.status.displayName())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailsScreen(
    details: RecipeDetails?,
    onBack: () -> Unit,
    onStatusChange: (RecipeStatus) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Детали рецепта") }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(onClick = onBack) { Text("Назад") }

            if (details == null) {
                Text("Рецепт не найден")
                return@Scaffold
            }

            Text(details.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Статус: ${details.status.displayName()}")
            Text("Время: ${details.time} мин")
            Text("Сложность: ${details.difficulty}")

            Text("Описание", fontWeight = FontWeight.Bold)
            Text(details.description)

            Text("Ингредиенты", fontWeight = FontWeight.Bold)
            details.ingredients.forEach { Text("• $it") }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { onStatusChange(RecipeStatus.WANT_TO_COOK) }) { Text(RecipeStatus.WANT_TO_COOK.displayName()) }
                Button(onClick = { onStatusChange(RecipeStatus.COOKING) }) { Text(RecipeStatus.COOKING.displayName()) }
                Button(onClick = { onStatusChange(RecipeStatus.COOKED) }) { Text(RecipeStatus.COOKED.displayName()) }
            }
        }
    }
}

private fun RecipeStatus.displayName(): String = when (this) {
    RecipeStatus.WANT_TO_COOK -> "Хочу приготовить"
    RecipeStatus.COOKING -> "Готовлю"
    RecipeStatus.COOKED -> "Готово"
}
