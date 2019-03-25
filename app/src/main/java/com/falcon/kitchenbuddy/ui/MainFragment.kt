package com.falcon.kitchenbuddy.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.navigation.Navigation
import com.falcon.kitchenbuddy.MainActivity

import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.adapter.MealClicked
import com.falcon.kitchenbuddy.adapter.SelectMealAdapter
import com.falcon.kitchenbuddy.helper.ActivityCallback
import com.falcon.kitchenbuddy.model.FoodModel
import com.falcon.kitchenbuddy.network.PythonService
import com.falcon.kitchenbuddy.pojo.Food
import com.falcon.kitchenbuddy.pojo.WeeklyMenuRequest
import com.falcon.kitchenbuddy.viewmodel.MainViewModel
import com.google.gson.Gson
import com.falcon.kitchenbuddy.network.ApiResponse
import com.falcon.kitchenbuddy.network.ApiResult
import kotlinx.android.synthetic.main.main_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainFragment : Fragment(),
        MealClicked {


    private var selectedItemsMaxCount: Int = 4
    private var numOfDishes: Int = 3
    private var numOfMeals: Int = 7
    private var num_fish_per_week: Int = 1
    private var num_bean_per_week: Int = 1
    private var num_vegi: Int = 1
    private var num_soup: Int = 1
    private var num_food_interval: Int = 2
    private var num_seafood_interval: Int = 3
    private var useChinese = true

    private lateinit var viewModel: MainViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is ActivityCallback) {
            (activity as ActivityCallback).updateMenuIndex(R.id.nav_home)
        }
        (activity as MainActivity).supportActionBar?.title = "New"
        activity?.let { viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java) }
        setHasOptionsMenu(true)
        if (viewModel.foodList.value == null) {
            clearUI()
            serverCall()
        } else {
            clearUI()
            viewModel.foodList.value?.let { loadList(processData(it)) }
        }

        setupAddButton()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.action_refresh -> {
                clearUI()
                serverCall()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun serverCall() {
        serverCallWithLiveData().observe(this, Observer { apiResponse ->
            if (apiResponse?.status == ApiResult.API_SUCCESS) {
                Log.e("retrieveList", "success")
                apiResponse.body?.let {
                    loadList(processData(it))
                    Log.e("response", Gson().toJson(it))
                    viewModel.storeFoodList(it)
                }
            } else if (apiResponse?.status == ApiResult.API_ERROR) {
                Log.e("retrieveList", "failed")
                Log.e("response", apiResponse.message)
                content_loading_progress_bar.hide()
                Toast.makeText(activity, apiResponse.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun serverCallWithLiveData(): MutableLiveData<ApiResponse<List<List<Food>>>> {

        val result = MutableLiveData<ApiResponse<List<List<Food>>>>()

        content_loading_progress_bar.show()
        val retrofit = Retrofit.Builder()
                .baseUrl("http://73.33.10.164:80")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(PythonService::class.java)

        val caller = service.getMenu(WeeklyMenuRequest(numOfMeals, numOfDishes, num_fish_per_week, num_bean_per_week, num_vegi, num_soup, num_food_interval, num_seafood_interval))
        caller.enqueue(object : Callback<List<List<Food>>> {
            override fun onFailure(call: Call<List<List<Food>>>, t: Throwable) {
                t.message?.let { result.value = ApiResponse.error(it) }
            }

            override fun onResponse(call: Call<List<List<Food>>>, response: Response<List<List<Food>>>) {
                if (response.code() != 200) {
                    result.value = ApiResponse.error(response.message())
                } else {
                    result.value = ApiResponse.success(response.body())
                }
            }
        })
        return result
    }

    private fun clearUI() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        selectedItemsMaxCount = Integer.parseInt(sharedPreferences.getString("selectedItemsMaxCount", "4"))
        numOfDishes = Integer.parseInt(sharedPreferences.getString("numOfDishes", "3"))
        numOfMeals = Integer.parseInt(sharedPreferences.getString("numOfMeals", "7"))
        num_fish_per_week = if (sharedPreferences.getBoolean("num_fish_per_week", true)) 1 else 0
        num_bean_per_week = if (sharedPreferences.getBoolean("num_bean_per_week", true)) 1 else 0
        num_vegi = Integer.parseInt(sharedPreferences.getString("num_vegi", "1"))
        num_soup = if (sharedPreferences.getBoolean("num_soup", true)) 1 else 0
        num_food_interval = Integer.parseInt(sharedPreferences.getString("num_food_interval", "2"))
        num_seafood_interval = Integer.parseInt(sharedPreferences.getString("num_seafood_interval", "3"))
        useChinese = sharedPreferences.getString("language", "Chinese").equals("Chinese")

        header.text = "Choose %s meals for the week".replace("%s", selectedItemsMaxCount.toString())
        select_add_button.text = "Select %s meals".replace("%s", selectedItemsMaxCount.toString())
        select_add_button.isEnabled = false
        select_add_button.isClickable = false
    }

    private fun loadList(foodModelList: ArrayList<FoodModel>) {

        list_meals.layoutManager = LinearLayoutManager(activity)
        list_meals.adapter =
                activity?.let { SelectMealAdapter(selectedItemsMaxCount, numOfDishes, foodModelList, it, this, ArrayList()) }
        ViewCompat.setNestedScrollingEnabled(list_meals, false)
        content_loading_progress_bar.hide()
    }

    private fun processData(foodList: List<List<Food>>): ArrayList<FoodModel> {
        var id = 0
        val foodModelList: ArrayList<FoodModel> = ArrayList()
        for ((i, meal) in foodList.withIndex()) {
            foodModelList.add(FoodModel(id, null, null, "Meal " + (i + 1), true))
            id++
            for (food in meal) {
//                if (useChinese) {
                food.name?.let { foodModelList.add(FoodModel(id, food.type, food, null, false)) }
//                } else {
//                    food.name?.let { foodModelList.add(FoodModel(id, foodIcon, it.split(" -- ")[1], null, foodType, false)) }
//                }
                id++
            }
        }
        return foodModelList
    }

    override fun onMealClicked(selectedMeals: ArrayList<FoodModel>) {
        list_meals.adapter?.notifyDataSetChanged()
        if (selectedMeals.size == selectedItemsMaxCount * (numOfDishes + 1)) {
            // plus 1 to consider the days
            enableSaveButton()
        } else {
            disableSaveButton(selectedMeals.size / (numOfDishes + 1))
        }
        viewModel.storeSelectedFoodList(selectedMeals)
    }

    private fun enableSaveButton() {
        header.text = "Great! You have %s meals selected!".replace("%s", selectedItemsMaxCount.toString())
        select_add_button.text = "Add these meals"
        select_add_button.isEnabled = true
        select_add_button.isClickable = true
    }

    private fun disableSaveButton(cantSelected: Int) {
        if (cantSelected == 0) {
            header.text = "Choose %s meals for the week".replace("%s", selectedItemsMaxCount.toString())
            select_add_button.text = "Select %s meals".replace("%s", selectedItemsMaxCount.toString())
        } else {
            header.text = "%s meals selected, %d more to go!"
                    .replace("%s", cantSelected.toString()).replace("%d", (selectedItemsMaxCount - cantSelected).toString())
            select_add_button.text = "Select %s meals".replace("%s", (selectedItemsMaxCount - cantSelected).toString())
        }
        select_add_button.isEnabled = false
        select_add_button.isClickable = false
    }

    private fun setupAddButton() {
        select_add_button.setOnClickListener {
            activity?.let { activity ->
                Navigation.findNavController(activity, R.id.my_nav_host_fragment).navigate(R.id.mealListFragment)
            }
        }
    }

}
