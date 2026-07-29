package com.rofgha.vpn

import android.content.Context
import android.os.Build
import java.io.*

class XrayCore(private val context: Context) {
    
    companion object {
        private const val TAG = "XrayCore"
    }
    
    private var process: Process? = null
    private var isRunning = false
    
    fun start(configPath: String): Boolean {
        return try {
            // Detect architecture and copy correct binary
            val binaryName = getBinaryName()
            val binaryPath = copyBinaryIfNeeded(binaryName)
            
            // Make executable
            Runtime.getRuntime().exec("chmod 755 $binaryPath")
            
            // Start Xray
            val processBuilder = ProcessBuilder(
                binaryPath,
                "run",
                "-c", configPath
            )
            processBuilder.redirectErrorStream(true)
            processBuilder.directory(context.filesDir)
            
            process = processBuilder.start()
            isRunning = true
            
            // Read output in background
            Thread {
                try {
                    val reader = process?.inputStream?.bufferedReader()
                    reader?.forEachLine { line ->
                        android.util.Log.d(TAG, "Xray: $line")
                    }
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Error reading output", e)
                }
            }.start()
            
            android.util.Log.d(TAG, "Xray started with binary: $binaryName")
            true
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error starting Xray", e)
            false
        }
    }
    
    fun stop() {
        try {
            process?.destroy()
            isRunning = false
            android.util.Log.d(TAG, "Xray stopped")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error stopping Xray", e)
        }
    }
    
    fun isRunning(): Boolean = isRunning
    
    private fun getBinaryName(): String {
        val arch = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        
        return when {
            arch.contains("arm64") || arch.contains("aarch64") -> "xray-arm64"
            arch.contains("arm") -> "xray-arm32"
            arch.contains("x86_64") || arch.contains("amd64") -> "xray-x64"
            arch.contains("x86") || arch.contains("i686") || arch.contains("i386") -> "xray-x86"
            else -> "xray-arm64" // default
        }
    }
    
    private fun copyBinaryIfNeeded(binaryName: String): String {
        val binaryFile = File(context.filesDir, "xray")
        
        if (!binaryFile.exists()) {
            android.util.Log.d(TAG, "Copying Xray binary: $binaryName")
            
            try {
                context.assets.open(binaryName).use { input ->
                    FileOutputStream(binaryFile).use { output ->
                        input.copyTo(output)
                    }
                }
                android.util.Log.d(TAG, "Binary copied successfully")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error copying binary", e)
                throw Exception("Xray binary not found: $binaryName")
            }
        }
        
        return binaryFile.absolutePath
    }
}
