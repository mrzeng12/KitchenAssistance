package com.falcon.kitchenbuddy.ui

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.falcon.kitchenbuddy.MainActivity
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.adapter.ShoppingListAdapter
import com.falcon.kitchenbuddy.adapter.ShoppingListClicked
import com.falcon.kitchenbuddy.helper.ActivityCallback
import com.falcon.kitchenbuddy.model.MenuModel
import com.falcon.kitchenbuddy.model.ShoppingListModel
import com.falcon.kitchenbuddy.viewmodel.ShoppingListViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.shopping_list_fragment.*


class ShoppingListFragment : Fragment(), ShoppingListClicked {

    companion object {
        fun newInstance() = ShoppingListFragment()
    }

    private lateinit var viewModel: ShoppingListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.shopping_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is ActivityCallback) {
            (activity as ActivityCallback).updateMenuIndex(R.id.nav_shopping_list)
        }
        (activity as MainActivity).supportActionBar?.title = "Shopping List"
        viewModel = ViewModelProviders.of(this).get(ShoppingListViewModel::class.java)

        shopping_list.layoutManager = LinearLayoutManager(activity)
        val mealPlan = getMealPlan()
        if (mealPlan != null) {
            activity?.let { shopping_list.adapter = ShoppingListAdapter(it, processData(mealPlan), this) }
        }
    }

    private fun processData(mealPlan: ArrayList<MenuModel>): ArrayList<ShoppingListModel> {

        val shoppingList = HashMap<String, ArrayList<String>>()

        for (foodModel in mealPlan.last().foodList) {
            if (!foodModel.isTitle) {
                foodModel.food?.shoppingList?.let { list ->
                    for (shoppingItem in list) {
                        foodModel.food?.name?.let { foodName ->
                            var foodList: ArrayList<String>? = shoppingList[shoppingItem]
                            if (foodList == null) {
                                foodList = ArrayList()
                            }
                            foodList.add(foodName.split("--")[0].trim())
                            shoppingList.put(shoppingItem, foodList)
                        }
                    }
                }

            }
        }

        var purchasedItemList: HashMap<String, Boolean>? = null
        activity?.let { act ->
            val pref = act.getSharedPreferences(getString(com.falcon.kitchenbuddy.R.string.sp_name), 0) // 0 - for private mode
            val purchasedItemListString = pref.getString(getString(com.falcon.kitchenbuddy.R.string.sp_key_purchasedItemList), null)

            if (purchasedItemListString != null) {
                purchasedItemList = Gson().fromJson(purchasedItemListString, object : TypeToken<HashMap<String, Boolean>>() {}.type)
            }
        }


        val shoppingListModels: ArrayList<ShoppingListModel> = ArrayList()
        for ((shoppingItem, foodList) in shoppingList) {
            var selected = false
            purchasedItemList?.let { if (it[shoppingItem] == true) selected = true}
            shoppingListModels.add(ShoppingListModel(shoppingItem, "(" + foodList.joinToString(", ") + ")", selected))
        }
        return shoppingListModels

    }

    private fun getMealPlan(): ArrayList<MenuModel>? {
        activity?.let { act ->
            val pref = act.getSharedPreferences(getString(R.string.sp_name), 0) // 0 - for private mode
            val foodList = pref.getString(getString(R.string.sp_key_selectedFoodList), null)
            if (foodList != null) {
                return Gson().fromJson(foodList, object : TypeToken<ArrayList<MenuModel>>() {}.type)
            }
        }
        return null
    }

    override fun onFoodClicked(selectedFood: ShoppingListModel, checked: Boolean) {
        activity?.let { act ->
            val pref = act.getSharedPreferences(getString(R.string.sp_name), 0) // 0 - for private mode
            val purchasedItemListString = pref.getString(getString(R.string.sp_key_purchasedItemList), null)
            val purchasedItemList: HashMap<String, Boolean>
            if (purchasedItemListString != null) {
                purchasedItemList = Gson().fromJson(purchasedItemListString, object : TypeToken<HashMap<String, Boolean>>() {}.type)
            } else {
                purchasedItemList = HashMap()
            }
            purchasedItemList[selectedFood.shoppingItemName] = checked

            val editor = pref.edit()
            editor.putString(getString(R.string.sp_key_purchasedItemList), Gson().toJson(purchasedItemList))
            editor.apply()
        }
    }

}
