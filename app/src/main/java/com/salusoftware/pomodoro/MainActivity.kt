package com.salusoftware.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import com.salusoftware.pomodoro.databinding.ActivityMainBinding
import com.salusoftware.pomodoro.models.Config
import java.util.Calendar

class MainActivity() : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var config: Config
    lateinit var timer: CountDownTimer
    var timerLeft: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root;
        setContentView(view)

        this.config = Config()
        this.timer = this.createTimer(this.config.workTime)

        setActions()
        this.setTimeText(this.config.workTime)
    }


    private fun setActions(){

        var state = "not_started"

        this.binding.btnPlay.setOnClickListener{

            state  = when(state){
                "not_started" -> {
                    this.timer.start()
                    this.binding.btnPlay.setImageResource(R.drawable.ic_btn_pause)
                    "started"
                }
                "started" -> {
                    this.timer.cancel()
                    this.binding.btnPlay.setImageResource(R.drawable.ic_btn_play)
                    "paused"
                }
                "paused" -> {
                    this.timer = createTimer(this.timerLeft)
                    this.timer.start()

                    this.binding.btnPlay.setImageResource(R.drawable.ic_btn_pause)
                    "started"
                }
                else -> ""
            }
        }

        this.binding.btnReset.setOnClickListener{
            this.timer.cancel()
            this.timerLeft = this.config.workTime

            this.binding.btnPlay.setImageResource(R.drawable.ic_btn_play)
            state = "not_started"

            this.setTimeText(this.config.workTime)
        }

        this.binding.btnConfig.setOnClickListener{

        }
    }

    private fun createTimer(time: Long): CountDownTimer {
        val context : MainActivity = this
        return object: CountDownTimer(time, 1){
            override fun onTick(timeInMillis: Long) {
                context.timerLeft = timeInMillis
                context.setTimeText(timeInMillis)
            }

            override fun onFinish() {
               context.setTimeText(context.config.workTime)
            }
        }
    }

    fun setTimeText(time: Long){
        val c = Calendar.getInstance()
        c.timeInMillis = time
        val seconds = c.get(Calendar.SECOND).toString().padStart(2,'0')
        val minutes = c.get(Calendar.MINUTE).toString().padStart(2, '0')

        val display  = "${minutes}:${seconds}"

        this.binding.txtTime.text = display
    }


}