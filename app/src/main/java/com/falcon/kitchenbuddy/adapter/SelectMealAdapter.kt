package com.falcon.kitchenbuddy.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.model.FoodModel
import kotlinx.android.synthetic.main.item_food.view.*
import kotlinx.android.synthetic.main.item_food_header.view.*


class SelectMealAdapter(private val selectedItemsMaxCount: Int, private val numOfDishes: Int, val items: ArrayList<FoodModel>, val context: Context,
                        private val mCallBack: MealClicked, private var selectedMeals: ArrayList<FoodModel>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_TITLE = 0
        const val TYPE_MEAL = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isTitle) TYPE_TITLE else TYPE_MEAL
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TITLE -> TitleViewHolder(LayoutInflater.from(context).inflate(R.layout.item_food_header, parent, false))
            else -> MealViewHolder(LayoutInflater.from(context).inflate(R.layout.item_food, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int
    ) {
        if (items[position].isTitle) {
            (holder as TitleViewHolder).mealTitle?.text = items[position].day
        } else {
            (holder as MealViewHolder).mealIcon.setImageDrawable(
                    items[position].foodIcon?.let {
                        when (it) {
                            "Vegi" -> {
                                ContextCompat.getDrawable(context, R.drawable.recipe_beans_icon)
                            }
                            "Meat" -> {
                                ContextCompat.getDrawable(context, R.drawable.recipe_chicken_icon)
                            }
                            "Soup" -> {
                                ContextCompat.getDrawable(context, R.drawable.recipe_soup_icon)
                            }
                            else -> null
                        }
                    })
            holder.listFoodName.text = items[position].food?.name

            holder.mealContainer.setOnClickListener {
                if (!items[position].selected && selectedMeals.size < selectedItemsMaxCount * (numOfDishes + 1)) {
                    for (i in 0..numOfDishes) { //start from 0 to include the days
                        items[(position / (numOfDishes + 1)) * (numOfDishes + 1) + i].selected = true
                        selectedMeals.add(items[(position / (numOfDishes + 1)) * (numOfDishes + 1) + i])
                    }

                } else if (items[position].selected) {
                    for (i in 0..numOfDishes) {
                        items[(position / (numOfDishes + 1)) * (numOfDishes + 1) + i].selected = false
                        selectedMeals.remove(items[(position / (numOfDishes + 1)) * (numOfDishes + 1) + i])
                    }
                }
                mCallBack.onMealClicked(selectedMeals)
            }
            if (items[position].selected) {
                holder.mealContainer.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.food_background_white
                )
                holder.selectIcon.setImageDrawable(ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_done
                ))
            } else {
                holder.mealContainer.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.list_background
                )
                holder.selectIcon.setImageDrawable(ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_plus
                ))
            }
        }

    }

}

interface MealClicked {
    fun onMealClicked(selectedMeals: ArrayList<FoodModel>)
}

class TitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val mealTitle = view.header_title_text_view
}

class MealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val mealContainer = view.select_meal_container
    val mealIcon = view.meal_icon
    val listFoodName = view.food_name_text_view
    val selectIcon = view.arrow_icon
}