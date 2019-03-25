package com.falcon.kitchenbuddy.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.falcon.kitchenbuddy.R
import kotlinx.android.synthetic.main.history_grid.view.*

class HistoryAdapter(private val context: Context, private val items: ArrayList<String>, private val historyClicked: HistoryClicked) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder(LayoutInflater.from(context).inflate(R.layout.history_grid, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.menuItem.text = items[position]
        holder.icon.setImageDrawable(ContextCompat.getDrawable(
                context,
                R.drawable.chef_menu
        ))
        holder.container.setOnClickListener{
            historyClicked.onHistoryClicked(position)
        }
    }

    class HistoryViewHolder(val view: View): RecyclerView.ViewHolder(view){
        val menuItem = view.menuItem
        val icon = view.history_meal_icon
        val container = view.select_meal_container
    }
}

interface HistoryClicked{
    fun onHistoryClicked(index: Int)
}