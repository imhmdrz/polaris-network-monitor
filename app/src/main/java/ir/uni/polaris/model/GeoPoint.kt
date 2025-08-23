package ir.uni.polaris.model

data class GeoPoint(val lat: Double, val lng: Double, val timeMillis: Long = System.currentTimeMillis())
