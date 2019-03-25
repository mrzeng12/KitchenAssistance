package com.falcon.kitchenbuddy.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.model.BrowseFoodModel
import com.falcon.kitchenbuddy.network.PythonService
import com.falcon.kitchenbuddy.pojo.FoodDetail
import com.falcon.kitchenbuddy.viewmodel.BrowseViewModel
import com.falcon.kitchenbuddy.viewmodel.HistoryViewModel
import com.google.gson.Gson
import com.falcon.kitchenbuddy.network.ApiResponse
import com.falcon.kitchenbuddy.network.ApiResult
import kotlinx.android.synthetic.main.food_detail_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FoodDetailFragment : Fragment() {

    companion object {
        fun newInstance() = FoodDetailFragment()
    }

    private lateinit var viewModel: HistoryViewModel

    private lateinit var browseViewModel: BrowseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.food_detail_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { viewModel = ViewModelProviders.of(it).get(HistoryViewModel::class.java) }
        activity?.let { browseViewModel = ViewModelProviders.of(it).get(BrowseViewModel::class.java) }
        // TODO: Use the ViewModel
        viewModel.selectedFood.observe(this, Observer {
            food_name.text = it?.food?.name
            it?.food?.uuid?.let { uuid ->
                serverCall(uuid)
            }?: run {
                more_info_button.text = "Item does not exist"
                more_info_button.isEnabled = false
            }
        })
    }

    private fun serverCall(uuid: String) {
        serverCallWithLiveData(uuid).observe(this, Observer { apiResponse ->
            if (apiResponse?.status == ApiResult.API_SUCCESS) {
                Log.e("retrieveList", "success")
                apiResponse.body?.let { foodDetail ->
                    Log.e("response", Gson().toJson(foodDetail))
                    if (foodDetail.isNotEmpty()) {
                        food_description.text = foodDetail[0].description
                        food_shopping_list.text = foodDetail[0].shoppingList?.joinToString()
                        more_info_button.setOnClickListener {
                            browseViewModel.storeSelectedFood(BrowseFoodModel(null, null, foodDetail[0], false))
                            browseViewModel.setMode(BrowseViewModel.EditMode.UPDATE)
                            activity?.let { Navigation.findNavController(it, R.id.my_nav_host_fragment).navigate(R.id.foodEditFragment) }
                        }
                    }
                    else {
                        more_info_button.text = "Item does not exist"
                        more_info_button.isEnabled = false
                    }
                }
                content_loading_progress_bar.hide()
            } else if (apiResponse?.status == ApiResult.API_ERROR) {
                Log.e("retrieveList", "failed")
                Log.e("response", apiResponse.message)

                more_info_button.text = "Item does not exist"
                more_info_button.isEnabled = false

                content_loading_progress_bar.hide()
                Toast.makeText(activity, apiResponse.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun serverCallWithLiveData(uuid: String): MutableLiveData<ApiResponse<List<FoodDetail>>> {

        val result = MutableLiveData<ApiResponse<List<FoodDetail>>>()

        content_loading_progress_bar.show()
        val retrofit = Retrofit.Builder()
                .baseUrl("http://73.33.10.164:80")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(PythonService::class.java)

        val foodList = ArrayList<String>()
        foodList.add(uuid)
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

}
