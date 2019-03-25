package com.falcon.kitchenbuddy.ui

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.falcon.kitchenbuddy.MainActivity
import com.falcon.kitchenbuddy.R
import com.falcon.kitchenbuddy.adapter.HistoryAdapter
import com.falcon.kitchenbuddy.adapter.HistoryClicked
import com.falcon.kitchenbuddy.helper.ActivityCallback
import com.falcon.kitchenbuddy.model.MenuModel
import com.falcon.kitchenbuddy.viewmodel.HistoryViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.history_fragment.*

class HistoryFragment : Fragment(), HistoryClicked {

    private lateinit var viewModel: HistoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.history_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (activity is ActivityCallback) {
            (activity as ActivityCallback).updateMenuIndex(R.id.nav_history)
        }
        (activity as MainActivity).supportActionBar?.title = "Plan"
        activity?.let { viewModel = ViewModelProviders.of(it).get(HistoryViewModel::class.java) }


        val menuList: ArrayList<String> = ArrayList()
        activity?.let { act ->
            val pref = act.getSharedPreferences(getString(R.string.sp_name), 0)
            val foodListString = pref.getString(getString(R.string.sp_key_selectedFoodList), null)
            if (foodListString != null) {
                val foodList: ArrayList<MenuModel> = Gson().fromJson(foodListString, object : TypeToken<ArrayList<MenuModel>>() {}.type)
                for (i in foodList.indices.reversed()) {
                    menuList.add(relativeTime(foodList[i].timestamp))
                }
            }
        }
        activity?.let { act ->
            gridview.layoutManager = GridLayoutManager(act, 2)
//            gridview.layoutManager = LinearLayoutManager(activity)
            gridview.adapter = HistoryAdapter(act, menuList, this)
        }

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

    override fun onHistoryClicked(index: Int) {
        activity?.let { act ->
            val pref = act.getSharedPreferences(getString(R.string.sp_name), 0)
            val foodListString = pref.getString(getString(R.string.sp_key_selectedFoodList), null)
            if (foodListString != null) {
                val foodList: ArrayList<MenuModel> = Gson().fromJson(foodListString, object : TypeToken<ArrayList<MenuModel>>() {}.type)
                viewModel.storeSelectedFoodList(foodList.reversed()[index])
                viewModel.storeselectedFoodListIndex(index)
            }
            Navigation.findNavController(act, R.id.my_nav_host_fragment).navigate(R.id.historyMealFragment)
        }
    }

}
