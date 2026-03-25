package com.example.android_dz_manychkin_2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.android_dz_manychkin_2.data.Recipe
import com.example.android_dz_manychkin_2.data.RecipeDetails
import com.example.android_dz_manychkin_2.data.RecipeStatus
import com.example.android_dz_manychkin_2.data.RecipeStats

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
