package com.example.magnifierapp.fragment.displayImageFragment

import etech.magnifierplus.R
import com.example.magnifierapp.fragment.homeFragment.FilterDataModel
import javax.inject.Inject

class DisplayRepository @Inject constructor() {
    fun getFilterList() = listOf(
        FilterDataModel(R.drawable.filter_1),
        FilterDataModel(R.drawable.filter_2),
        FilterDataModel(R.drawable.filter_3),
        FilterDataModel(R.drawable.filter_4),
        FilterDataModel(R.drawable.filter_1),
        FilterDataModel(R.drawable.filter_2),
        FilterDataModel(R.drawable.filter_3),
        FilterDataModel(R.drawable.filter_4),
    )
}