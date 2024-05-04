package com.example.oktodo.metro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.oktodo.R
import com.example.oktodo.metro.data.BusInfo

class BusInfoAdapter(private var busInfoList: List<BusInfo>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.businfo_recycler_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.businfo_recycler_item, parent, false)
            BusInfoViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is BusInfoViewHolder -> {
                if (busInfoList.isNotEmpty()) {
                    val busInfo = busInfoList[position - 1]
                    holder.bind(busInfo)
                }
            }
            is HeaderViewHolder -> {
                if (busInfoList.isNotEmpty()) {
                    val busInfo = busInfoList[0]
                    holder.bind(busInfo)
                }
            }
        }
    }

    override fun getItemCount() = busInfoList.size + 1 // 헤더를 고려해서 +1


    fun updateDate(newData: List<BusInfo>) {
        this.busInfoList = newData
        notifyDataSetChanged()
    }

    class BusInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(busInfo: BusInfo) {
            itemView.findViewById<TextView>(R.id.lineno).text = "${busInfo.lineno}번 버스"
//            itemView.findViewById<TextView>(R.id.min1).text = "${busInfo.min1}분 후 도착"
//            itemView.findViewById<TextView>(R.id.min2).text = "${busInfo.min2}분 후 도착"
//            itemView.findViewById<TextView>(R.id.station1).text = "${busInfo.station1}정거장 전"
//            itemView.findViewById<TextView>(R.id.station2).text = "${busInfo.station2}정거장 전"
            itemView.findViewById<TextView>(R.id.busInfo1).text = "${busInfo.min1}분 후 도착 ${busInfo.station1}정거장 전"
            itemView.findViewById<TextView>(R.id.busInfo2).text = "${busInfo.min2}분 후 도착 ${busInfo.station2}정거장 전"

        }
    }
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(busInfo: BusInfo) {
            itemView.findViewById<TextView>(R.id.nodenm).text = "현재 정류장\n${busInfo.nodenm}"
        }
        // 헤더에 대한 뷰와 데이터 바인딩 로직을 여기에 구현합니다.
    }
}