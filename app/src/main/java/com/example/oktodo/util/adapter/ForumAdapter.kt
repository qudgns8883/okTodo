package com.example.oktodo.util.adapter

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.oktodo.databinding.ForumItemBinding
import com.example.oktodo.db.Forum

class ForumAdapter(
    private val recyclerView: RecyclerView,
    private val onClick: (Forum) -> Unit
) :
    ListAdapter<Forum, ForumAdapter.ForumViewHolder>(ForumDiffUtilCallback()) {
    private lateinit var binding: ForumItemBinding

    // 항목 갯수
    override fun getItemCount(): Int {
        val itemCount = currentList.size
        return itemCount
    }

    // 항목 구성에 필요한 뷰 홀더 객체 준비
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumViewHolder {
        binding = ForumItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForumViewHolder(binding, onClick)
    }

    // 뷰에 데이터 출력
    override fun onBindViewHolder(holder: ForumViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.setOnClickListener(getItem(position))
    }

    @SuppressLint("NotifyDataSetChanged")
    // 데이터 리스트 업데이트
    fun loadData(newData: List<Forum>) {
        submitList(newData) // submitList()를 사용하여 데이터 리스트 업데이트
    }

    class ForumViewHolder(
        val binding: ForumItemBinding,
        private val onClick: (Forum) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(forum: Forum) {
            binding.itemData.text = forum.forumContent
            binding.forumTime.text = DateFormat.format("HH:mm", forum.forumTime)
        }

        fun setOnClickListener(forum: Forum) {
            binding.root.setOnClickListener {
                onClick(forum)
            }
        }
    }
}