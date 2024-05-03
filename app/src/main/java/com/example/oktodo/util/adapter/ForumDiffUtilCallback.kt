package com.example.oktodo.util.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.oktodo.db.Forum

class ForumDiffUtilCallback : DiffUtil.ItemCallback<Forum>() {

    // 아이템이 같음을 판단하는 규칙
    override fun areItemsTheSame(oldItem: Forum, newItem: Forum): Boolean {
        return oldItem.cno == newItem.cno
    }

    // 내용을 비교하는 규칙
    override fun areContentsTheSame(oldItem: Forum, newItem: Forum): Boolean {
//        return oldItem.cno == newItem.cno
        return oldItem.cno == newItem.cno // Data class의 경우, 모든 필드 비교를 자동으로 수행
    }
}