package com.example.android_dz_manychkin_2.navigation

object RecipeRoutes {
    const val LIST = "recipe_list"
    const val DETAILS = "recipe_details"
    const val ID = "recipeId"
    const val DETAILS_PATTERN = "$DETAILS/{$ID}"
    fun details(id: Int) = "$DETAILS/$id"
}
