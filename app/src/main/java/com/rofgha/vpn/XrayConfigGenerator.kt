package com.rofgha.vpn

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class XrayConfigGenerator {
    
    data class XrayConfig(
        val log: LogConfig,
        val inbounds: List<Inbound>,
        val outbounds: List<Outbound>,
        val dns: DnsConfig,
        val routing: RoutingConfig
    )
    
    data class LogConfig(val loglevel: String)
    
    data class Inbound(
        val tag: String,
        val port: Int,
        val protocol: String,
        val settings: Map<String, Any>,
        val sniffing: Map<String, Any>? = null
    )
    
    data class Outbound(
        val tag: String,
        val protocol: String,
        val settings: Map<String, Any>,
        val streamSettings: Map<String, Any>? = null
    )
    
    data class DnsConfig(
        val servers: List<String>
    )
    
    data class RoutingConfig(
        val domainStrategy: String,
        val rules: List<Map<String, Any>>
    )
    
    fun generate(vlessConfig: VlessConfig): String {
        val config = XrayConfig(
            log = LogConfig(loglevel = "warning"),
            inbounds = listOf(
                Inbound(
                    tag = "socks",
                    port = 10808,
                    protocol = "socks",
                    settings = mapOf(
                        "auth" to "noauth",
                        "udp" to true
                    ),
                    sniffing = mapOf(
                        "enabled" to true,
                        "destOverride" to listOf("http", "tls")
                    )
                ),
                Inbound(
                    tag = "http",
                    port = 10809,
                    protocol = "http",
                    settings = mapOf(
                        "auth" to "noauth",
                        "udp" to true
                    )
                )
            ),
            outbounds = listOf(
                Outbound(
                    tag = "proxy",
                    protocol = "vless",
                    settings = mapOf(
                        "vnext" to listOf(
                            mapOf(
                                "address" to vlessConfig.server,
                                "port" to vlessConfig.port,
                                "users" to listOf(
                                    mapOf(
                                        "id" to vlessConfig.uuid,
                                        "encryption" to "none",
                                        "level" to 8
                                    )
                                )
                            )
                        )
                    ),
                    streamSettings = generateStreamSettings(vlessConfig)
                ),
                Outbound(
                    tag = "direct",
                    protocol = "freedom",
                    settings = mapOf(
                        "domainStrategy" to "UseIP"
                    )
                ),
                Outbound(
                    tag = "block",
                    protocol = "blackhole",
                    settings = mapOf(
                        "response" to mapOf("type" to "http")
                    )
                )
            ),
            dns = DnsConfig(
                servers = listOf("8.8.8.8", "1.1.1.1")
            ),
            routing = RoutingConfig(
                domainStrategy = "IPIfNonMatch",
                rules = listOf(
                    mapOf(
                        "type" to "field",
                        "ip" to listOf("1.1.1.1"),
                        "outboundTag" to "proxy",
                        "port" to "53"
                    ),
                    mapOf(
                        "type" to "field",
                        "ip" to listOf("8.8.8.8"),
                        "outboundTag" to "proxy",
                        "port" to "53"
                    )
                )
            )
        )
        
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(config)
    }
    
    private fun generateStreamSettings(vlessConfig: VlessConfig): Map<String, Any> {
        val settings = mutableMapOf<String, Any>(
            "network" to vlessConfig.network,
            "security" to vlessConfig.security
        )
        
        // XHTTP settings
        if (vlessConfig.network == "xhttp") {
            settings["xhttpSettings"] = mapOf(
                "path" to vlessConfig.path,
                "host" to vlessConfig.host,
                "mode" to "auto",
                "extra" to mapOf(
                    "mode" to "auto",
                    "xPaddingBytes" to "0-0"
                )
            )
        }
        
        // Reality settings
        if (vlessConfig.security == "reality") {
            settings["realitySettings"] = mapOf(
                "serverName" to vlessConfig.sni,
                "fingerprint" to vlessConfig.fingerprint,
                "publicKey" to vlessConfig.publicKey,
                "shortId" to vlessConfig.shortId,
                "spiderX" to vlessConfig.spiderX,
                "allowInsecure" to true
            )
        }
        
        // TLS settings
        if (vlessConfig.security == "tls") {
            settings["tlsSettings"] = mapOf(
                "serverName" to vlessConfig.sni,
                "fingerprint" to vlessConfig.fingerprint
            )
        }
        
        return settings
    }
}
