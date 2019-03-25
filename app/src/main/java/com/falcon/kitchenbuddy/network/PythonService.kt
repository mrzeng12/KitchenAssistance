package com.falcon.kitchenbuddy.network

import com.falcon.kitchenbuddy.pojo.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PythonService {
    @POST("mealList/get")
    fun getMenu(@Body menu: WeeklyMenuRequest): Call<List<List<Food>>>

    @POST("foodList/get")
    fun getFood(@Body uuid:List<String>): Call<List<FoodDetail>>

    @POST("food/update")
    fun updateFood(@Body food: UpdateFoodRequest): Call<UpdateFoodResponse>

    @POST("food/add")
    fun addFood(@Body food: AddFoodRequest): Call<AddFoodResponse>

    @POST("food/remove")
    fun removeFood(@Body food: RemoveFoodRequest): Call<RemoveFoodResponse>
}