package pt.ipp.estg.cachyhunt.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "geocache", indices = [Index(value = ["id"], unique = true)])
data class Geocache(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "description")
    var description: String,
    @ColumnInfo(name = "latitude")
    var latitude: Double,
    @ColumnInfo(name = "longitude")
    var longitude: Double,
    @ColumnInfo(name = "location")
    var location: String,
    @ColumnInfo(name = "points")
    var points: Int,
    @ColumnInfo(name = "clues")
    var clues: List<String>,
    @ColumnInfo(name = "createdByUserId")
    val createdByUserId: Int?,
    @ColumnInfo(name = "createdAt")
    val createdAt: Date? = Date(),
    @ColumnInfo(name = "questionId")
    val questionId : Int,
    @ColumnInfo(name = "difficulty")
    var dificuldade: Int,
    @ColumnInfo(name = "lastdiscovered")
    var lastdiscovered: Date? = Date(),
    @ColumnInfo(name = "rating")
    var rating: Double,
    @ColumnInfo(name = "numberofratings")
    var numberofratings: Int,
    @ColumnInfo(name = "status")
    var status: GeocacheStatus
){
    // No-argument constructor for Firestore
    constructor() : this(
        id = 0,
        name = "",
        description = "",
        latitude = 0.0,
        longitude = 0.0,
        location = "",
        points = 0,
        clues = emptyList(),
        createdByUserId = null,
        createdAt = Date(),
        questionId = 0,
        dificuldade = 0,
        lastdiscovered = Date(),
        rating = 0.0,
        numberofratings = 0,
        status = GeocacheStatus.ACTIVE
    )
}