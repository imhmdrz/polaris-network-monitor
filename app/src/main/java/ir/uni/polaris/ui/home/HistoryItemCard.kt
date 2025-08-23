// ui/history/HistoryItemCard.kt
package ir.uni.polaris.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.uni.polaris.model.Measurement
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun HistoryItemCard(
    m: Measurement,
    modifier: Modifier = Modifier
) {
    val formatterFa = remember {
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale("fa"))
            .withZone(ZoneId.of("Asia/Tehran"))
    }
    val dateStrFa = remember(m.location.timeMillis) {
        formatterFa.format(Instant.ofEpochMilli(m.location.timeMillis))
    }
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "time: $dateStrFa",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(text = m.cell?.radioTech ?: "—")
                    if (m.cell?.generation != null) Pill(text = m.cell.generation)
                    if (m.cell?.band != null) Pill(text = m.cell.band)
                }
            }
            InfoRow(label = "Location", value = "${m.location.lat}, ${m.location.lng}")

            Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.surfaceVariant)

            SectionTitle("شناسه‌ها")
            InfoGrid(
                "Cell ID" to fmtOrDash(m.cell?.cellId),
                "PLMN" to (m.cell?.pLMN ?: dash()),
                ("MCC/MNC" to listOf(m.cell?.mcc, m.cell?.mnc).joinToString("/")),
                "LAC" to fmtOrDash(m.cell?.lac),
                "TAC" to fmtOrDash(m.cell?.tac),
                "RAC" to fmtOrDash(m.cell?.rac),
            )

            if (
                listOf(m.cell?.arfcn, m.cell?.uarfcn, m.cell?.earfcn, m.cell?.nrarfcn, m.cell?.downlinkFreqHz).any { it != null }
            ) {
                SectionTitle("کانال و فرکانس")
                InfoGrid(
                    "ARFCN (2G)" to fmtOrDash(m.cell?.arfcn),
                    "UARFCN (3G)" to fmtOrDash(m.cell?.uarfcn),
                    "EARFCN (4G)" to fmtOrDash(m.cell?.earfcn),
                    "NRARFCN (5G)" to fmtOrDash(m.cell?.nrarfcn),
                    "DL Freq" to hzToPretty(m.cell?.downlinkFreqHz)
                )
            }

            SectionTitle("سیگنال")
            val primarySignal = primarySignalValue(m)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SignalDot(primarySignal?.first, primarySignal?.second)
                Text(
                    text = primarySignal?.second ?: "—",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            InfoGrid(
                "RSRP" to dbm(m.cell?.rSRP),
                "RSRQ" to db(m.cell?.rSRQ),
                "RSCP" to dbm(m.cell?.rSCP),
                "Ec/No" to db(m.cell?.ecNo),
                "RxLev" to dbm(m.cell?.rxLev),
                "SINR" to db(m.cell?.sinr),
            )
        }
    }
}

@Composable
private fun Pill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun InfoGrid(vararg items: Pair<String, String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.toList().chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { (k, v) ->
                    Column(Modifier.weight(1f)) { InfoRow(k, v) }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun dash() = "—"
private fun fmtOrDash(any: Any?) = any?.toString() ?: dash()

private fun hzToPretty(hz: Long?): String = when {
    hz == null -> dash()
    hz >= 1_000_000_000L -> String.format(Locale.getDefault(), "%.2f GHz", hz / 1_000_000_000f)
    hz >= 1_000_000L -> String.format(Locale.getDefault(), "%.1f MHz", hz / 1_000_000f)
    hz >= 1_000L -> String.format(Locale.getDefault(), "%.0f kHz", hz / 1_000f)
    else -> "$hz Hz"
}

private fun dbm(x: Int?): String = x?.let { "$it dBm" } ?: dash()
private fun db(x: Int?): String = x?.let { "$it dB" } ?: dash()
private fun primarySignalValue(m: Measurement): Pair<Int?, String>? {
    val c = m.cell ?: return null
    return when (c.radioTech) {
        "LTE", "NR" -> c.rSRP?.let { it to "RSRP: ${dbm(it)}" }
        "UMTS" -> c.rSCP?.let { it to "RSCP: ${dbm(it)}" }
        "GSM" -> c.rxLev?.let { it to "RxLev: ${dbm(it)}" }
        else -> null
    }
}
@Composable
private fun SignalDot(levelDbm: Int?, label: String?) {
    val color = when {
        levelDbm == null -> MaterialTheme.colorScheme.outline
        levelDbm >= -80 -> MaterialTheme.colorScheme.tertiary
        levelDbm in -95..-81 -> MaterialTheme.colorScheme.primary
        levelDbm in -105..-96 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
    }
}
