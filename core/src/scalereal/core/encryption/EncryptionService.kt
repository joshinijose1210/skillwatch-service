package scalereal.core.encryption

import jakarta.inject.Singleton
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

@Singleton
class EncryptionService {
    fun encryptPassword(password: String): String {
        val key = SecretKeySpec(password.toByteArray().copyOf(16), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedPassword = cipher.doFinal(password.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedPassword)
    }
}
