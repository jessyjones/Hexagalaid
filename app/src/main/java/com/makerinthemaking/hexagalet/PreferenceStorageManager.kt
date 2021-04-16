package com.makerinthemaking.hexagalet

import androidx.room.*
import android.content.Context

@Entity
data class AppSetting(
        @PrimaryKey val uid: Int,
        @ColumnInfo(name = "package_name") val packageName: String?,
        @ColumnInfo(name = "command_string") val commandString: String?
)

@Dao
interface AppSettingDao {
    @Query("SELECT * FROM appsetting")
    fun getAll(): List<AppSetting>

    @Query("SELECT * FROM appsetting WHERE uid IN (:appSettingIds)")
    fun loadAllByIds(appSettingIds: IntArray): List<AppSetting>

    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): AppSetting

    @Insert
    fun insertAll(vararg users: AppSetting)

    @Delete
    fun delete(user: AppSetting)
}

@Database(entities = arrayOf(AppSetting::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): AppSettingDao
}

class PreferenceStorageManager {
/*    public fun initi(Context: ctx)
    {
        val db = Room.databaseBuilder(
                ctx,
                AppDatabase::class.java, "database-name"
        ).build()

    }

 */
}