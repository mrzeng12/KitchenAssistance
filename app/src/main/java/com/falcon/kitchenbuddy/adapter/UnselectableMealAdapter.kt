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


class UnselectableMealAdapter(private val items: ArrayList<FoodModel>, val context: Context, private val mealDisplayMode: MealDisplayMode, private val mCallBack: FoodClicked)
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
            TYPE_TITLE -> UnselectableTitleViewHolder(LayoutInflater.from(context).inflate(R.layout.item_food_header, parent, false))
            else -> UnselectableMealViewHolder(LayoutInflater.from(context).inflate(R.layout.item_food, parent, false))
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
            (holder as UnselectableTitleViewHolder).mealTitle?.text = items[position].day
        } else {
            (holder as UnselectableMealViewHolder).mealIcon.setImageDrawable(
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
                mCallBack.onFoodClicked(items[position], position)
            }
            holder.mealContainer.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.food_background_white
            )
            when (mealDisplayMode) {
                MealDisplayMode.ARROW ->
                    holder.selectIcon.setImageDrawable(ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_keyboard_arrow_right_black_24dp
                    ))
                MealDisplayMode.EDIT ->
                    holder.selectIcon.setImageDrawable(ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_mode_edit_black_24dp
                    ))
                MealDisplayMode.NONE ->
                    holder.selectIcon.setImageDrawable(null)
            }
        }

    }

}

interface FoodClicked {
    fun onFoodClicked(food: FoodModel, index: Int)
}

class UnselectableTitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val mealTitle = view.header_title_text_view
}

class UnselectableMealViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val mealContainer = view.select_meal_container
    val mealIcon = view.meal_icon
    val listFoodName = view.food_name_text_view
    val selectIcon = view.arrow_icon
}

enum class MealDisplayMode {
    ARROW, EDIT, NONE
}