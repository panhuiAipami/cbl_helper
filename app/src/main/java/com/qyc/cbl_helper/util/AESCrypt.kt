package cn.cpocar.qyc_cbl.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AESCrypt private constructor() {

    companion object {
        private const val KEY = "3GqYBsNwc48un4PV"

        //AES加密
        fun encrypt(input: String): String {
            //初始化cipher对象
            val cipher = Cipher.getInstance("AES")
            // 生成密钥
            val keySpec: SecretKeySpec? = SecretKeySpec(KEY.toByteArray(), "AES")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            //加密解密
            val encrypt = cipher.doFinal(input.toByteArray())
            return Base64.encodeToString(encrypt, Base64.DEFAULT)
        }

        //AES解密
        fun decrypt(input: String): String {
            //初始化cipher对象
            val cipher = Cipher.getInstance("AES")
            // 生成密钥
            val keySpec: SecretKeySpec? = SecretKeySpec(KEY.toByteArray(), "AES")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            //加密解密
            return Base64.encodeToString(input.toByteArray(), Base64.DEFAULT)
        }
    }
}

fun main() {
    print(Base64.encodeToString("input".toByteArray(), Base64.DEFAULT))
}



