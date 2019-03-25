package com.falcon.kitchenbuddy.pojo


data class FoodDetail(

        val bean: Boolean? = false,

        val description: String? = null,

        val fish: Boolean? = false,

        val id: Int? = 0,

        val ingredient: List<String>? = null,

        val name: String? = null,

        val seafood: Boolean? = false,

        val shoppingList: List<String>? = null,

        val soup: Boolean? = false,

        val uuid: String? = null,

        val vegitable: Boolean? = false
)