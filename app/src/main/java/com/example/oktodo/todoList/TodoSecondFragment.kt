package com.example.oktodo.todoList

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.oktodo.MainActivity
import com.example.oktodo.R
import com.example.oktodo.databinding.TodoFragmentSecondBinding
import java.time.LocalTime
import java.util.Calendar

class TodoSecondFragment : Fragment() {
    private var _binding: TodoFragmentSecondBinding? = null
    private val binding get() = _binding!!

    // mno 가져오기
    private val prefs by lazy {
        requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    }
    private val isLoggedIn by lazy {
        prefs.getBoolean("IsLoggedIn", false)
    }
    private val mno by lazy {
        if (isLoggedIn) prefs.getString("mno", "").toString() else "default_value"
    }

    private val viewModel by activityViewModels<TodoMainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = TodoFragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // todo_time 버튼을 찾음
        val todoTimeButton = binding.todoTime

        // todo_time 버튼에 클릭 리스너 설정
        todoTimeButton.setOnClickListener {
            showTimePickerDialog()
        }

        // 홈 이미지 클릭 리스너
        binding.homeIcon.setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
        }

        viewModel.selectedTodo?.let { todo ->
            binding.todoEditText.setText(todo.todoContent)

            // todoTime 값을 버튼에 설정
            val formattedTime = String.format(
                "%02d:%02d",
                todo.todoTime / (60 * 60 * 1000),
                (todo.todoTime % (60 * 60 * 1000)) / (60 * 1000)
            )
            binding.todoTime.text = formattedTime

            when (todo.importance) {
                "1" -> binding.btnHigh.isChecked = true
                "2" -> binding.btnMiddle.isChecked = true
                "3" -> binding.btnLow.isChecked = true
            }

            val daysOfWeek = listOf(
                binding.checkboxMonday,
                binding.checkboxTuesday,
                binding.checkboxWednesday,
                binding.checkboxThursday,
                binding.checkboxFriday,
                binding.checkboxSaturday,
                binding.checkboxSunday
            )
            val selectedDays = todo.day.split(", ")
            for (day in selectedDays) {
                when (day) {
                    "mon" -> daysOfWeek[0].isChecked = true
                    "tue" -> daysOfWeek[1].isChecked = true
                    "wed" -> daysOfWeek[2].isChecked = true
                    "thu" -> daysOfWeek[3].isChecked = true
                    "fri" -> daysOfWeek[4].isChecked = true
                    "sat" -> daysOfWeek[5].isChecked = true
                    "sun" -> daysOfWeek[6].isChecked = true
                }
            }
        }

        val calendar = Calendar.getInstance()

        fun convertBooleanArrayToString(checkedArray: BooleanArray): String {
            val daysOfWeek = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun")
            val result = StringBuilder()

            for (i in checkedArray.indices) {
                if (checkedArray[i]) {
                    if (result.isNotEmpty()) {
                        result.append(", ")
                    }
                    result.append(daysOfWeek[i])
                }
            }
            return result.toString()
        }

        binding.doneFab.setOnClickListener {
            try {
                val inputData = binding.todoEditText.text.toString()
                // 값이 유효한지 확인
                if (inputData.isBlank()) {
                    // 유효하지 않은 경우 Toast 메시지를 띄움
                    Toast.makeText(requireContext(), "내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (inputData.isNotEmpty()) {
                    val checkedArray = booleanArrayOf(
                        binding.checkboxMonday.isChecked,
                        binding.checkboxTuesday.isChecked,
                        binding.checkboxWednesday.isChecked,
                        binding.checkboxThursday.isChecked,
                        binding.checkboxFriday.isChecked,
                        binding.checkboxSaturday.isChecked,
                        binding.checkboxSunday.isChecked,
                    )

                    val dayCheckedString = convertBooleanArrayToString(checkedArray)

                    val checkedRadioButtonId = binding.todoImportanceRadioGroup.checkedRadioButtonId
                    val todoImportance = when (checkedRadioButtonId) {
                        R.id.btn_high -> "1"
                        R.id.btn_middle -> "2"
                        R.id.btn_low -> "3"
                        else -> ""
                    }

                    val selectedTimeText = binding.todoTime.text.toString()
                    val selectedTimeParts = selectedTimeText.split(":")
                    val selectedHour = selectedTimeParts[0].toInt()
                    val selectedMinute = selectedTimeParts[1].toInt()

                    // 선택된 시간을 밀리초로 변환
                    val selectedTimeInMillis =
                        ((selectedHour.toLong() * 60 * 60 * 1000) + (selectedMinute.toLong() * 60 * 1000))

                    if (viewModel.selectedTodo != null) {
                        viewModel.updateTodo(
                            inputData,
                            calendar.timeInMillis,
                            day = dayCheckedString,
                            importance = todoImportance,
                            todoTime = selectedTimeInMillis,
                        )
                    } else {
                        if (mno != null) {
                            viewModel.addTodo(
                                mno = mno,
                                inputData,
                                day = dayCheckedString,
                                checked = false,
                                importance = todoImportance,
                                date = calendar.timeInMillis,
                                todoTime = selectedTimeInMillis,
                                todoRegTime = LocalTime.now(),
                            )
                        } else {
                            Toast.makeText(requireContext(), "잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                // 예외가 발생한 경우에 대한 처리
                e.printStackTrace() // 예외 로그 출력
                Toast.makeText(requireContext(), "입력되지 않은 값이 있습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.resetBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.deleteFab.setOnClickListener {
            viewModel.deleteTodo(viewModel.selectedTodo!!.tno)
            findNavController().popBackStack()
        }

        // 선택된 할 일이 없을 때는 지우기 버튼 감추기
        if (viewModel.selectedTodo == null) {
            binding.deleteFab.visibility = View.GONE
        }

        // 뒤로 가기 버튼을 누르면 선택된 할 일 초기화
        findNavController().addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id != R.id.SecondFragment) {
                viewModel.selectedTodo = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()

        // 현재 시간 가져오기
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // todoTime이 있는지 확인하여 있으면 해당 시간으로 타임 피커 다이얼로그에 표시
        viewModel.selectedTodo?.let { todo ->
            // todoTime 값에서 시와 분 추출
            val selectedHour = (todo.todoTime / (60 * 60 * 1000)).toInt()
            val selectedMinute = ((todo.todoTime % (60 * 60 * 1000)) / (60 * 1000)).toInt()

            // 타임 피커 다이얼로그 생성 및 설정
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    binding.todoTime.text = formattedTime
                },
                selectedHour, // 저장된 todoTime의 시
                selectedMinute, // 저장된 todoTime의 분
                false
            )
            timePickerDialog.show()
        } ?: run {
            // 저장된 todoTime이 없으면 현재 시간으로 타임 피커 다이얼로그에 표시
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                TimePickerDialog.OnTimeSetListener { _, selectedHour, selectedMinute ->
                    val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    binding.todoTime.text = formattedTime
                },
                hour,
                minute,
                false
            )
            timePickerDialog.show()
        }
    }
}