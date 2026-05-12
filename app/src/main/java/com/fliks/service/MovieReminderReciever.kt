package com.fliks.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar

class MovieReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val calendar = Calendar.getInstance()
        val hora = calendar.get(Calendar.HOUR_OF_DAY)
        val minuto = calendar.get(Calendar.MINUTE)
        if (hora == 20 && minuto == 30) {//avisa a las 8:30 PM para que el usuario revise su lista de "ver más tarde"
            val serviceIntent = Intent(context, MovieReminderService::class.java).apply {
                action = "ENVIAR_NOTIFICACION"
            }
            context.startService(serviceIntent)
        }
    }
}