package com.example.oktodo.metro.data

data class TrainSchedule(
    val stationName: String,
    val destination: String,
    val arrivalTime: String,
    val lineName: String
)
