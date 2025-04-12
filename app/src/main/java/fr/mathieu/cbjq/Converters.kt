package fr.mathieu.cbjq

import androidx.room.TypeConverter
import java.util.Date

// On utilise une classe Converters pour gérer le type Date dans Room en le stockant comme un Long.
// En effet Room ne sait pas comment conserver les objets Date (https://developer.android.com/training/data-storage/room/referencing-data?hl=fr#kotlin)
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}