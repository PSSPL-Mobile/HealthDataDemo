package com.psspl.healthdatademo.wear.presentation.data.model

data class HeartRateData(
    var bpm: Int,
    var timestamp: Long,
    var isAlert: Boolean = false,
    var alertMsg: String
)