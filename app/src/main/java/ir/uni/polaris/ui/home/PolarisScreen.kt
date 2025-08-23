package ir.uni.polaris.ui.home

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import ir.uni.polaris.ui.login.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PolarisScreen(
    paddingValues: PaddingValues,
    vm: HomeViewModel = hiltViewModel<HomeViewModel>(),
    avm: AuthViewModel = hiltViewModel<AuthViewModel>()
) {
    val st by vm.state.collectAsStateWithLifecycle()
    val last = vm.history.collectAsStateWithLifecycle().value.firstOrNull()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    val context = LocalContext.current

    val isLocationEnabled by rememberLocationEnabledAsState()

    fun promptEnableLocation() {
        val client = LocationServices.getSettingsClient(context)
        val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000).build()
        val settings = LocationSettingsRequest.Builder()
            .addLocationRequest(req)
            .setAlwaysShow(true)
            .build()

        client.checkLocationSettings(settings)
            .addOnSuccessListener {}
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        context.findAndroidActivity()?.let { e.startResolutionForResult(it, 1001) };
                    } catch (_: Throwable) {
                        openLocationSettings(context)
                    }
                } else {
                    openLocationSettings(context)
                }
            }
    }


    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_PHONE_STATE,
                )
            )
        }
    }

    Column(
        Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLocationEnabled) {
            avm.user.collectAsStateWithLifecycle().value?.let {
                Text(text = "user : ${it.username}")
            }
            Button(onClick = { vm.onIntent(HomeIntent.ToggleMonitor) }) {
                Text(if (st.isRunning) "Shut Down Polaris" else "Start Polaris")
            }
            last?.let {
                Text("last record: ")
                HistoryItemCard(it)
            }
            Text("records count : ${st.measurementsCount}")
        } else {
            ElevatedCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Location is off", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { promptEnableLocation() }) {
                        Text("turn on")
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberLocationEnabledAsState(): State<Boolean> {
    val context = LocalContext.current
    val lm = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    val state = remember { mutableStateOf(lm.isLocationEnabled) }

    DisposableEffect(context) {
        val filter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            addAction(LocationManager.MODE_CHANGED_ACTION)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: Intent?) {
                state.value = lm.isLocationEnabled
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return state
}

private fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun Context.findAndroidActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}