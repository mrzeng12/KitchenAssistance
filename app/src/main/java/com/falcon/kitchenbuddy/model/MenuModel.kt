package com.falcon.kitchenbuddy.model

data class MenuModel(
        var foodList: ArrayList<FoodModel>,
        var timestamp: Long,
        var updateTimestamp: Long?

)
