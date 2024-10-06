package com.example.magnifierapp.fragment.homeFragment

import com.example.magnifierapp.R
import javax.inject.Inject

class HomeRepository @Inject constructor() {

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