package ir.uni.polaris.ui.home

sealed interface HomeIntent {
    data object ToggleMonitor : HomeIntent
    data object RequestPermissionsIfNeeded : HomeIntent
}