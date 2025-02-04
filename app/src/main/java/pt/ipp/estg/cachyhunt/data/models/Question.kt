package pt.ipp.estg.cachyhunt.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Question", indices = [Index(value = ["id"], unique = true)])
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "question")
    val question: String,
    @ColumnInfo(name = "correctAnswer")
    val correctAnswer: String
){
    constructor() : this(0, "", "")
}