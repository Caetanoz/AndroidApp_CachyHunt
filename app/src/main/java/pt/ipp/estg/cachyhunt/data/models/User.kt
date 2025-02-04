package pt.ipp.estg.cachyhunt.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user", indices = [Index(value = ["id"], unique = true)])
data class User(

    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "nickName")
    var nickName: String,
    @ColumnInfo(name = "email")
    var email: String,
    @ColumnInfo(name = "password")
    var password: String,
    @ColumnInfo(name = "photo")
    var photo: String,
    @ColumnInfo(name = "userLevel")
    var userLevel: String,
    @ColumnInfo(name = "currentPoints")
    var currentPoints: Int,
    @ColumnInfo(name = "totalPoints")
    var totalPoints: Int,
){
    // No-argument constructor for Firebase
    constructor() : this(0, "", "", "", "", "Beginner", 0, 0)
}