package com.falcon.kitchenbuddy.pojo

data class WeeklyMenuRequest(
	val num_meals: Int? = null,
	val num_dishes: Int? = null,
	val num_fish_per_week: Int? = null,
	val num_bean_per_week: Int? = null,
	val num_vegi: Int? = null,
	val num_soup: Int? = null,
	val num_food_interval: Int? = null,
	val num_seafood_interval: Int? = null
)
