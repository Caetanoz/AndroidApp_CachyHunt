package pt.ipp.estg.cachyhunt.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "geocache_captured", indices = [Index(value = ["id"], unique = true)])
data class GeocacheCaptured(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "geocacheId")
    val geocacheId: Int,
    @ColumnInfo(name = "userId")
    var userId: Int,
    @ColumnInfo(name = "capturedAt")
    val capturedAt: String
){
    constructor() : this(0, 0, 0, "")
}
