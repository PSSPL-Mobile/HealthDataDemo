package com.psspl.healthdatademo.data.blockchain

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

data class Block(
    val index: Int,
    val timestamp: Long,
    val heartRateData: String,
    var previousHash: String,
    var hash: String = calculateHash()
) {
    companion object {
        fun calculateHash(
            index: Int = 0,
            timestamp: Long = System.currentTimeMillis(),
            data: String = "0",
            previousHash: String = ""
        ): String {
            val dataToHash = "$index$timestamp$data$previousHash"
            val bytes = dataToHash.toByteArray(StandardCharsets.UTF_8)
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }
}