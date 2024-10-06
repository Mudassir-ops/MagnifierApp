package com.example.magnifierapp.fragment.homeFragment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.magnifierapp.databinding.FilterItemBinding

class FilterAdapter(private var filterList: List<FilterDataModel>,val onItemClick: (Pair<FilterDataModel, Int>) -> Unit) :
    RecyclerView.Adapter<FilterAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FilterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = filterList[position]
        holder.binding.imgFilter.setImageResource(dataModel.filterImage)
        holder.itemView.setOnClickListener {
            onItemClick(Pair(dataModel, position))
        }
    }

    class ViewHolder(val binding: FilterItemBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateFilterList(newLanguageList: List<FilterDataModel>) {
        filterList = newLanguageList
        notifyDataSetChanged()
    }
}