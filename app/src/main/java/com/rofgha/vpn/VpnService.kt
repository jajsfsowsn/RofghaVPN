package com.rofgha.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class VpnService : VpnService() {
    
    companion object {
        private const val TAG = "VpnService"
        private const val CHANNEL_ID = "VpnServiceChannel"
        private const val NOTIFICATION_ID = 1
    }
    
    private var vpnInterface: ParcelFileDescriptor? = null
    private var xrayCore: XrayCore? = null
    private var configUrl: String? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            stopVpn()
            return START_NOT_STICKY
        }
        
        configUrl = intent?.getStringExtra("config_url")
        
        if (configUrl.isNullOrEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Start VPN
        startVpn()
        
        return START_STICKY
    }
    
    private fun startVpn() {
        try {
            // Build VPN config
            val builder = Builder()
            builder.setSession("Rofgha VPN")
            builder.addAddress("10.0.0.2", 32)
            builder.addRoute("0.0.0.0", 0)
            builder.addDnsServer("8.8.8.8")
            builder.addDnsServer("8.8.4.4")
            builder.setMtu(1500)
            
            // Establish VPN
            vpnInterface = builder.establish()
            
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN")
                stopSelf()
                return
            }
            
            // Start Xray core
            startXray()
            
            Log.d(TAG, "VPN started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopSelf()
        }
    }
    
    private fun startXray() {
        try {
            // Create Xray config file
            val configFile = createXrayConfig()
            
            // Initialize XrayCore
            xrayCore = XrayCore(this)
            
            // Start Xray
            val started = xrayCore!!.start(configFile.absolutePath)
            
            if (!started) {
                Log.e(TAG, "Failed to start Xray")
                stopSelf()
                return
            }
            
            Log.d(TAG, "Xray started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Xray", e)
        }
    }
    
    private fun createXrayConfig(): File {
        val configFile = File(filesDir, "config.json")
        
        // Parse VLESS URL
        val parser = VlessParser(configUrl!!)
        val vlessConfig = parser.parse()
        
        // Generate Xray config using the generator
        val generator = XrayConfigGenerator()
        val configJson = generator.generate(vlessConfig)
        
        FileOutputStream(configFile).use { fos ->
            fos.write(configJson.toByteArray())
        }
        
        return configFile
    }
    
    private fun stopVpn() {
        try {
            xrayCore?.stop()
            vpnInterface?.close()
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping VPN", e)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Rofgha VPN Service"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Rofgha VPN")
                .setContentText("متصل به VPN")
                .setSmallIcon(R.drawable.ic_vpn_key)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Rofgha VPN")
                .setContentText("متصل به VPN")
                .setSmallIcon(R.drawable.ic_vpn_key)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build()
        }
    }
    
    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
