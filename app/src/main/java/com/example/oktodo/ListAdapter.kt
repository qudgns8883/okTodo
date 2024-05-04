package com.example.oktodo

import com.example.oktodo.databinding.NoticeListBinding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ViewHolder(val binding: NoticeListBinding): RecyclerView.ViewHolder(binding.root)

// 리사이클러 뷰 어댑터
class ListAdapter(var itemList: MutableList<Notice>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.notice_list, parent, false)
//        return ViewHolder(view)
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(NoticeListBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ViewHolder).binding

        binding.listTitle.text = itemList[position].title
        binding.listContent.text = itemList[position].content
        binding.listRegTime.text = itemList[position].regTime.toString()
    }
}