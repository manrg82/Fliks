package com.fliks.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.fliks.R
import com.fliks.data.SupabaseClient
import com.fliks.model.WatchLaterMovie
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MovieReminderService : Service() {
    private lateinit var receiver: MovieReminderReceiver
    private val canalId = "canal_fliks_01"
    override fun onCreate() {
        super.onCreate()
        receiver = MovieReminderReceiver()
        crearCanalNotificaciones()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "ENVIAR_NOTIFICACION") {
            consultarYNotificar()
        } else {
            val filter = IntentFilter(Intent.ACTION_TIME_TICK)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(receiver, filter)
            }
        }
        return START_STICKY
    }
    private fun consultarYNotificar() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usuario = SupabaseClient.client.auth.currentUserOrNull()
                val userId = usuario?.id ?: return@launch
                val respuesta = SupabaseClient.client.from("watch_later").select {
                    filter { eq("user_id", userId) }
                }
                val listaPendientes = respuesta.decodeList<WatchLaterMovie>()
                val cantidad = listaPendientes.size
                // solo notifica si el usuario tiene películas pendientes
                if (cantidad > 0) {
                    mostrarNotificacion(cantidad)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun mostrarNotificacion(cantidad: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(R.drawable.logosvg)
            .setContentTitle(getString(R.string.notif_titulo))
            .setContentText(getString(R.string.notif_mensaje, cantidad))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        notificationManager.notify(1, builder.build())
    }
    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Recordatorios de Fliks",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones diarias de tu lista de pendientes"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(canal)
        }
    }
    override fun onBind(intent: Intent): IBinder? = null
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}