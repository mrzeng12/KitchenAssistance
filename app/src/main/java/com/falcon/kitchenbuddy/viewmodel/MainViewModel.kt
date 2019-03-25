package com.falcon.kitchenbuddy.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel;
import com.falcon.kitchenbuddy.model.FoodModel
import com.falcon.kitchenbuddy.pojo.Food

class MainViewModel : ViewModel() {
    var foodList = MutableLiveData<List<List<Food>>>()
    var selectedFoodList = MutableLiveData<ArrayList<FoodModel>>()
    var selectedFood = MutableLiveData<FoodModel>()
    fun storeFoodList(food: List<List<Food>>) {
        foodList.value = food
    }

    fun storeSelectedFoodList(food: ArrayList<FoodModel>) {
        selectedFoodList.value = food
    }

    fun storeSelectedFood(food: FoodModel) {
        selectedFood.value = food
    }
}
