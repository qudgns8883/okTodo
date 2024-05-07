package com.example.oktodo.metro.data

data class TrainSchedule(
    val stationName: String, // 역이름
    val destination: String, // 목적지 (방향)
    val arrivalTime: String, // 도착예정시간
    val lineName: String // 호선
)
