package top.colter.myplugin.translate

import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

/**
 * MD5编码相关的类
 *
 * @author wangjingtao
 */
object MD5 {
    // 首先初始化一个字符数组，用来存放每个16进制字符
    private val hexDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f')

    /**
     * 获得一个字符串的MD5值
     *
     * @param input 输入的字符串
     * @return 输入字符串的MD5值
     */
    fun md5(input: String?): String? {
        return if (input == null) null else try {
            // 拿到一个MD5转换器（如果想要SHA1参数换成”SHA1”）
            val messageDigest = MessageDigest.getInstance("MD5")
            // 输入的字符串转换成字节数组
            var inputByteArray = ByteArray(0)
            try {
                inputByteArray = input.toByteArray(charset("utf-8"))
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            // inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(inputByteArray)
            // 转换并返回结果，也是字节数组，包含16个元素
            val resultByteArray = messageDigest.digest()
            // 字符数组转换成字符串返回
            byteArrayToHex(resultByteArray)
        } catch (e: NoSuchAlgorithmException) {
            null
        }
    }

    /**
     * 获取文件的MD5值
     *
     * @param file
     * @return
     */
    fun md5(file: File): String? {
        try {
            if (!file.isFile) {
                System.err.println("文件" + file.absolutePath + "不存在或者不是文件")
                return null
            }
            val `in` = FileInputStream(file)
            val result = md5(`in`)
            `in`.close()
            return result
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    fun md5(`in`: InputStream): String? {
        try {
            val messagedigest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(1024)
            var read = 0
            while (`in`.read(buffer).also { read = it } != -1) {
                messagedigest.update(buffer, 0, read)
            }
            `in`.close()
            return byteArrayToHex(messagedigest.digest())
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun byteArrayToHex(byteArray: ByteArray): String {
        // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
        val resultCharArray = CharArray(byteArray.size * 2)
        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        var index = 0
        for (b in byteArray) {
            resultCharArray[index++] = hexDigits[b.toInt().ushr(4) and 0xf]
            resultCharArray[index++] = hexDigits[(b and 0xf).toInt()]
        }

        // 字符数组组合成字符串返回
        return String(resultCharArray)
    }
}