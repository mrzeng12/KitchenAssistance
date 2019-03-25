package com.falcon.kitchenbuddy.ui

import android.app.AlertDialog
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Toast
import androidx.navigation.Navigation

import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.model.BrowseFoodModel
import com.falcon.kitchenbuddy.network.PythonService
import com.falcon.kitchenbuddy.pojo.*
import com.falcon.kitchenbuddy.viewmodel.BrowseViewModel
import com.falcon.kitchenbuddy.network.ApiResponse
import com.falcon.kitchenbuddy.network.ApiResult
import kotlinx.android.synthetic.main.food_edit_fragment.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FoodEditFragment : Fragment() {

    companion object {
        fun newInstance() = FoodEditFragment()
    }

    private lateinit var viewModel: BrowseViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.food_edit_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { viewModel = ViewModelProviders.of(it).get(BrowseViewModel::class.java) }

        viewModel.isEditing.observe(this, Observer {
            if (it == false) {
                loadReadingView(viewModel.selectedFood.value)
            } else {
                loadEditingView(viewModel.selectedFood.value)
            }
        })
        if (viewModel.editMode.value == BrowseViewModel.EditMode.ADD) {
            viewModel.setEditingStatus(true)
            setHasOptionsMenu(false)
        } else {
            viewModel.setEditingStatus(false)
            setHasOptionsMenu(true)
        }
        edit_save_button.setOnClickListener {
            if (viewModel.isEditing.value == true) {

                //make server call
                content_loading_progress_bar.show()

                if (viewModel.editMode.value == BrowseViewModel.EditMode.ADD) {
                    addFoodServerCall(createAddFoodRequest()).observe(this@FoodEditFragment, Observer { apiResponse ->
                        if (apiResponse?.status == ApiResult.API_SUCCESS) {
                            Toast.makeText(activity, "Add food successfully", Toast.LENGTH_LONG).show()
                            activity?.let { activity -> Navigation.findNavController(activity, R.id.my_nav_host_fragment).popBackStack() }
                        }
                        if (apiResponse?.status == ApiResult.API_ERROR) {
                            Toast.makeText(activity, "Add food error", Toast.LENGTH_LONG).show()
                        }
                        content_loading_progress_bar.hide()
                    })
                } else {
                    updateFoodServerCall(createUpdateFoodRequest()).observe(this@FoodEditFragment, Observer { apiResponse ->
                        if (apiResponse?.status == ApiResult.API_SUCCESS) {
                            Toast.makeText(activity, "Update food successfully", Toast.LENGTH_LONG).show()
                        }
                        if (apiResponse?.status == ApiResult.API_ERROR) {
                            Toast.makeText(activity, "Update food error", Toast.LENGTH_LONG).show()
                        }
                        content_loading_progress_bar.hide()
                    })
                }

                viewModel.setEditingStatus(false)
            } else {
                viewModel.setEditingStatus(true)
                //begin editing
            }
        }
        cancel_button.setOnClickListener {
            if (viewModel.editMode.value == BrowseViewModel.EditMode.ADD) {
                activity?.let { activity -> Navigation.findNavController(activity, R.id.my_nav_host_fragment).popBackStack() }
            } else {
                viewModel.setEditingStatus(false)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.food_edit_list, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        return when (item?.itemId) {
            R.id.action_remove -> {
                val builder = AlertDialog.Builder(activity)
                builder.setTitle("Remove food")
                        .setMessage("Are you sure to remove this food?")
                        .setPositiveButton(android.R.string.yes){ _, _ ->
                            removeFoodServerCall(RemoveFoodRequest(viewModel.selectedFood.value?.food?.uuid)).observe(this@FoodEditFragment, Observer { apiResponse ->
                                if (apiResponse?.status == ApiResult.API_SUCCESS) {
                                    Toast.makeText(activity, "Remove food successfully", Toast.LENGTH_LONG).show()
                                    activity?.let { activity -> Navigation.findNavController(activity, R.id.my_nav_host_fragment).popBackStack() }
                                }
                                if (apiResponse?.status == ApiResult.API_ERROR) {
                                    Toast.makeText(activity, "Remove food error", Toast.LENGTH_LONG).show()
                                }
                                content_loading_progress_bar.hide()
                            })
                        }
                        .setNegativeButton(android.R.string.no){_,_->}
                        .setIcon(R.drawable.ic_delete_forever_black_24dp)
                        .show()

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createAddFoodRequest(): AddFoodRequest {
        val request = createUpdateFoodRequest()
        return AddFoodRequest(request.bean, request.description, request.fish, request.ingredient,
                request.name, request.seafood, request.shoppingList, request.soup, request.vegitable)
    }

    private fun createUpdateFoodRequest(): UpdateFoodRequest {
        val beanResult = bean.isChecked
        val descriptionResult = food_description_edit_text.text.toString()
        val fishResult = fish.isChecked
        val ingredientResult = ingredients_edit_text.text.toString().split("\n").filter { str -> !str.isEmpty() }
        val nameResult = food_name_edit_text.text.toString()
        val seafoodResult = seafood.isChecked
        val shoppingListResult = shopping_list_edit_text.text.toString().split("\n").filter { str -> !str.isEmpty() }
        val soupResult = soup.isChecked
        val uuid = viewModel.selectedFood.value?.food?.uuid
        val vegetableResult = vegetable.isChecked

        //create BrowseFoodModel,
        val foodDetail = FoodDetail(beanResult, descriptionResult, fishResult, 0, ingredientResult, nameResult,
                seafoodResult, shoppingListResult, soupResult, uuid, vegetableResult)
        val imageId: Int = when {
            foodDetail.vegitable == true -> R.drawable.recipe_beans_icon
            foodDetail.soup == true -> R.drawable.recipe_soup_icon
            else -> R.drawable.recipe_chicken_icon
        }

        viewModel.storeSelectedFood(BrowseFoodModel(0, imageId, foodDetail, false))

        return UpdateFoodRequest(beanResult, descriptionResult, fishResult, ingredientResult, nameResult,
                seafoodResult, shoppingListResult, soupResult, uuid, vegetableResult)
    }

    private fun updateFoodServerCall(updateFoodRequest: UpdateFoodRequest): MutableLiveData<ApiResponse<UpdateFoodResponse>> {

        val result = MutableLiveData<ApiResponse<UpdateFoodResponse>>()

        val retrofit = Retrofit.Builder()
                .baseUrl("http://73.33.10.164:80")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(PythonService::class.java)

        val caller = service.updateFood(updateFoodRequest)
        caller.enqueue(object : Callback<UpdateFoodResponse> {
            override fun onFailure(call: Call<UpdateFoodResponse>, t: Throwable) {
                t.message?.let { result.value = ApiResponse.error(it) }
            }

            override fun onResponse(call: Call<UpdateFoodResponse>, response: Response<UpdateFoodResponse>) {
                if (response.code() != 200) {
                    result.value = ApiResponse.error(response.message())
                } else {
                    result.value = ApiResponse.success(response.body())
                }
            }
        })
        return result
    }

    private fun addFoodServerCall(addFoodRequest: AddFoodRequest): MutableLiveData<ApiResponse<AddFoodResponse>> {

        val result = MutableLiveData<ApiResponse<AddFoodResponse>>()

        val retrofit = Retrofit.Builder()
                .baseUrl("http://73.33.10.164:80")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(PythonService::class.java)

        val caller = service.addFood(addFoodRequest)
        caller.enqueue(object : Callback<AddFoodResponse> {
            override fun onFailure(call: Call<AddFoodResponse>, t: Throwable) {
                t.message?.let { result.value = ApiResponse.error(it) }
            }

            override fun onResponse(call: Call<AddFoodResponse>, response: Response<AddFoodResponse>) {
                if (response.code() != 200) {
                    result.value = ApiResponse.error(response.message())
                } else {
                    result.value = ApiResponse.success(response.body())
                }
            }
        })
        return result
    }

    private fun removeFoodServerCall(removeFoodRequest: RemoveFoodRequest): MutableLiveData<ApiResponse<RemoveFoodResponse>> {

        val result = MutableLiveData<ApiResponse<RemoveFoodResponse>>()

        val retrofit = Retrofit.Builder()
                .baseUrl("http://73.33.10.164:80")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(PythonService::class.java)

        val caller = service.removeFood(removeFoodRequest)
        caller.enqueue(object : Callback<RemoveFoodResponse> {
            override fun onFailure(call: Call<RemoveFoodResponse>, t: Throwable) {
                t.message?.let { result.value = ApiResponse.error(it) }
            }

            override fun onResponse(call: Call<RemoveFoodResponse>, response: Response<RemoveFoodResponse>) {
                if (response.code() != 200) {
                    result.value = ApiResponse.error(response.message())
                } else {
                    result.value = ApiResponse.success(response.body())
                }
            }
        })
        return result
    }

    private fun loadEditingView(browseFoodModel: BrowseFoodModel?) {
        food_name_text_view.visibility = View.GONE
        food_name_edit_text.visibility = View.VISIBLE

        food_description_text_view.visibility = View.GONE
        food_description_edit_text.visibility = View.VISIBLE

        ingredients_text_view.visibility = View.GONE
        ingredients_edit_text.visibility = View.VISIBLE

        shopping_list_text_view.visibility = View.GONE
        shopping_list_edit_text.visibility = View.VISIBLE

        edit_save_button.text = "Save"
        cancel_button.visibility = View.VISIBLE

        browseFoodModel?.food?.let { foodDetail ->
            food_name_edit_text.setText(foodDetail.name)
            food_description_edit_text.setText(foodDetail.description)
            ingredients_edit_text.setText(foodDetail.ingredient?.joinToString("\n"))
            shopping_list_edit_text.setText(foodDetail.shoppingList?.joinToString("\n"))
        }
    }

    private fun loadReadingView(browseFoodModel: BrowseFoodModel?) {
        food_name_text_view.visibility = View.VISIBLE
        food_name_edit_text.visibility = View.GONE

        food_description_text_view.visibility = View.VISIBLE
        food_description_edit_text.visibility = View.GONE

        ingredients_text_view.visibility = View.VISIBLE
        ingredients_edit_text.visibility = View.GONE

        shopping_list_text_view.visibility = View.VISIBLE
        shopping_list_edit_text.visibility = View.GONE

        edit_save_button.text = "Edit"
        cancel_button.visibility = View.GONE

        browseFoodModel?.food?.let { foodDetail ->

            food_name_text_view.text = foodDetail.name
            food_description_text_view.text = foodDetail.description

            vegetable.isChecked = foodDetail.vegitable ?: false
            soup.isChecked = foodDetail.soup ?: false
            fish.isChecked = foodDetail.fish ?: false
            seafood.isChecked = foodDetail.seafood ?: false
            bean.isChecked = foodDetail.bean ?: false

            ingredients_text_view.text = foodDetail.ingredient?.joinToString("\n")
            shopping_list_text_view.text = foodDetail.shoppingList?.joinToString("\n")
        }

    }

}
