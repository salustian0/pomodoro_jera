package com.salusoftware.pomodoro
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.salusoftware.pomodoro.databinding.ActivityMainBinding
import com.salusoftware.pomodoro.databinding.DialogConfigBinding
import com.salusoftware.pomodoro.models.Config
import java.text.SimpleDateFormat
import java.util.*

class MainActivity() : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var config: Config
    lateinit var timer: CountDownTimer
    lateinit var mp: MediaPlayer
    var timerLeft: Long = 0
    var currentFlow: String = "work"
    var state : String =  "not_started"
    val notification_channel = "channel01"
    var countPomodoro = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root;
        setContentView(view)

        this.binding.labelQtdPomodoro.text = getString(R.string.texto_quantidade_pomodoros).replace("{qtd_pomodoros}", "0")

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


    /**
     * Adiciona as ações  aos botões
     */
    private fun setActions(){

        this.binding.btnPlay.setOnClickListener{
            this.state  = when(this.state){
                "not_started"
                -> {
                    this.binding.btnConfig.visibility = View.GONE
                    this.binding.labelBtnConfig.visibility = View.GONE
                    this.binding.switchMode.visibility = View.GONE
                    this.binding.btnReset.visibility = View.VISIBLE
                    this.binding.labelBtnPlay.text = getString(R.string.btn_pause_text)


                    val time = when(this.currentFlow){"work" -> this.config.workTime else -> this.config.restTime}
                    this.timer = this.createTimer(time)


                    this.timer.start()
                    this.binding.btnPlay.setImageResource(R.drawable.ic_btn_pause)
                    "started"
                }
                "started" -> {
                    this.timer.cancel()
                    this.binding.btnPlay.setImageResource(R.drawable.ic_btn_play)
                    this.binding.labelBtnPlay.text = getString(R.string.btn_play_text)

                    "paused"
                }
                "paused" -> {
                    this.timer = createTimer(this.timerLeft)
                    this.timer.start()

                    this.binding.btnPlay.setImageResource(R.drawable.ic_btn_pause)
                    this.binding.labelBtnPlay.text = getString(R.string.btn_pause_text)
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
            this.binding.labelBtnPlay.text = getString(R.string.btn_play_text)


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

    /**
     * Cria um novo countdown
     */
    private fun createTimer(time: Long): CountDownTimer {
        val context : MainActivity = this
        return object: CountDownTimer(time, 1){
            override fun onTick(timeInMillis: Long) {
                context.timerLeft = timeInMillis
                context.setTimeText(timeInMillis)
            }

            override fun onFinish() {
                context.state = "not_started"
                var message: String = "";

                context.currentFlow = when(context.currentFlow){
                    "work" -> {
                        context.setTimeText(context.config.restTime)
                        context.binding.labelFlow.text = getString(R.string.tempo_de_descanso)
                        message = "Tempo de trabalho finalizado!"

                        if(context.binding.switchMode.isChecked){
                            context.countPomodoro++

                            if(context.countPomodoro == 4){
                                context.config.restTime = 10
                            }

                            context.binding.labelQtdPomodoro.text = getString(R.string.texto_quantidade_pomodoros).replace("{qtd_pomodoros}", context.countPomodoro.toString())
                        }
                        "rest"
                    }
                    else -> {
                        context.setTimeText(context.config.workTime)
                        context.binding.labelFlow.text = getString(R.string.tempo_de_trabalho)
                        message = "Tempo de descanso finalizado!"

                        if(!context.binding.switchMode.isChecked){
                            context.countPomodoro++
                            if(context.countPomodoro == 4){
                                context.config.restTime = 10
                            }
                            context.binding.labelQtdPomodoro.text = getString(R.string.texto_quantidade_pomodoros).replace("{qtd_pomodoros}", context.countPomodoro.toString())
                        }

                        "work"
                    }
                }

                context.binding.btnPlay.setImageResource(R.drawable.ic_btn_play)
                context.binding.labelBtnPlay.text = getString(R.string.btn_play_text)

                AlertDialog.Builder(context)
                    .setTitle(R.string.btn_config_text)
                    .setTitle("Tempo finalizado")
                    .setMessage(message)
                    .setPositiveButton("Ok") { _, _ -> }
                    .setOnDismissListener {
                        context.stopSound()
                    }.show()

                context.notification("Tempo finalizado", message)
                context.playSound()
            }
        }
    }

    /**
     * Adiciona o texto na viewText de acordo com os milisegundos informados
     */
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
        this.mp = MediaPlayer.create(this@MainActivity, R.raw.alarm)
        this.mp!!.start()
    }

    private fun stopSound(){
        if(::mp.isInitialized){
            this.mp!!.stop()
            this.mp!!.release()
        }
    }

    /**
     * Envia notificação
     */
    private fun notification(title: String, message: String){
        this.createNotificationChannel()
        val date = Date()
        val notificationId = SimpleDateFormat("ddHHmmss", Locale.US).format(date).toInt()

        val builder = NotificationCompat.Builder(this, this.notification_channel)
            .setSmallIcon(R.drawable.ic_btn_play)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager  = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        notificationManager.notify(notificationId, builder.build())
    }

    fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                this.notification_channel,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            mChannel.description = "This is default channel used for all other notifications"

            val notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

}