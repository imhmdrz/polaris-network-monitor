package ir.uni.polaris.model

data class CellSnapshot(
  // Tech & IDs
  val radioTech: String,
  val generation: String? = null,
  val pLMN: String? = null,
  val mcc: String? = null,
  val mnc: String? = null,
  val lac: Int? = null,
  val rac: Int? = null,
  val tac: Int? = null,
  val tacLacRac: String? = null,
  val cellId: Long? = null,

  // Channels & Frequency
  val arfcn: Int? = null,
  val uarfcn: Int? = null,
  val earfcn: Int? = null,
  val nrarfcn: Int? = null,
  val band: String? = null,
  val downlinkFreqHz: Long? = null,

  // Signal
  val rSRP: Int? = null,
  val rSRQ: Int? = null,
  val rSCP: Int? = null,
  val ecNo: Int? = null,
  val rxLev: Int? = null,
  val sinr: Int? = null
)
