package com.falcon.kitchenbuddy.model

import com.falcon.kitchenbuddy.pojo.FoodDetail

data class BrowseFoodModel(
        var id: Int?,
        var imageId: Int?,
        var food: FoodDetail?,
        var selected: Boolean = false

)
