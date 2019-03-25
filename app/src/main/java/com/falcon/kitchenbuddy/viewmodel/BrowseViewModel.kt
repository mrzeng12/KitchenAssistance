package com.falcon.kitchenbuddy.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel;
import com.falcon.kitchenbuddy.model.BrowseFoodModel

class BrowseViewModel : ViewModel() {
    var selectedFood = MutableLiveData<BrowseFoodModel>()
    var isEditing = MutableLiveData<Boolean>()
    var editMode = MutableLiveData<EditMode>()
    fun storeSelectedFood (browseFoodModel: BrowseFoodModel){
        selectedFood.value = browseFoodModel
    }
    fun setEditingStatus(status: Boolean){
        isEditing.value = status
    }
    fun setMode(mode: EditMode){
        editMode.value = mode
    }

    enum class EditMode{
        ADD, UPDATE
    }
}
