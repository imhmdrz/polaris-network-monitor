package ir.uni.polaris.data.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timeMillis: Long,
    val lat: Double,
    val lng: Double,

    // Cell
    val radioTech: String?,
    val generation: String?,
    val plmn: String?,
    val mcc: String?,
    val mnc: String?,
    val lac: Int?,
    val rac: Int?,
    val tac: Int?,
    val tacLacRac: String?,
    val cellId: Long?,

    val arfcn: Int?,
    val uarfcn: Int?,
    val earfcn: Int?,
    val nrarfcn: Int?,
    val band: String?,
    val downlinkFreqHz: Long?,

    val rsrp: Int?,
    val rsrq: Int?,
    val rscp: Int?,
    val ecNo: Int?,
    val rxLev: Int?,
    val sinr: Int?
)
