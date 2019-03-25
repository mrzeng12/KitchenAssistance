package com.falcon.kitchenbuddy.pojo


data class AddFoodRequest(

        val bean: Boolean? = false,

        val description: String? = null,

        val fish: Boolean? = false,

        val ingredient: List<String>? = null,

        val name: String? = null,

        val seafood: Boolean? = false,

        val shoppingList: List<String>? = null,

        val soup: Boolean? = false,

        val vegitable: Boolean? = false
)