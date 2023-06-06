class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database
        ).build()
        val pointDao = db.pointDao()
        pointDao.insertAll(Point(0,12315,.1235,.76,.125987))
        setContent {
            WearApp("Android")
        }
    }
}
@Entity
data class Point(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "timestamp") val timestamp: Long?,
    @ColumnInfo(name = "acc_x") val x: Float?,
    @ColumnInfo(name = "acc_y") val y: Float?,
    @ColumnInfo(name = "acc_z") val z: Float?
)
@Dao
interface PointDao {
    @Insert
    suspend fun insertAll(vararg points: Point)
}
@Database(entities = [Point::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pointDao(): PointDao
}
