package com.falcon.kitchenbuddy

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import com.falcon.kitchenbuddy.helper.ActivityCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ActivityCallback {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSupportNavigateUp() = findNavController(this, R.id.nav_graph_kitchenbuddy).navigateUp()


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        when (item.itemId) {
            R.id.nav_home -> {
                Navigation.findNavController(this, R.id.my_nav_host_fragment).
                        navigate(R.id.mainFragment, null, NavOptions.Builder().setPopUpTo(R.id.mainFragment, true).build())
            }
            R.id.nav_browse -> {
                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.browseFragment)
            }
            R.id.nav_shopping_list -> {
                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.shoppingListFragment)
            }
            R.id.nav_history -> {
                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.historyFragment)
            }
            R.id.nav_settings -> {
                Navigation.findNavController(this, R.id.my_nav_host_fragment).navigate(R.id.settingsFragment)
            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun updateMenuIndex(id: Int) {
        nav_view.setCheckedItem(id)
    }

}
