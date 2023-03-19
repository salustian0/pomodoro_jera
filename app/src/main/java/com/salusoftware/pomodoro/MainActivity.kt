package com.salusoftware.pomodoro
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.salusoftware.pomodoro.databinding.ActivityMainBinding
import com.salusoftware.pomodoro.databinding.DialogConfigBinding
import com.salusoftware.pomodoro.models.Config
import java.util.Calendar

class MainActivity() : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var config: Config
    lateinit var timer: CountDownTimer
    lateinit var mp: MediaPlayer
    var timerLeft: Long = 0
    var currentFlow: String = "work"
    var state : String =  "not_started"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root;
        setContentView(view)

        /**
         * Inicializando objeto e setando valores padrão
         */
        this.config = Config(
            workTime = 25,
            restTime = 5
        )

        setActions()

        this.binding.btnReset.visibility = View.GONE

        this.setTimeText(this.config.workTime)
    }


    private fun setActions(){
        this.binding.btnPlay.setOnClickListener{
            this.state  = when(this.state){
                "not_started" -> {
                    this.binding.btnConfig.visibility = View.GONE
                    this.binding.labelBtnConfig.visibility = View.GONE
                    this.binding.switchMode.visibility = View.GONE
                    this.binding.btnReset.visibility = View.VISIBLE


                    val time = when(this.currentFlow){"work" -> this.config.workTime else -> this.config.restTime}
                    this.timer = this.createTimer(time)


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
                else -> "not_started"
            }
        }

        this.binding.btnReset.setOnClickListener{
            this.timer.cancel()
            this.timerLeft = this.config.workTime

            this.binding.btnPlay.setImageResource(R.drawable.ic_btn_play)
            this.state = "not_started"

            this.binding.btnConfig.visibility = View.VISIBLE
            this.binding.labelBtnConfig.visibility = View.VISIBLE
            this.binding.switchMode.visibility = View.VISIBLE
            this.binding.btnReset.visibility = View.GONE


            val time = when(this.currentFlow){"work" -> this.config.workTime else -> this.config.restTime}
            this.setTimeText(time)
        }

        this.binding.btnConfig.setOnClickListener{
            this.alert()
        }

        this.binding.switchMode.setOnCheckedChangeListener { compoundButton, isChecked: Boolean ->
            if(isChecked){
                this.currentFlow = "rest"
                this.binding.labelFlow.text = getString(R.string.tempo_de_descanso)
                this.setTimeText(this.config.restTime)

            }else{
                this.currentFlow = "work"
                this.binding.labelFlow.text = getString(R.string.tempo_de_trabalho)
                this.setTimeText(this.config.workTime)
            }
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
                context.state = "not_started"

                if(currentFlow == "work"){
                    currentFlow = "rest"
                    context.setTimeText(context.config.restTime)
                    context.binding.labelFlow.text = getString(R.string.tempo_de_descanso)
                }else{
                    currentFlow = "work"
                    context.setTimeText(context.config.workTime)
                    context.binding.labelFlow.text = getString(R.string.tempo_de_trabalho)
                }

                context.binding.btnPlay.setImageResource(R.drawable.ic_btn_play)


                val message = if (context.currentFlow == "work")   "Tempo de descanso finalizado!" else  "Tempo de trabalho finalizado!"

                AlertDialog.Builder(context)
                    .setTitle(R.string.btn_config_text)
                    .setTitle("Tempo finalizado")
                    .setMessage(message)
                    .setPositiveButton("Ok") { _, _ ->
                      context.stopSound()
                    }.setOnDismissListener {
                        context.stopSound()
                    }.show()

               context.playSound()
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

    private fun alert(){
        val bindingDialogConfig = DialogConfigBinding.inflate(layoutInflater)
            bindingDialogConfig.txtWorkTime.setText(this.config.getWorkTimeAsSeconds().toString())
            bindingDialogConfig.txtRestTime.setText(this.config.getRestTimeAsSeconds().toString())

        AlertDialog.Builder(this)
            .setTitle(R.string.btn_config_text)
            .setView(bindingDialogConfig.root)
            .setPositiveButton("Salvar") { _, _ ->
                var workTime = bindingDialogConfig.txtWorkTime.text.toString()
                var restTime = bindingDialogConfig.txtRestTime.text.toString()

                if(workTime.isBlank()){
                    workTime = "0"
                }

                if(restTime.isBlank()){
                    restTime = "0"
                }

                this.config.workTime = workTime.toLong()
                this.config.restTime = restTime.toLong()


                val time = when(this.currentFlow){"work" -> this.config.workTime else -> this.config.restTime}
                this.setTimeText(time)

        }.show()
    }

    private fun playSound(){
        if(!::mp.isInitialized){
            mp = MediaPlayer.create(this@MainActivity, R.raw.alarm)
            mp!!.isLooping = true
            mp!!.start()
        }else mp!!.start()
    }

    private fun stopSound(){
        if(::mp.isInitialized)
            mp!!.stop()
    }

}