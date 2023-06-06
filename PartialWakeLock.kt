class MainActivity : ComponentActivity() {
    private lateinit var mPowerManager: PowerManager
    private lateinit var mWakeLock: WakeLock
  
    private val mainViewModel = MainViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPowerManager = getSystemService(POWER_SERVICE) as PowerManager
        Log.d("0000","partial wake lock supported: ${mPowerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)}")
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"sensor-logger:pwl")
        mWakeLock.acquire()
        // do stuff
        mWakeLock.release() // eventually 
        setContent {
            WearApp(mainViewModel)
        }
    }
