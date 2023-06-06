class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        for (sensor in sensorManager.getSensorList(Sensor.TYPE_ALL)) {
            Log.d("0000",sensor.toString())
            Log.d("0000","Type Memory File: ${sensor.isDirectChannelTypeSupported(SensorDirectChannel.TYPE_MEMORY_FILE)}")
            Log.d("0000","Type Hardware Buffer: ${sensor.isDirectChannelTypeSupported(SensorDirectChannel.TYPE_HARDWARE_BUFFER)}")
        }
        setContent {
            WearApp("Android")
        }
    }
}
