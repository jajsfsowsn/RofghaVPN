package com.rofgha.vpn

import android.net.Uri
import java.net.URLDecoder

data class VlessConfig(
    val uuid: String,
    val server: String,
    val port: Int,
    val network: String,
    val security: String,
    val sni: String,
    val fingerprint: String,
    val publicKey: String,
    val shortId: String,
    val spiderX: String,
    val path: String,
    val host: String,
    val remark: String
)

class VlessParser(private val url: String) {
    
    fun parse(): VlessConfig {
        try {
            val uri = Uri.parse(url)
            
            // Extract UUID
            val uuid = uri.userInfo ?: throw IllegalArgumentException("UUID not found")
            
            // Extract server and port
            val server = uri.host ?: throw IllegalArgumentException("Server not found")
            val port = uri.port.takeIf { it > 0 } ?: throw IllegalArgumentException("Port not found")
            
            // Extract query parameters
            val params = mutableMapOf<String, String>()
            uri.queryParameterNames.forEach { name ->
                params[name] = uri.getQueryParameter(name) ?: ""
            }
            
            // Extract parameters
            val network = params["type"] ?: "tcp"
            val security = params["security"] ?: "tls"
            val sni = params["sni"] ?: ""
            val fingerprint = params["fp"] ?: "chrome"
            val publicKey = params["pbk"] ?: ""
            val shortId = params["sid"] ?: ""
            val spiderX = URLDecoder.decode(params["spx"] ?: "/", "UTF-8")
            val path = URLDecoder.decode(params["path"] ?: "/", "UTF-8")
            val host = params["host"] ?: server
            val remark = URLDecoder.decode(uri.fragment ?: "Rofgha VPN", "UTF-8")
            
            return VlessConfig(
                uuid = uuid,
                server = server,
                port = port,
                network = network,
                security = security,
                sni = sni,
                fingerprint = fingerprint,
                publicKey = publicKey,
                shortId = shortId,
                spiderX = spiderX,
                path = path,
                host = host,
                remark = remark
            )
            
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid VLESS URL: ${e.message}")
        }
    }
}
