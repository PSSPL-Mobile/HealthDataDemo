package com.psspl.healthdatademo.data.security

import android.os.Build
import androidx.annotation.RequiresApi
import com.psspl.healthdatademo.ui.theme.StringResources
import java.security.Key
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@RequiresApi(Build.VERSION_CODES.O)
object EncryptionUtil {

    /***
     * Secret key used for decrypt and encrypt.
     */
    private val keySpec: Key = SecretKeySpec(StringResources.secretKey, "AES")

    /***
     * Secret key used for decrypt and encrypt.
     * @param encryptedData : Stores encrypted data.
     */
    fun decryptData(encryptedData: String): String? {
        try {
            val cipher = Cipher.getInstance(StringResources.transformationAlgo)
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.getDecoder().decode(encryptedData)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            return String(decryptedBytes)
        } catch (e: Exception) {
            return null
        }
    }
}