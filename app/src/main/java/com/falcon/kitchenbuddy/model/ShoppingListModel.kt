package com.falcon.kitchenbuddy.model

data class ShoppingListModel(

        var shoppingItemName: String,
        var foodName: String,
        var selected: Boolean = false

)
