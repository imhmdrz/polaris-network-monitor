package ir.uni.polaris.ui.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ir.uni.polaris.model.TestDefinition
import ir.uni.polaris.model.TestResult
import ir.uni.polaris.model.TestType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestsScreen(
    paddingValues: PaddingValues,
    vm: TestsViewModel = hiltViewModel<TestsViewModel>()
) {
    val defs by vm.definitions.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<TestDefinition?>(null) }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        topBar = { TopAppBar(title = { Text("Network Test") }) }
    ) { inner ->
        if (defs.isEmpty()) {
            Box(Modifier
                .fillMaxSize()
                .padding(inner), contentAlignment = Alignment.Center) {
                Text("هنوز تستی تعریف نشده. دکمه + را بزن!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(defs, key = { it.id }) { def ->
                    val lastFlow = remember(def.id) { vm.lastResult(def.id) }
                    val last by lastFlow.collectAsState()
                    TestDefinitionCard(
                        def = def,
                        last = last,
                        onEdit = { editing = def; showDialog = true }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddEditTestDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { vm.save(it) { showDialog = false } }
        )
    }
}


@Composable
private fun TestDefinitionCard(
    def: TestDefinition,
    last: TestResult?,
    onEdit: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    def.target,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                def.type.readable() + "  ---  ",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "intervalMillis: ${def.intervalMillis / 1000}s",
                style = MaterialTheme.typography.bodyMedium
            )

            if (def.type == TestType.HTTP_UPLOAD) {
                Text(
                    "Payload: ${def.payloadBytes ?: 0} bytes",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            Text(
                "last test:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Log.d("mhmdrz" , "${last?.note} - ${last?.timeMillis}")
            if (last == null) {
                Text("——", style = MaterialTheme.typography.bodyMedium)
            } else {
                val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
                val t = fmt.format(Date(last.timeMillis))
                Text("${last.value.format()} ${last.unit}   •   ${if (last.ok) "OK" else "Fail"}   •   $t")
                if (!last.ok && !last.note.isNullOrBlank()) {
                    Text(
                        "message: ${last.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onEdit) { Text("edit") }
            }
        }
    }
}

private fun Double.format(): String = when {
    isNaN() -> "NaN"
    this >= 100 -> String.format(Locale.getDefault(), "%.0f", this)
    this >= 10 -> String.format(Locale.getDefault(), "%.1f", this)
    else -> String.format(Locale.getDefault(), "%.2f", this)
}

private fun TestType.readable(): String = when (this) {
    TestType.HTTP_UPLOAD -> "HTTP Upload"
    TestType.HTTP_DOWNLOAD -> "HTTP Download"
    TestType.PING -> "Ping"
    TestType.DNS -> "DNS"
    TestType.WEB -> "Web (TTFB)"
}

