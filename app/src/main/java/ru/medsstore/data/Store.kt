package ru.medsstore.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Одна позиция в домашнем запасе препаратов.
 *
 * Поля «как принимать» и «риски» хранятся в самой записи, а не берутся из
 * справочника при показе. Справочник только подставляет текст в пустые поля
 * при добавлении: инструкция к конкретному препарату может отличаться, и
 * править её должен человек, а не справочник.
 */
@Entity(tableName = "med", indices = [Index("inn"), Index("expiresOn")])
data class MedEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Международное непатентованное наименование. */
    val inn: String = "",
    val form: String = "",
    val dose: String = "",
    /** Сколько осталось: штук, упаковок, миллилитров. */
    val count: Int = 0,
    val unit: String = "шт",
    /** Срок годности в формате ISO yyyy-MM-dd. null — срок не указан. */
    val expiresOn: String? = null,
    val storage: String = "",
    val howToUse: String = "",
    val risks: String = "",
    val note: String = "",
    val addedOn: String = "",
)

@Dao
interface MedDao {
    /**
     * Просроченные и подходящие идут первыми, без срока — в самый низ:
     * человек открывает список, чтобы увидеть, что скоро испортится.
     */
    @Query("SELECT * FROM med ORDER BY (expiresOn IS NULL), expiresOn ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<MedEntity>>

    @Query("SELECT * FROM med WHERE id = :id")
    suspend fun byId(id: Long): MedEntity?

    @Insert
    suspend fun insert(med: MedEntity): Long

    @Update
    suspend fun update(med: MedEntity)

    @Delete
    suspend fun delete(med: MedEntity)
}

@Database(entities = [MedEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meds(): MedDao
}

/**
 * Ручная внедрение зависимостей, как в остальных приложениях проекта: одна
 * база и один дао, отдельный фреймворк ради этого не нужен.
 */
class AppContainer(context: Context) {
    val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "meds.db",
    ).build()

    val settings: Settings = Settings(context.applicationContext)
}

/** Настройки оформления. Хранятся в обычных SharedPreferences. */
class Settings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("meds_prefs", Context.MODE_PRIVATE)

    /** system | dark | light */
    var themeMode: String
        get() = prefs.getString("theme", "dark") ?: "dark"
        set(v) = prefs.edit().putString("theme", v).apply()

    /** teal | indigo | amber | graphite */
    var accent: String
        get() = prefs.getString("accent", "teal") ?: "teal"
        set(v) = prefs.edit().putString("accent", v).apply()

    var compact: Boolean
        get() = prefs.getBoolean("compact", false)
        set(v) = prefs.edit().putBoolean("compact", v).apply()

    /** Порог «скоро истекает» в днях, меняется пользователем. */
    var soonDays: Int
        get() = prefs.getInt("soon", 30)
        set(v) = prefs.edit().putInt("soon", v).apply()
}