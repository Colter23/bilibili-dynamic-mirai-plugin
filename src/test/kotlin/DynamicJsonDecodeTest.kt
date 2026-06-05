package top.colter

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import top.colter.mirai.plugin.bilibili.data.ModuleAuthor
import top.colter.mirai.plugin.bilibili.utils.json

class DynamicJsonDecodeTest {
    @Test
    fun moduleAuthorAcceptsNumericFollowing() {
        val author = json.decodeFromString<ModuleAuthor>(
            """
            {
              "mid": 397823528,
              "name": "author",
              "face": "https://example.invalid/face.jpg",
              "face_nft": false,
              "following": 1,
              "pub_ts": "1780640502"
            }
            """.trimIndent()
        )

        assertEquals("1", author.following?.jsonPrimitive?.content)
        assertEquals(1780640502L, author.pubTs)
    }

    @Test
    fun moduleAuthorKeepsBooleanFollowingCompatible() {
        val author = json.decodeFromString<ModuleAuthor>(
            """
            {
              "mid": 397823528,
              "name": "author",
              "face": "https://example.invalid/face.jpg",
              "following": true
            }
            """.trimIndent()
        )

        assertEquals("true", author.following?.jsonPrimitive?.content)
    }
}
