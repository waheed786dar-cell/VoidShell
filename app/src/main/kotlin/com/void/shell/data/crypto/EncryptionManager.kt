package com.void.shell.data.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val ctx: Context
) {
    private val KEYSTORE   = "AndroidKeyStore"
    private val KEY_ALIAS  = "VoidShellMasterKey"
    private val TRANSFORM  = "AES/GCM/NoPadding"
    private val GCM_IV_LEN = 12
    private val GCM_TAG    = 128

    private lateinit var secretKey: SecretKey

    suspend fun initialize(context: Context) {
        secretKey = getOrCreateKey()
    }

    fun encrypt(plainText: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv         = cipher.iv
        val encrypted  = cipher.doFinal(plainText)
        // Format: [ivLength(1)][iv][encryptedData]
        return byteArrayOf(iv.size.toByte()) + iv + encrypted
    }

    fun decrypt(cipherData: ByteArray): ByteArray {
        val ivLen      = cipherData[0].toInt()
        val iv         = cipherData.copyOfRange(1, 1 + ivLen)
        val encrypted  = cipherData.copyOfRange(1 + ivLen, cipherData.size)
        val cipher     = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG, iv))
        return cipher.doFinal(encrypted)
    }

    fun encryptString(text: String) : ByteArray = encrypt(text.toByteArray(Charsets.UTF_8))
    fun decryptString(data: ByteArray): String  = decrypt(data).toString(Charsets.UTF_8)

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        ks.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }
}
