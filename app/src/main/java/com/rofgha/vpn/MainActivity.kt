package com.rofgha.vpn

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var etConfigUrl: EditText
    private lateinit var btnConnect: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvInfo: TextView
    
    private var isConnected = false
    private var vpnService: VpnService? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize views
        etConfigUrl = findViewById(R.id.etConfigUrl)
        btnConnect = findViewById(R.id.btnConnect)
        tvStatus = findViewById(R.id.tvStatus)
        tvInfo = findViewById(R.id.tvInfo)
        
        // Set click listener
        btnConnect.setOnClickListener {
            if (isConnected) {
                disconnect()
            } else {
                connect()
            }
        }
        
        // Load saved config
        loadSavedConfig()
    }
    
    private fun connect() {
        val configUrl = etConfigUrl.text.toString().trim()
        
        if (configUrl.isEmpty()) {
            Toast.makeText(this, "لطفاً کانفیگ VLESS رو وارد کنید", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Validate VLESS URL
        if (!configUrl.startsWith("vless://")) {
            Toast.makeText(this, "فرمت کانفیگ نادرست است", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Save config
        saveConfig(configUrl)
        
        // Start VPN service
        val intent = Intent(this, VpnService::class.java)
        intent.putExtra("config_url", configUrl)
        ContextCompat.startForegroundService(this, intent)
        
        // Update UI
        isConnected = true
        updateUI()
        
        // Parse and show config info
        showConfigInfo(configUrl)
    }
    
    private fun disconnect() {
        val intent = Intent(this, VpnService::class.java)
        intent.action = "STOP"
        startService(intent)
        
        isConnected = false
        updateUI()
    }
    
    private fun updateUI() {
        if (isConnected) {
            btnConnect.text = "قطع اتصال"
            btnConnect.setBackgroundColor(ContextCompat.getColor(this, R.color.disconnect_red))
            tvStatus.text = "وضعیت: متصل ✅"
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.connected_green))
        } else {
            btnConnect.text = "اتصال"
            btnConnect.setBackgroundColor(ContextCompat.getColor(this, R.color.connect_green))
            tvStatus.text = "وضعیت: قطع ❌"
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.disconnected_red))
        }
    }
    
    private fun showConfigInfo(configUrl: String) {
        try {
            val parser = VlessParser(configUrl)
            val info = parser.parse()
            
            tvInfo.text = """
                📍 سرور: ${info.server}
                🚪 پورت: ${info.port}
                🔐 امنیت: ${info.security}
                🌐 شبکه: ${info.network}
                🎯 FP: ${info.fingerprint}
            """.trimIndent()
        } catch (e: Exception) {
            tvInfo.text = "خطا در خواندن کانفیگ"
        }
    }
    
    private fun saveConfig(config: String) {
        val prefs = getSharedPreferences("vpn_config", MODE_PRIVATE)
        prefs.edit().putString("config_url", config).apply()
    }
    
    private fun loadSavedConfig() {
        val prefs = getSharedPreferences("vpn_config", MODE_PRIVATE)
        val savedConfig = prefs.getString("config_url", "")
        if (!savedConfig.isNullOrEmpty()) {
            etConfigUrl.setText(savedConfig)
        }
    }
}
