package com.falcon.kitchenbuddy.ui

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation
import com.falcon.kitchenbuddy.MainActivity
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.adapter.FoodClicked
import com.falcon.kitchenbuddy.adapter.MealDisplayMode
import com.falcon.kitchenbuddy.adapter.UnselectableMealAdapter
import com.falcon.kitchenbuddy.helper.ActivityCallback
import com.falcon.kitchenbuddy.model.FoodModel
import com.falcon.kitchenbuddy.model.MenuModel
import com.falcon.kitchenbuddy.viewmodel.HistoryViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.history_meal_fragment.*
import java.util.*
import android.view.ViewGroup
import android.widget.FrameLayout
import com.falcon.kitchenbuddy.pojo.Food


class HistoryMealFragment : Fragment(), FoodClicked {

    private lateinit var viewModel: HistoryViewModel

    private lateinit var savedFoodList: ArrayList<FoodModel>

    private var isEditing: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.history_meal_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is ActivityCallback) {
            (activity as ActivityCallback).updateMenuIndex(R.id.nav_home)
        }
        (activity as MainActivity).supportActionBar?.title = "Meal Plan"
        activity?.let { viewModel = ViewModelProviders.of(it).get(HistoryViewModel::class.java) }

        viewModel.selectedFoodList.observe(this, Observer { foodList ->
            foodList?.let {

                Log.d("History", "trigger")
                var headerText = "Created " + relativeTime(foodList.timestamp)
                foodList.updateTimestamp?.let {
                    headerText += "\nUpdated " + relativeTime(it)
                }
                header.text = headerText

                isEditing = false
                //clean up the days
                var day = 1
                for (meal in foodList.foodList) {
                    if (meal.day != null) {
                        meal.day = "Meal $day"
                        day++
                    }
                }

                savedFoodList = foodList.foodList
                unselectable_list_meals.layoutManager = LinearLayoutManager(activity)
                unselectable_list_meals.adapter =
                        activity?.let { UnselectableMealAdapter(savedFoodList, it, MealDisplayMode.ARROW,this) }
                ViewCompat.setNestedScrollingEnabled(unselectable_list_meals, false)
            }

        })

        setHasOptionsMenu(true)

    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.history_meal_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {

            R.id.action_remove -> {
                val builder = AlertDialog.Builder(activity)
                builder.setTitle("Remove meal plan")
                        .setMessage("Are you sure to remove this meal plan?")
                        .setPositiveButton(android.R.string.yes) { _, _ ->
                            Toast.makeText(activity, "Remove meal plan successfully", Toast.LENGTH_LONG).show()

                            activity?.let { act ->
                                val pref = act.getSharedPreferences(getString(R.string.sp_name), 0)
                                val editor = pref.edit()
                                val foodListString = pref.getString(getString(R.string.sp_key_selectedFoodList), null)
                                if (foodListString != null) {
                                    val foodListHistory: ArrayList<MenuModel> = Gson().fromJson(foodListString, object : TypeToken<ArrayList<MenuModel>>() {}.type)
                                    val index = viewModel.selectedFoodListIndex.value
                                    if (index != null) {
                                        foodListHistory.removeAt(foodListHistory.size - index - 1)
                                        editor.putString(getString(R.string.sp_key_selectedFoodList), Gson().toJson(foodListHistory))
                                        editor.apply()
                                        Toast.makeText(activity, "Remove meal plan successfully", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(activity, "Remove meal plan failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }



                            activity?.let { activity -> Navigation.findNavController(activity, R.id.my_nav_host_fragment).popBackStack() }
                        }
                        .setNegativeButton(android.R.string.no) { _, _ -> }
                        .setIcon(R.drawable.ic_delete_forever_black_24dp)
                        .show()

                return true
            }

            R.id.action_edit -> {
                unselectable_list_meals.adapter =
                        activity?.let { UnselectableMealAdapter(savedFoodList, it, MealDisplayMode.EDIT,this) }
                isEditing = true
                true
            }

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

    private fun generateText(): String {
        var message = "本周菜谱：\n"
        viewModel.selectedFoodList.value?.let {
            for (food in it.foodList) {
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

    private fun relativeTime(timestamp: Long): String {
        return DateUtils.getRelativeDateTimeString(
                activity, // Suppose you are in an activity or other Context subclass
                timestamp, // The time to display
                DateUtils.MINUTE_IN_MILLIS, // The resolution. This will display only
                // minutes (no "3 seconds ago")
                DateUtils.WEEK_IN_MILLIS, // The maximum resolution at which the time will switch
                // to default date instead of spans. This will not
                // display "3 weeks ago" but a full date instead
                0
        ).toString()
    }

    override fun onFoodClicked(food: FoodModel, index: Int) {

        if (isEditing){
            activity?.let { act ->
                val input = EditText(act)
                input.setSingleLine()
                input.hint = "Enter alternative food"
                val container = FrameLayout(act)
                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                params.leftMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                params.rightMargin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                input.layoutParams = params
                container.addView(input)

                val builder = AlertDialog.Builder(activity)
                builder.setTitle("Change your food")
                        .setMessage("Replace \"${food.food?.name}\" with")
                        .setView(container)
                        .setPositiveButton(android.R.string.yes) { _, _ ->

                            //save to view model
                            val enteredName = input.text.toString()
                            val menuModel: MenuModel? = viewModel.selectedFoodList.value
                            food.food = Food(enteredName, null, null, null)
                            menuModel?.foodList?.set(index, food)
                            menuModel?.updateTimestamp = Date().time
                            menuModel?.let { viewModel.storeSelectedFoodList(it) }

                            //save to shared preference
                            val pref = act.getSharedPreferences(getString(R.string.sp_name), 0)
                            val editor = pref.edit()
                            val foodListString = pref.getString(getString(R.string.sp_key_selectedFoodList), null)
                            if (foodListString != null) {
                                val foodListHistory: ArrayList<MenuModel> = Gson().fromJson(foodListString, object : TypeToken<ArrayList<MenuModel>>() {}.type)
                                val selectedFoodListIndex = viewModel.selectedFoodListIndex.value
                                if (selectedFoodListIndex != null && menuModel != null) {
                                    foodListHistory[foodListHistory.size - selectedFoodListIndex - 1] = menuModel
                                    editor.putString(getString(R.string.sp_key_selectedFoodList), Gson().toJson(foodListHistory))
                                    editor.apply()
                                    Toast.makeText(activity, "Update meal plan successfully", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(activity, "Update meal plan failed", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        .setNegativeButton(android.R.string.no) { _, _ -> }
                        .setIcon(R.drawable.ic_mode_edit_black_24dp)
                        .show()
            }
        }
        else {
            viewModel.storeSelectedFood(food)
            activity?.let {
                Navigation.findNavController(it, R.id.my_nav_host_fragment).navigate(R.id.foodDetailFragment)
            }
        }
    }

}
