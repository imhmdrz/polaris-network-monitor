package ir.uni.polaris.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.uni.polaris.model.CellSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelephonyDataSourceImpl @Inject constructor(
    private val tm: TelephonyManager,
    @ApplicationContext private val ctx: Context,
) : TelephonyDataSource {

    override suspend fun snapshot(): CellSnapshot? = withContext(Dispatchers.IO) {
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return@withContext null

        val cells = tm.allCellInfo ?: return@withContext null
        val primary =
            cells.firstOrNull { it.isRegistered } ?: cells.firstOrNull() ?: return@withContext null

        when (primary) {
            is CellInfoLte -> parseLte(primary)
            is CellInfoWcdma -> parseWcdma(primary)
            is CellInfoGsm -> parseGsm(primary)
            else -> if (Build.VERSION.SDK_INT >= 29 && primary is CellInfoNr) parseNr29(primary) else null
        }
    }

    private fun parseLte(info: CellInfoLte): CellSnapshot {
        val id = info.cellIdentity
        val ss = info.cellSignalStrength
        val mcc = id.mccString
        val mnc = id.mncString
        val earfcn = id.earfcn
        val bandInfo = earfcn?.let { ArfcnUtils.lteBand(it) }

        return CellSnapshot(
            radioTech = "LTE",
            generation = "4G",
            pLMN = "${mcc ?: ""}${mnc ?: ""}",
            mcc = mcc,
            mnc = mnc,
            tac = id.tac,
            tacLacRac = id.tac?.toString(),
            cellId = id.ci?.toLong(),
            earfcn = earfcn,
            band = bandInfo?.band,
            downlinkFreqHz = bandInfo?.dlFreqHz,
            rSRP = ss.rsrp,
            rSRQ = ss.rsrq,
            sinr = ss.rssnr,
            rSCP = null,
            ecNo = null,
            arfcn = null,
            uarfcn = null,
            nrarfcn = null,
            rxLev = null,
            lac = null,
            rac = null
        )
    }

    private fun parseWcdma(info: CellInfoWcdma): CellSnapshot {
        val id = info.cellIdentity
        val ss = info.cellSignalStrength
        val mcc = id.mccString
        val mnc = id.mncString
        val uarfcn = id.uarfcn
        val bandInfo = uarfcn?.let { ArfcnUtils.umtsDlFreqHz(it) }
        val ecNo = if (Build.VERSION.SDK_INT >= 30) ss.ecNo else null

        return CellSnapshot(
            radioTech = "UMTS",
            generation = "3G",
            pLMN = "${mcc ?: ""}${mnc ?: ""}",
            mcc = mcc,
            mnc = mnc,
            lac = id.lac,
            tacLacRac = id.lac?.toString(),
            cellId = id.cid?.toLong(),
            uarfcn = uarfcn,
            band = bandInfo?.band,
            downlinkFreqHz = bandInfo?.dlFreqHz,
            rSCP = ss.dbm,
            ecNo = ecNo,
            rSRP = null,
            rSRQ = null,
            sinr = null,
            arfcn = null,
            earfcn = null,
            nrarfcn = null,
            rxLev = null,
            tac = null,
            rac = null
        )
    }

    private fun parseGsm(info: CellInfoGsm): CellSnapshot {
        val id = info.cellIdentity
        val ss = info.cellSignalStrength
        val mcc = id.mccString
        val mnc = id.mncString
        val arfcn = id.arfcn
        val band = arfcn?.let { ArfcnUtils.gsmBand(it) }

        return CellSnapshot(
            radioTech = "GSM",
            generation = "2G",
            pLMN = "${mcc ?: ""}${mnc ?: ""}",
            mcc = mcc,
            mnc = mnc,
            lac = id.lac,
            rac = null,
            tacLacRac = id.lac?.toString(),
            cellId = id.cid?.toLong(),
            arfcn = arfcn,
            band = band,
            downlinkFreqHz = null,
            rxLev = ss.dbm,
            rSRP = null,
            rSRQ = null,
            rSCP = null,
            ecNo = null,
            sinr = null,
            uarfcn = null,
            earfcn = null,
            nrarfcn = null,
            tac = null
        )
    }

    @RequiresApi(29)
    private fun parseNr29(info: CellInfoNr): CellSnapshot {
        val id = info.cellIdentity as CellIdentityNr
        val ss = info.cellSignalStrength as CellSignalStrengthNr
        val mcc = id.mccString
        val mnc = id.mncString
        val nrarfcn = id.nrarfcn
        val approxHz = nrarfcn?.let { ArfcnUtils.nrApproxFreqHz(it) }

        return CellSnapshot(
            radioTech = "NR",
            generation = "5G",
            pLMN = "${mcc ?: ""}${mnc ?: ""}",
            mcc = mcc,
            mnc = mnc,
            tac = id.tac,
            tacLacRac = id.tac?.toString(),
            cellId = id.nci,
            nrarfcn = nrarfcn,
            band = null,
            downlinkFreqHz = approxHz,
            rSRP = ss.ssRsrp,
            rSRQ = ss.ssRsrq,
            sinr = ss.ssSinr,
            rSCP = null,
            ecNo = null,
            rxLev = null,
            arfcn = null,
            uarfcn = null,
            earfcn = null,
            lac = null,
            rac = null
        )
    }
}
