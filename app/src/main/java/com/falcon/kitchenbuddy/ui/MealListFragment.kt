package com.falcon.kitchenbuddy.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.Toast
import com.falcon.kitchenbuddy.MainActivity
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.adapter.FoodClicked
import com.falcon.kitchenbuddy.adapter.MealDisplayMode
import com.falcon.kitchenbuddy.adapter.UnselectableMealAdapter
import com.falcon.kitchenbuddy.helper.ActivityCallback
import com.falcon.kitchenbuddy.model.FoodModel
import com.falcon.kitchenbuddy.model.MenuModel
import com.falcon.kitchenbuddy.viewmodel.MainViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.meal_list_fragment.*
import java.util.*


class MealListFragment : Fragment(), FoodClicked {

    companion object {
        fun newInstance() = MealListFragment()
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.meal_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is ActivityCallback) {
            (activity as ActivityCallback).updateMenuIndex(R.id.nav_home)
        }
        (activity as MainActivity).supportActionBar?.title = "Meal Plan"
        activity?.let { viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java) }

        viewModel.selectedFoodList.observe(this, Observer { foodList ->
            foodList?.let {

                //clean up the days
                var day = 1
                for (meal in foodList) {
                    if (meal.day != null) {
                        meal.day = "Meal $day"
                        day++
                    }
                }

                unselectable_list_meals.layoutManager = LinearLayoutManager(activity)
                unselectable_list_meals.adapter =
                        activity?.let { UnselectableMealAdapter(foodList, it, MealDisplayMode.NONE,this) }
                ViewCompat.setNestedScrollingEnabled(unselectable_list_meals, false)
            }

        })

        confirm_selection_button.setOnClickListener {
            confirmMealPlan()
        }

        setHasOptionsMenu(true)

    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.meal_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.action_share -> {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, generateText())
                    type = "text/plain"
                }
                startActivity(sendIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmMealPlan() {
        activity?.let { act ->

            val pref = act.getSharedPreferences(getString(com.falcon.kitchenbuddy.R.string.sp_name), 0)
            val foodListHistoryString = pref.getString(getString(com.falcon.kitchenbuddy.R.string.sp_key_selectedFoodList), null)

            val editor = pref.edit()
            val foodListHistory: ArrayList<MenuModel>
            if (foodListHistoryString == null) {
                foodListHistory = ArrayList()
            } else {
                foodListHistory =
                        Gson().fromJson(foodListHistoryString, object : TypeToken<ArrayList<MenuModel>>() {}.type)
            }

            viewModel.selectedFoodList.value?.let {
                val timestamp = Date().time
                val menuModel = MenuModel(it, timestamp, null)
                foodListHistory.add(menuModel) }
            editor.putString(getString(R.string.sp_key_selectedFoodList), Gson().toJson(foodListHistory))
            editor.apply()

            val toast = Toast.makeText(act, "You've confirmed a meal plan!", Toast.LENGTH_LONG)
//            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
            confirm_selection_button.isEnabled = false
            confirm_selection_button.text = "You've confirmed a meal plan!"

            clearShoppingList()

        }
    }

    private fun clearShoppingList() {
        activity?.let { act ->
            val pref = act.getSharedPreferences(getString(R.string.sp_name), 0)
            val editor = pref.edit()
            editor.remove(getString(R.string.sp_key_purchasedItemList))
            editor.apply()
        }
    }

    private fun generateText(): String {
        var message = "本周菜谱：\n"
        viewModel.selectedFoodList.value?.let {
            for (food in it) {
                if (food.isTitle) {
                    message += "\n" + food.day + "\n\n"
                } else {
//                    message += food.food?.type+": "
                    message += food.food?.name + "\n"
                }
            }
        }
        return message
    }

    override fun onFoodClicked(food: FoodModel, index: Int) {
        viewModel.storeSelectedFood(food)
//        activity?.let {
//            Navigation.findNavController(it, R.id.my_nav_host_fragment).navigate(R.id.foodDetailFragment)
//        }
    }

}
