package com.example.android_dz_manychkin_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import com.example.android_dz_manychkin_2.ui.theme.Android_dz_manychkin_2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_dz_manychkin_2Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecipeApp()
                }
            }
        }
    }
}

enum class RecipeStatus { WANT_TO_COOK, COOKING, COOKED }

data class Recipe(
    val id: Int,
    val title: String,
    val category: String,
    val time: Int,
    val difficulty: String,
    val status: RecipeStatus,
)

data class RecipeDetails(
    val id: Int,
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val time: Int,
    val difficulty: String,
    val status: RecipeStatus,
)

data class RecipeStats(val want: Int, val cooking: Int, val done: Int) {
    val total: Int = want + cooking + done
}

data class RecipeListUiState(
    val search: String = "",
    val filter: RecipeStatus? = null,
    val recipes: List<Recipe> = sampleRecipes,
    val stats: RecipeStats = RecipeStats(0, 0, 0)
)

private val sampleRecipes = listOf(
    Recipe(1, "Паста с филиным куре", "Паста", 30, "Легко", RecipeStatus.WANT_TO_COOK),
    Recipe(2, "Чили кон карне", "Основное", 50, "Средне", RecipeStatus.COOKING),
    Recipe(3, "Панкейки", "Завтрак", 20, "Легко", RecipeStatus.COOKED),
    Recipe(4, "Карри с овощами", "Вегетарианское", 35, "Средне", RecipeStatus.WANT_TO_COOK),
    Recipe(5, "Брауни", "Десерт", 40, "Средне", RecipeStatus.COOKED),
)

private val sampleDetails = listOf(
    RecipeDetails(1, "Паста с филиным куре", "Сливочная паста с чесноком.", listOf("Паста", "Курица", "Сливки"), 30, "Легко", RecipeStatus.WANT_TO_COOK),
    RecipeDetails(2, "Чили кон карне", "Острая фасоль с говядиной.", listOf("Фарш", "Фасоль", "Томаты"), 50, "Средне", RecipeStatus.COOKING),
    RecipeDetails(3, "Панкейки", "Пышные на завтрак.", listOf("Мука", "Яйца", "Молоко"), 20, "Легко", RecipeStatus.COOKED),
    RecipeDetails(4, "Карри с овощами", "Кокосовое карри.", listOf("Овощи", "Нут", "Кокосовое молоко"), 35, "Средне", RecipeStatus.WANT_TO_COOK),
    RecipeDetails(5, "Брауни", "Шоколадный пирог.", listOf("Шоколад", "Яйца", "Масло"), 40, "Средне", RecipeStatus.COOKED),
)

class RecipeViewModel : ViewModel() {
    private var allRecipes by mutableStateOf(sampleRecipes)

    var uiState by mutableStateOf(
        RecipeListUiState(
            recipes = sampleRecipes,
            stats = buildStats(sampleRecipes)
        )
    )
        private set

    fun onSearchChange(newValue: String) {
        refresh(newValue, uiState.filter)
    }

    fun onFilterChange(newFilter: RecipeStatus?) {
        refresh(uiState.search, newFilter)
    }

    fun updateStatus(id: Int, status: RecipeStatus) {
        allRecipes = allRecipes.map { if (it.id == id) it.copy(status = status) else it }
        refresh(uiState.search, uiState.filter)
    }

    fun getDetails(id: Int): RecipeDetails? {
        val recipeStatus = allRecipes.find { it.id == id }?.status
        val details = sampleDetails.find { it.id == id }
        return details?.copy(status = recipeStatus ?: details.status)
    }

    private fun refresh(search: String, filter: RecipeStatus?) {
        val filtered = filterRecipes(search, filter)
        uiState = uiState.copy(
            search = search,
            filter = filter,
            recipes = filtered,
            stats = buildStats(filtered)
        )
    }

    private fun filterRecipes(query: String, filter: RecipeStatus?): List<Recipe> {
        val base = if (filter == null) allRecipes else allRecipes.filter { it.status == filter }
        if (query.isBlank()) return base
        return base.filter { it.title.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true) }
    }

    private fun buildStats(recipes: List<Recipe>): RecipeStats {
        return RecipeStats(
            want = recipes.count { it.status == RecipeStatus.WANT_TO_COOK },
            cooking = recipes.count { it.status == RecipeStatus.COOKING },
            done = recipes.count { it.status == RecipeStatus.COOKED },
        )
    }
}

object RecipeRoutes {
    const val LIST = "recipe_list"
    const val DETAILS = "recipe_details"
    const val ID = "recipeId"
    const val DETAILS_PATTERN = "$DETAILS/{$ID}"
    fun details(id: Int) = "$DETAILS/$id"
}

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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