package ir.uni.polaris.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.uni.polaris.domain.MeasurementsRepository
import javax.inject.Inject
import android.R
import ir.uni.polaris.domain.NetworkTestsRepository

@AndroidEntryPoint
class ForegroundMonitorService : Service() {

    @Inject
    lateinit var repo: MeasurementsRepository

    @Inject
    lateinit var testsRepo: NetworkTestsRepository

    override fun onCreate() {
        super.onCreate()
        startInForeground()
        testsRepo.start()
        repo.start(5_000)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        repo.stop()
        testsRepo.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    private fun startInForeground() {
        val channelId = "monitor_channel"
        val mgr = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        mgr.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Polaris Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
        )
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.btn_radio)
            .setContentTitle("Polaris در حال پایش")
            .setContentText("ثبت دوره‌ای موقعیت و پارامترهای شبکه")
            .build()
        startForeground(1001, notif)
    }
}
