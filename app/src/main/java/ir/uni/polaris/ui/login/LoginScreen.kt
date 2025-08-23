package ir.uni.polaris.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    vm: AuthViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    var mode by remember { mutableStateOf(AuthMode.Login) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ElevatedCard(Modifier.padding(24.dp)) {
            Column(
                Modifier
                  .padding(20.dp)
                  .widthIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Polaris", style = MaterialTheme.typography.headlineMedium)
                Text(
                    if (mode == AuthMode.Login) "ورود" else "ثبت‌نام",
                    style = MaterialTheme.typography.titleMedium
                )

                SegmentedButtons(mode) { mode = it }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("نام کاربری") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("رمز عبور") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (state.error != null) {
                    Text(
                        state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        if (mode == AuthMode.Login)
                            vm.login(username.trim(), password) { onLoggedIn() }
                        else
                            vm.signup(username.trim(), password) { onLoggedIn() }
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoading) CircularProgressIndicator(
                        Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    else Text(if (mode == AuthMode.Login) "ورود" else "ثبت‌نام")
                }
            }
        }
    }
}

private enum class AuthMode { Login, SignUp }

@Composable
private fun SegmentedButtons(current: AuthMode, onChange: (AuthMode) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = current == AuthMode.Login,
            onClick = { onChange(AuthMode.Login) },
            label = { Text("Login") })
        FilterChip(
            selected = current == AuthMode.SignUp,
            onClick = { onChange(AuthMode.SignUp) },
            label = { Text("Sign Up") })
    }
}
