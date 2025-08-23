package ir.uni.polaris.data

import ir.uni.polaris.model.CellSnapshot

interface TelephonyDataSource {
    suspend fun snapshot(): CellSnapshot?
}
