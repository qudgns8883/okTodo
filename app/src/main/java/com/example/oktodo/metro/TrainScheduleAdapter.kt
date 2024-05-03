package com.example.oktodo.metro

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.oktodo.R
import com.example.oktodo.metro.data.TrainSchedule


// 리사이클러 뷰 어댑터
class   TrainScheduleAdapter(private var trainScheduleList: List<TrainSchedule>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // TrainSchedule = API에서 가져온 데이터를 저장하는 데이터 클래스
    companion object {
        // 리사이클러뷰 헤더,아이템 을 구분하기위한 임의의 상수
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1

        // 서울 지하철 API에서는 호선을 특정번호로 구분하기 때문에 API에서 받아온 특정번호를
        // 거기에 맞는 호선이름으로 바꿔주는 함수 (lineNumToName)
        private fun lineNumToName(lineNum: String): String {
            return when (lineNum) {
                "1001" -> "1호선"
                "1002" -> "2호선"
                "1003" -> "3호선"
                "1004" -> "4호선"
                "1005" -> "5호선"
                "1006" -> "6호선"
                "1007" -> "7호선"
                "1008" -> "8호선"
                "1009" -> "9호선"
                "1061" -> "중앙선"
                "1063" -> "경의중앙선"
                "1065" -> "공항철도"
                "1067" -> "경춘선"
                "1075" -> "수의분당선"
                "1077" -> "신분당선"
                "1092" -> "우이신설선"
                "1093" -> "서해선"
                "1081" -> "경강선"
                else -> "알 수 없는 호선"
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.train_recycler_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.train_recycler_item, parent, false)
            TrainScheduleViewHolder(view)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TrainScheduleViewHolder -> {
                if (trainScheduleList.isNotEmpty()) {
                    val schedule = trainScheduleList[position - 1] // 헤더를 고려해서 -1
                    holder.bind(schedule)
                }
            }
            is HeaderViewHolder -> {
                if (trainScheduleList.isNotEmpty()) {
                    val schedule = trainScheduleList[0] // 첫 번째 데이터를 헤더로 사용
                    holder.bind(schedule)
                }
            }
        }
    }

    override fun getItemCount() = trainScheduleList.size + 1

    fun updateData(newData: List<TrainSchedule>) {
        Log.d("TrainScheduleAdapter", "Updating data with ${newData.size} items.")
        this.trainScheduleList = newData
        notifyDataSetChanged() // 어댑터에 데이터가 변경되었음을 알림
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stationTextView: TextView = itemView.findViewById(R.id.sub_station)
        private val lineNameTextView: TextView = itemView.findViewById(R.id.line_station)

        fun bind(schedule: TrainSchedule) {
            lineNameTextView.text = lineNumToName(schedule.lineName)
            stationTextView.text = "${schedule.stationName}역"
        }
    }

    class TrainScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val arrivalTimeTextView: TextView = itemView.findViewById(R.id.textViewArrivalTime)
        private val destinationTextView: TextView = itemView.findViewById(R.id.textViewDestination)

        fun bind(schedule: TrainSchedule) {
            destinationTextView.text = "${schedule.destination}행"
            arrivalTimeTextView.text = "${schedule.arrivalTime}분 뒤 도착"
        }
    }
}
