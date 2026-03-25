package com.example.android_dz_manychkin_2.data

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
