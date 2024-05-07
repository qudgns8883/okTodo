package com.example.oktodo.metro.data

data class BusInfo(
    val bstopid: String, // 정류소 ID
    val nodenm: String, // 정류소명
    val min1: String, // 남은 도착시간 1 (앞차, 분)
    val min2: String, // 남은 도착시간 2 (뒷차, 분)
    val station1: String, // 남은 정류소 수 (min1 의 남은 정류소 수)
    val station2: String, // 남은 정류소 수 (min2 의 남은 정류소 수)
    val lineno: String // 버스번호
)
