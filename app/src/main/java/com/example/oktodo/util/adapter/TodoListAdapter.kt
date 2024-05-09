package com.example.oktodo.util.adapter

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.oktodo.R
import com.example.oktodo.databinding.TodoItemBinding
import com.example.oktodo.db.Todo
import java.util.Calendar

class TodoListAdapter(
    private val recyclerView: RecyclerView, // RecyclerView 인스턴스를 저장하기 위한 변수 추가
    private val onClick: (Todo) -> Unit,
    private val updateDoneChecked: (Todo, Boolean) -> Unit
) : ListAdapter<Todo, TodoListAdapter.TodoViewHolder>(TodoDiffUtilCallback()) {
    private lateinit var binding: TodoItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        binding =
            TodoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoViewHolder(binding, onClick, updateDoneChecked)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(getItem(position))
        holder.setOnClickListener(getItem(position))
        holder.binding.doneCheck.isChecked = currentItem.checked
    }

    class TodoViewHolder(
        val binding: TodoItemBinding,
        private val onClick: (Todo) -> Unit,
        private val updateDoneChecked: (Todo, Boolean) -> Unit // 추가: Todo의 체크 상태를 업데이트하는 함수
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: Todo) {
            val newImageResourceId = when (todo.importance) {
                "1" -> R.drawable.vvvip
                "2" -> R.drawable.vvip
                "3" -> R.drawable.vip
                else -> R.drawable.add // 다른 경우에는 기본 이미지를 사용하거나 원하는 대체 이미지 리소스 ID를 지정할 수 있습니다.
            }

            val selectedDays = todo.day.split(", ")
            val weekday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            val dayOfWeek = when (weekday) {
                Calendar.SUNDAY -> "sun"
                Calendar.MONDAY -> "mon"
                Calendar.TUESDAY -> "tue"
                Calendar.WEDNESDAY -> "wed"
                Calendar.THURSDAY -> "thu"
                Calendar.FRIDAY -> "fri"
                Calendar.SATURDAY -> "sat"
                else -> ""
            }

            // doneCheck 기본적으로 비활성화
            binding.doneCheck.isEnabled = false
            // 선택한 요일이 있는 경우에만 해당 요일에 맞게 활성화
            // 현재 요일이 선택된 요일 중 하나인지 확인
            if (dayOfWeek in selectedDays) {
                binding.doneCheck.isEnabled = true
            }

            binding.text1.text = todo.todoContent
            binding.text2.text = DateFormat.format("HH:mm", todo.todoTime)
            binding.doneCheck.isChecked = todo.checked
            binding.todoImportantView.setImageResource(newImageResourceId)

            // 체크박스의 상태가 변경될 때마다 해당 Todo의 체크 상태를 업데이트하는 기능 추가
            binding.doneCheck.setOnCheckedChangeListener { _, isChecked ->
                updateDoneChecked(todo, isChecked) // 상태 변경 시 해당 함수를 호출하여 Todo 상태를 업데이트합니다
            }
        }

        fun setOnClickListener(todo: Todo) {
            binding.root.setOnClickListener {
                onClick(todo)
            }
        }
    }

    // 체크박스를 재설정하는 메서드 추가
    @SuppressLint("NotifyDataSetChanged")
    fun unCheckboxes() {
        // 현재 표시되고 있는 아이템 수만큼 반복
        for (i in 0 until itemCount) {
            // 해당 위치의 ViewHolder 가져오기
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(i)
            if (viewHolder is TodoViewHolder) {
                // ViewHolder 내의 binding을 사용하여 체크박스 비활성화
                viewHolder.binding.doneCheck.isChecked = false
            }
        }
        notifyDataSetChanged() // RecyclerView(현재 표시되고 있는 데이터를 다시 로드하고 갱신하여 화면에 새로운 데이터를 반영)에 연결된 어댑터가 데이터의 변경 사항을 알려주는 역할
    }
}