package com.example.practicaexamen

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "new_bar_notifications"
        private const val CHANNEL_NAME = "Bares"
        private const val CHANNEL_DESCRIPTION = "Notificaciones de nuevos bares"
        private const val TAG = "NotificationHelper"
    }

    init {
        Log.d(TAG, "Inicializando NotificationHelper")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creando canal de notificación")
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager: NotificationManager =
                context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNewBarNotification(barName: String, barWeb: String) {
        Log.d(TAG, "Intentando enviar notificación para: $barName - $barWeb")

        // Verificar permisos en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Permiso POST_NOTIFICATIONS NO concedido")
                return
            } else {
                Log.d(TAG, "Permiso POST_NOTIFICATIONS concedido")
            }
        }

        // Crear intent para abrir la web del bar
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(barWeb)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nuevo bar: $barName")
            .setContentText("Visita su web: $barWeb")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Enviar la notificación
        NotificationManagerCompat.from(context).notify(1, notification)
        Log.d(TAG, "Notificación enviada correctamente")
    }
}