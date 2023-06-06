/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.triggersensor.presentation

import android.annotation.SuppressLint
import android.hardware.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.triggersensor.presentation.theme.TriggerSensorTheme
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(){
    private lateinit var sensorManager: SensorManager
    private lateinit var triggerSensor: Sensor
    private lateinit var listener: TriggerListener
    private lateinit var accSensor: Sensor
    private var accListener: AccListener = AccListener()
    private var viewModel = MainViewModel()
    private lateinit var logFileStream: FileOutputStream
    private lateinit var accFileStream: FileOutputStream
    private var triggerTimer: Timer = Timer()
    private var triggerTimerTask: TriggerTimerTask = TriggerTimerTask()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(viewModel.x)
        }

        val dir = SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.ENGLISH).format(Date())
        File(this.filesDir, dir).mkdir()

        logFileStream = FileOutputStream(File(this.filesDir, "$dir/Log.txt"))
        accFileStream = FileOutputStream(File(this.filesDir, "$dir/data.txt"))
        accFileStream.write("timestamp,x,y,z,real_time_ms\n".toByteArray())
        logFileStream.write("real_time_ms,event\n".toByteArray())
        logFileStream.write("${Calendar.getInstance().timeInMillis},OnCreate()\n".toByteArray())
        Log.d("TriggerSensorState", "onCreate")

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        for (sensor in sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            Log.d("TriggerSensorSensors",sensor.toString())
            Log.d("TriggerSensorSensors","Type Memory File:${sensor.isDirectChannelTypeSupported(SensorDirectChannel.TYPE_MEMORY_FILE)}")
            Log.d("TriggerSensorSensors","Type Hardware Buffer: ${sensor.isDirectChannelTypeSupported(SensorDirectChannel.TYPE_HARDWARE_BUFFER)}")
        }
        listener = TriggerListener()
        triggerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)
        sensorManager.requestTriggerSensor(listener, triggerSensor)



    }

    fun unregisterAcc() {
        sensorManager.unregisterListener(accListener)
        Log.d("TriggerSensorState", "Acc stop")
        logFileStream.write("${Calendar.getInstance().timeInMillis},Acc stop\n".toByteArray())
    }

    inner class TriggerListener : TriggerEventListener() {
        override fun onTrigger(event: TriggerEvent) {
            Log.d("TriggerSensorState", "Triggered")
            logFileStream.write("${Calendar.getInstance().timeInMillis},Triggered\n".toByteArray())

            // register
            accListener.register()

            // Set timer to reset trigger, stop accelerometer, and lock screen on
            var timerLengthMins: Long = 3
            triggerTimer.schedule(triggerTimerTask, (timerLengthMins * 60 * 1e3).toLong())
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    inner class AccListener : SensorEventListener {
        fun register() {
            logFileStream.write("${Calendar.getInstance().timeInMillis},Acc start\n".toByteArray())
            Log.d("TriggerSensorState", "Acc start")
            accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val samplingPeriodMicroseconds = 1000000/100
            sensorManager.registerListener(this@AccListener, accSensor, samplingPeriodMicroseconds)
        }

        override fun onSensorChanged(event: SensorEvent) {
            Log.d("TriggerSensorOnChange", "${event.values[0]}")
            viewModel.updateX(event.values[0])
            accFileStream.write("${event.timestamp},${event.values[0]},${event.values[1]},${event.values[2]},${Calendar.getInstance().timeInMillis}\n".toByteArray())
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    inner class TriggerTimerTask : TimerTask() {
        override fun run() {
            // unregister accelerometer, reset trigger, and let go of screen lock
            Log.d("TriggerSensorState", "Timer End")
            logFileStream.write("${Calendar.getInstance().timeInMillis},Timer End\n".toByteArray())
            unregisterAcc()
            sensorManager.requestTriggerSensor(listener, triggerSensor)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    override fun onStart() {
        super.onStart()
        logFileStream.write("${Calendar.getInstance().timeInMillis},OnStart()\n".toByteArray())
        Log.d("TriggerSensorState", "onStart")
    }
    override fun onResume() {
        super.onResume()
        Log.d("TriggerSensorState", "onResume")
        logFileStream.write("${Calendar.getInstance().timeInMillis},OnResume()\n".toByteArray())
    }
    override fun onPause() {
        super.onPause()
        logFileStream.write("${Calendar.getInstance().timeInMillis},onPause()\n".toByteArray())
        Log.d("TriggerSensorState", "onPause")
    }
    override fun onStop() {
        super.onStop()
        logFileStream.write("${Calendar.getInstance().timeInMillis},OnStop()\n".toByteArray())
        Log.d("TriggerSensorState", "onStop")
        viewModel.updateX(0F)
    }
    override fun onDestroy() {
        super.onDestroy()
        logFileStream.write("${Calendar.getInstance().timeInMillis},onDestroy()\n".toByteArray())
        Log.d("TriggerSensorState", "onDestroy")
        accFileStream.close()
        logFileStream.close()
    }
    override fun onRestart() {
        super.onRestart()
        logFileStream.write("${Calendar.getInstance().timeInMillis},onRestart()\n".toByteArray())
        Log.d("TriggerSensorState", "onRestart")
    }
}

class MainViewModel : ViewModel() {
    var x by mutableStateOf(0F)

    fun updateX(xValue: Float) {
        x = xValue
    }
}

@Composable
fun WearApp(x: Float) {
    TriggerSensorTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ) {
            Greeting(x = "$x")
        }
    }
}

@Composable
fun Greeting(x: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = "Trigger Sensor\nx: $x"
    )
}