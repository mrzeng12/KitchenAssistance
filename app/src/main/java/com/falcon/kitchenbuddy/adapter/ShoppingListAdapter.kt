package com.falcon.kitchenbuddy.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.model.ShoppingListModel
import kotlinx.android.synthetic.main.item_shopping_list.view.*

class ShoppingListAdapter(val context: Context, private val items: ArrayList<ShoppingListModel>, private val mCallBack: ShoppingListClicked): RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder {
        return ShoppingListViewHolder(LayoutInflater.from(context).inflate(R.layout.item_shopping_list, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int) {

        holder.shoppingItemName.text = items[position].shoppingItemName

        holder.foodName.text = items[position].foodName

        holder.mealContainer.setOnClickListener {
            if (!items[position].selected) {
                items[position].selected = true
                holder.checkMarkIcon.setImageDrawable(ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_check_box_black_24dp
                ))
                mCallBack.onFoodClicked(items[position], true)
            }
            else {
                items[position].selected = false
                holder.checkMarkIcon.setImageDrawable(ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_check_box_outline_blank_black_24dp
                ))
                mCallBack.onFoodClicked(items[position], false)
            }
        }
        holder.mealContainer.background = ContextCompat.getDrawable(
                context,
                R.drawable.food_background_white
        )
        if (items[position].selected) {
            holder.checkMarkIcon.setImageDrawable(ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_check_box_black_24dp
            ))
        }
        else {
            holder.checkMarkIcon.setImageDrawable(ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_check_box_outline_blank_black_24dp
            ))
        }
    }

    class ShoppingListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val mealContainer = view.select_meal_container
        val foodName = view.food_name_text_view
        val shoppingItemName = view.shopping_item_text_view
        val checkMarkIcon = view.arrow_icon
    }
}

interface ShoppingListClicked {
    fun onFoodClicked(selectedFood: ShoppingListModel, checked: Boolean)
}