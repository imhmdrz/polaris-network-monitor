package ir.uni.polaris.domain

import ir.uni.polaris.data.storage.MeasurementEntity
import ir.uni.polaris.model.CellSnapshot
import ir.uni.polaris.model.GeoPoint
import ir.uni.polaris.model.Measurement

fun Measurement.toEntity() = MeasurementEntity(
    timeMillis = location.timeMillis,
    lat = location.lat,
    lng = location.lng,
    radioTech = cell?.radioTech,
    generation = cell?.generation,
    plmn = cell?.pLMN,
    mcc = cell?.mcc,
    mnc = cell?.mnc,
    lac = cell?.lac,
    rac = cell?.rac,
    tac = cell?.tac,
    tacLacRac = cell?.tacLacRac,
    cellId = cell?.cellId,
    arfcn = cell?.arfcn,
    uarfcn = cell?.uarfcn,
    earfcn = cell?.earfcn,
    nrarfcn = cell?.nrarfcn,
    band = cell?.band,
    downlinkFreqHz = cell?.downlinkFreqHz,
    rsrp = cell?.rSRP,
    rsrq = cell?.rSRQ,
    rscp = cell?.rSCP,
    ecNo = cell?.ecNo,
    rxLev = cell?.rxLev,
    sinr = cell?.sinr
)

fun MeasurementEntity.toDomain(): Measurement =
    Measurement(
        location = GeoPoint(
            lat = lat,
            lng = lng,
            timeMillis = timeMillis
        ),
        CellSnapshot(
            radioTech = radioTech ?: "UNKNOWN",
            generation = generation,
            pLMN = plmn,
            mcc = mcc,
            mnc = mnc,
            lac = lac,
            rac = rac,
            tac = tac,
            tacLacRac = tacLacRac,
            cellId = cellId,
            arfcn = arfcn,
            uarfcn = uarfcn,
            earfcn = earfcn,
            nrarfcn = nrarfcn,
            band = band,
            downlinkFreqHz = downlinkFreqHz,
            rSRP = rsrp,
            rSRQ = rsrq,
            rSCP = rscp,
            ecNo = ecNo,
            rxLev = rxLev,
            sinr = sinr
        )
    )


