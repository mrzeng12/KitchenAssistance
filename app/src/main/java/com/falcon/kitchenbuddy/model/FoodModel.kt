package com.falcon.kitchenbuddy.model

import com.falcon.kitchenbuddy.pojo.Food

data class FoodModel(
        var id: Int?,
        var foodIcon: String?,
        var food: Food?,
        var day: String? = null,
        var isTitle: Boolean,
        var selected: Boolean = false

)
