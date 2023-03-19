package com.salusoftware.pomodoro.models

data class Config(
    var workTime: Long = 25 * 1000 * 60,
    var restTime: Long = 5 * 1000 * 60
)
