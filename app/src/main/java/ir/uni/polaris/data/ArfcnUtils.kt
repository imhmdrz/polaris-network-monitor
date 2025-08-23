package ir.uni.polaris.data

object ArfcnUtils {

    data class BandInfo(val band: String, val dlFreqHz: Long?)

    fun lteBand(earfcn: Int): BandInfo? = when (earfcn) {
        in 0..599 -> band("B1", 2110.0, 0, earfcn)
        in 1200..1949 -> band("B3", 1805.0, 1200, earfcn)
        in 2750..3449 -> band("B7", 2620.0, 2750, earfcn)
        in 6150..6449 -> band("B20", 791.0, 6150, earfcn)
        in 37750..38249 -> band("B38", 2570.0, 37750, earfcn)
        in 38650..39649 -> band("B40", 2300.0, 38650, earfcn)
        in 39650..41589 -> band("B41", 2496.0, 39650, earfcn)
        else -> null
    }

    private fun band(name: String, fdlLowMHz: Double, nOffs: Int, earfcn: Int): BandInfo {
        val mhz = fdlLowMHz + 0.1 * (earfcn - nOffs)
        return BandInfo(name, (mhz * 1_000_000).toLong())
    }

    fun umtsDlFreqHz(uarfcn: Int): BandInfo? = when (uarfcn) {
        in 10562..10838 -> { // Band 1: FDL = 2112.4 + 0.2*(N-10562)
            val mhz = 2112.4 + 0.2 * (uarfcn - 10562)
            BandInfo("B1", (mhz * 1_000_000).toLong())
        }

        in 2937..3088 -> { // Band 8: FDL = 925.2 + 0.2*(N-2937)
            val mhz = 925.2 + 0.2 * (uarfcn - 2937)
            BandInfo("B8", (mhz * 1_000_000).toLong())
        }

        else -> null
    }

    fun gsmBand(arfcn: Int): String? = when (arfcn) {
        in 1..124 -> "GSM-900"
        in 128..251 -> "GSM-850"
        in 512..885 -> "DCS-1800"
        in 512..810 -> "PCS-1900"
        else -> null
    }

    fun nrApproxFreqHz(nrarfcn: Int): Long = 5_000L * nrarfcn
}
