package com.falcon.kitchenbuddy.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.falcon.kitchenbuddy.model.FoodModel
import com.falcon.kitchenbuddy.model.MenuModel

class HistoryViewModel : ViewModel() {
    var selectedFood = MutableLiveData<FoodModel>()
    var selectedFoodList = MutableLiveData<MenuModel>()
    var selectedFoodListIndex = MutableLiveData<Int>()
    fun storeSelectedFood(food: FoodModel) {
        selectedFood.value = food
    }
    fun storeSelectedFoodList(menu: MenuModel){
        selectedFoodList.value = menu
    }
    fun storeselectedFoodListIndex(index: Int){
        selectedFoodListIndex.value = index
    }
}
