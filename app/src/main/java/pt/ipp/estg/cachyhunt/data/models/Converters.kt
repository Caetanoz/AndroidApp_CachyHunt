package pt.ipp.estg.cachyhunt.data.models

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {

    @TypeConverter
    fun fromUserIdList(userId: List<Int>): String {
        return Gson().toJson(userId)
    }

    @TypeConverter
    fun toUserIdList(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromGeocacheCapturedList(value: List<GeocacheCaptured>): String {
        val gson = Gson()
        val type = object : TypeToken<List<GeocacheCaptured>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toGeocacheCapturedList(value: String): List<GeocacheCaptured> {
        val gson = Gson()
        val type = object : TypeToken<List<GeocacheCaptured>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromQuestion(question: Question?): String? {
        return Gson().toJson(question)
    }

    @TypeConverter
    fun toQuestion(questionString: String?): Question? {
        return Gson().fromJson(questionString, object : TypeToken<Question>() {}.type)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
