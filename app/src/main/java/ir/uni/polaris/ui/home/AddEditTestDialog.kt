package ir.uni.polaris.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ir.uni.polaris.model.TestDefinition
import ir.uni.polaris.model.TestType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTestDialog(
    initial: TestDefinition?,
    onDismiss: () -> Unit,
    onSave: (TestDefinition) -> Unit
) {
    var type by remember { mutableStateOf(initial?.type ?: TestType.PING) }
    var target by remember { mutableStateOf(initial?.target ?: "") }
    var intervalSec by remember {
        mutableStateOf(
            ((initial?.intervalMillis ?: 15000L) / 1000).toString()
        )
    }
    var payload by remember { mutableStateOf((initial?.payloadBytes ?: 524288).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "تعریف تست جدید" else "ویرایش تست") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }) {
                    TextField(
                        modifier = Modifier
                          .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                          .fillMaxWidth(),
                        readOnly = true,
                        value = type.readable(),
                        onValueChange = {},
                        label = { Text("نوع تست") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        TestType.entries.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.readable()) },
                                onClick = { type = t; expanded = false }
                            )
                        }
                    }
                }

                // هدف
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target (host یا URL)") },
                    singleLine = true
                )

                // بازه
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = intervalSec,
                    onValueChange = { intervalSec = it.filter { c -> c.isDigit() } },
                    label = { Text("بازه اجرا (ثانیه)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Payload فقط برای Upload
                if (type == TestType.HTTP_UPLOAD) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = payload,
                        onValueChange = { payload = it.filter { c -> c.isDigit() } },
                        label = { Text("Payload (bytes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val intervalMs = (intervalSec.toLongOrNull() ?: 15L) * 1000L
                val def = (initial ?: TestDefinition(
                    type = type,
                    target = target,
                    intervalMillis = intervalMs,
                    payloadBytes = if (type == TestType.HTTP_UPLOAD) payload.toIntOrNull() else null,
                    enabled = true
                )).copy(
                    type = type,
                    target = target,
                    intervalMillis = intervalMs,
                    payloadBytes = if (type == TestType.HTTP_UPLOAD) payload.toIntOrNull() else null,
                    enabled = true
                )
                onSave(def)
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

private fun TestType.readable(): String = when (this) {
    TestType.HTTP_UPLOAD -> "HTTP Upload"
    TestType.HTTP_DOWNLOAD -> "HTTP Download"
    TestType.PING -> "Ping"
    TestType.DNS -> "DNS"
    TestType.WEB -> "Web (TTFB)"
}