package com.example.magnifierapp.fragment.homeFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {
    val filterList = MutableLiveData<List<FilterDataModel>?>()

    init {
        filterList.value = homeRepository.getFilterList()
    }

    fun getFilterList(): List<FilterDataModel> {
        return homeRepository.getFilterList()
    }
}