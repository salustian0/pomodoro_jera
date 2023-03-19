package com.salusoftware.pomodoro.models

class Config(
    workTime : Long = 0,
    restTime : Long = 0
){

    var workTime: Long = 0
        set(value) { field = value * 1000 * 60 }

    var restTime: Long = 0
        set(value) { field = value  * 1000 * 60 }

    init {
        this.workTime =  workTime
        this.restTime = restTime
    }


    fun getWorkTimeAsSeconds() : Long{
        return this.workTime / 60 / 1000
    }
    fun getRestTimeAsSeconds() : Long{
        return this.restTime / 60 / 1000
    }
}
