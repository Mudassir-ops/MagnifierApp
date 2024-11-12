package com.example.magnifierapp.fragment.displayImageFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.magnifierapp.fragment.homeFragment.FilterDataModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DisplayViewModel @Inject constructor(
    private val displayRepository: DisplayRepository
) : ViewModel() {
    val filterList = MutableLiveData<List<FilterDataModel>?>()

    init {
        filterList.value = displayRepository.getFilterList()
    }

    fun getFilterList(): List<FilterDataModel> {
        return displayRepository.getFilterList()
    }
}