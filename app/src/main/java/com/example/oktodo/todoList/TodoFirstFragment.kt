package com.example.oktodo.todoList

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.oktodo.R
import com.example.oktodo.databinding.TodoFragmentFirstBinding
import com.example.oktodo.databinding.TodoItemBinding
import com.example.oktodo.db.Todo
import com.example.oktodo.db.Todo2
import com.example.oktodo.util.adapter.TodoListAdapter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.observeOn
import kotlinx.coroutines.launch
import java.util.Calendar

class TodoFirstFragment : Fragment() {
    private lateinit var adapter: TodoListAdapter
    private val viewModel by activityViewModels<TodoMainViewModel>()    // ①
    private var _binding: TodoFragmentFirstBinding? = null
    private var _binding2: TodoItemBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val binding2 get() = _binding2!!

    // 이모티콘 관련
    val calendar = Calendar.getInstance()
    val todayInMillis = calendar.timeInMillis
    val startDayOfWeek = calendar.apply {
        set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek) // 주의 첫 번째 요일
        add(Calendar.DAY_OF_MONTH, -6)
    }.timeInMillis
    val endDayOfWeek = todayInMillis

    private fun initRecyclerView(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            this.adapter = adapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TodoFragmentFirstBinding.inflate(inflater, container, false)
        _binding2 = TodoItemBinding.inflate(inflater)

        return binding.root
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView 초기화 및 설정
        val recyclerView = binding.recyclerView

        // Adapter 초기화 시 TodoDiffCallback를 사용하여 DiffUtil 계산
        adapter = TodoListAdapter(
            recyclerView, // RecyclerView 인스턴스를 전달
            onClick = { todo ->
                viewModel.selectedTodo = todo
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            },
            updateDoneChecked = { todo, isChecked ->
                viewModel.updateDoneChecked(todo, isChecked)
            }
        )

        initRecyclerView(recyclerView, adapter)

        // ViewModel에서 데이터를 수집하여 RecyclerView에 반영
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collect { newList ->
                    adapter.submitList(newList)
                    // 데이터가 업데이트되었음을 알고나서 작업 예약
                    scheduleWorkIfNeeded(newList)
                }
            }
        }

        // ViewModel에서 해당 주의Todo 데이터를 가져와서 설정
        viewModel.loadThisWeekTodos(startDayOfWeek, endDayOfWeek)

        // ViewModel에서 데이터를 수집하여 RecyclerView에 반영
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items2.collect { newList ->
                    // Todo데이터를 전달하여 이모티콘 설정
                    setEmotionImages(newList)
                }
            }
        }

        binding.addFab.setOnClickListener {
            viewModel.selectedTodo = null // 새 할 일을 추가할 때 선택된 할 일 초기화
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    // 백그라운드 작업 예약을 필요에 따라 실행
    private fun scheduleWorkIfNeeded(newList: List<Todo>) {
        if (newList.isNotEmpty()) {
            // 새 데이터가 있는 경우에만 작업 예약
            scheduleWork(requireContext())
        }
    }

    private fun scheduleWork(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<TodoWorker>()
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    // 각 요일에 해당하는 이모티콘을 설정하는 함수
    private fun setEmotionImages(todos: List<Todo2>) {
        val emotionImagesMap = mutableMapOf<Long, Int>()

        // 각 요일에 해당하는 calculate 컬럼 데이터를 가져와서 이모티콘 설정
        for (i in Calendar.SUNDAY..Calendar.SATURDAY) {
            // 해당 요일의 날짜를 구함
            val dayOfWeekDate = getDayOfWeekDate(i)

            // 해당 요일의 calculate 컬럼 데이터를 가져옴
            val calculateData = todos.find { todo ->
                todo.tno2 == dayOfWeekDate
            }?.calculate ?: -1

            // calculate 컬럼 데이터를 바탕으로 이모티콘 설정
            val emotionImageResourceId = when {
                calculateData in 67..100 -> R.drawable.icon_emotion3
                calculateData in 33 until 67 -> R.drawable.icon_emotion2
                calculateData in 0 until 33 -> R.drawable.icon_emotion1
                else -> R.drawable.custom_checkbox_selector
            }

            emotionImagesMap[i.toLong()] = emotionImageResourceId
        }

        // 당일에 대한 기본 이모티콘 설정
        val todayImageResourceId = R.drawable.bg_soso
        emotionImagesMap[Calendar.getInstance().get(Calendar.DAY_OF_WEEK).toLong()] = todayImageResourceId

        // 설정된 이모티콘을 UI에 적용
        binding.apply {
            emotionSun.setImageResource(emotionImagesMap.getOrDefault(Calendar.SUNDAY.toLong(), R.drawable.custom_checkbox_selector))
            emotionMon.setImageResource(emotionImagesMap.getOrDefault(Calendar.MONDAY.toLong(), R.drawable.custom_checkbox_selector))
            emotionTue.setImageResource(emotionImagesMap.getOrDefault(Calendar.TUESDAY.toLong(), R.drawable.custom_checkbox_selector))
            emotionWed.setImageResource(emotionImagesMap.getOrDefault(Calendar.WEDNESDAY.toLong(), R.drawable.custom_checkbox_selector))
            emotionThu.setImageResource(emotionImagesMap.getOrDefault(Calendar.THURSDAY.toLong(), R.drawable.custom_checkbox_selector))
            emotionFri.setImageResource(emotionImagesMap.getOrDefault(Calendar.FRIDAY.toLong(), R.drawable.custom_checkbox_selector))
            emotionSat.setImageResource(emotionImagesMap.getOrDefault(Calendar.SATURDAY.toLong(), R.drawable.custom_checkbox_selector))
        }
    }

    // 해당 요일의 날짜를 가져오는 함수
    private fun getDayOfWeekDate(dayOfWeek: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek) // 해당 요일 설정
            set(Calendar.HOUR_OF_DAY, 0) // 시간 설정 (자정)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _binding2 = null
    }
}