package com.falcon.kitchenbuddy.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.model.BrowseFoodModel
import kotlinx.android.synthetic.main.item_food.view.*



class BrowseAdapter(val context: Context, private val items: ArrayList<BrowseFoodModel>, private val mCallBack: BrowseFoodClicked): RecyclerView.Adapter<BrowseAdapter.MenuViewHolder>() {
    private val itemsCopy = items.toList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder(LayoutInflater.from(context).inflate(R.layout.item_food, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.mealIcon.setImageDrawable(
                items[position].imageId?.let { ContextCompat.getDrawable(context, it) })
        holder.listFoodName.text = items[position].food?.name

        holder.mealContainer.setOnClickListener {
            mCallBack.onFoodClicked(items[position])
        }
        holder.mealContainer.background = ContextCompat.getDrawable(
                context,
                R.drawable.food_background_white
        )
        holder.selectIcon.setImageDrawable(ContextCompat.getDrawable(
                context,
                R.drawable.ic_keyboard_arrow_right_black_24dp
        ))
    }

    fun filter(text: String) {
        items.clear()
        if (text.isEmpty()) {
            items.addAll(itemsCopy)
        } else {
            for (item in itemsCopy) {
                if (item.food?.name?.contains(text) == true) {
                    items.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    class MenuViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val mealContainer = view.select_meal_container
        val mealIcon = view.meal_icon
        val listFoodName = view.food_name_text_view
        val selectIcon = view.arrow_icon
    }
}

interface BrowseFoodClicked {
    fun onFoodClicked(selectedFood: BrowseFoodModel)
}