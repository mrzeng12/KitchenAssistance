package com.falcon.kitchenbuddy.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.SearchView
import androidx.navigation.Navigation
import com.falcon.kitchenbuddy.MainActivity
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.adapter.BrowseAdapter
import com.falcon.kitchenbuddy.adapter.BrowseFoodClicked
import com.falcon.kitchenbuddy.helper.ActivityCallback
import com.falcon.kitchenbuddy.model.BrowseFoodModel
import com.falcon.kitchenbuddy.network.PythonService
import com.falcon.kitchenbuddy.pojo.FoodDetail
import com.falcon.kitchenbuddy.viewmodel.BrowseViewModel
import com.falcon.kitchenbuddy.network.ApiResponse
import com.falcon.kitchenbuddy.network.ApiResult
import kotlinx.android.synthetic.main.browse_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class BrowseFragment : Fragment(), BrowseFoodClicked {

    private lateinit var viewModel: BrowseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.browse_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is ActivityCallback) {
            (activity as ActivityCallback).updateMenuIndex(R.id.nav_browse)
        }
        (activity as MainActivity).supportActionBar?.title = "Browse"
        activity?.let { viewModel = ViewModelProviders.of(it).get(BrowseViewModel::class.java) }


        serverCallWithLiveData().observe(this, Observer {
            if (it?.status == ApiResult.API_SUCCESS) {
                content_loading_progress_bar.hide()
                it.body?.let { foodList -> loadUI(foodList) }

            } else if (it?.status == ApiResult.API_ERROR) {
                content_loading_progress_bar.hide()
            }
        })
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.browse_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.action_add -> {
                viewModel.setMode(BrowseViewModel.EditMode.ADD)
                viewModel.storeSelectedFood(BrowseFoodModel(null, null, null))
                activity?.let { Navigation.findNavController(it, R.id.my_nav_host_fragment).navigate(R.id.foodEditFragment) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadUI(foodList: List<FoodDetail>) {
        menu_list.layoutManager = LinearLayoutManager(activity)
        activity?.let {
            val adapter = BrowseAdapter(it, processData(foodList), this)
            menu_list.adapter = adapter
            search_bar.setQuery("", false)
            search_bar.isIconified = false
            search_bar.clearFocus()
            search_bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    adapter.filter(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    adapter.filter(newText)
                    return true
                }
            })
        }
        header.text = "${foodList.size} different dishes to choose from"

    }


    private fun processData(foodList: List<FoodDetail>): ArrayList<BrowseFoodModel> {
        val foodModelList: ArrayList<BrowseFoodModel> = ArrayList()
        for ((i, food) in foodList.reversed().withIndex()) {
            val imageId: Int = when {
                food.vegitable == true -> R.drawable.recipe_beans_icon
                food.soup == true -> R.drawable.recipe_soup_icon
                else -> R.drawable.recipe_chicken_icon
            }
            foodModelList.add(BrowseFoodModel(i, imageId, food, false))
        }
        return foodModelList
    }

    private fun serverCallWithLiveData(): MutableLiveData<ApiResponse<List<FoodDetail>>> {

        val result = MutableLiveData<ApiResponse<List<FoodDetail>>>()

        content_loading_progress_bar.show()
        val retrofit = Retrofit.Builder()
                .baseUrl("http://73.33.10.164:80")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(PythonService::class.java)

        val foodList = ArrayList<String>()
        val caller = service.getFood(foodList)
        caller.enqueue(object : Callback<List<FoodDetail>> {
            override fun onFailure(call: Call<List<FoodDetail>>, t: Throwable) {
                t.message?.let { result.value = ApiResponse.error(it) }
            }

            override fun onResponse(call: Call<List<FoodDetail>>, response: Response<List<FoodDetail>>) {
                if (response.code() != 200) {
                    result.value = ApiResponse.error(response.message())
                } else {
                    result.value = ApiResponse.success(response.body())
                }
            }
        })
        return result
    }

    override fun onFoodClicked(selectedFood: BrowseFoodModel) {
        viewModel.storeSelectedFood(selectedFood)
        viewModel.setMode(BrowseViewModel.EditMode.UPDATE)
        activity?.let { Navigation.findNavController(it, R.id.my_nav_host_fragment).navigate(R.id.foodEditFragment) }
    }

}
