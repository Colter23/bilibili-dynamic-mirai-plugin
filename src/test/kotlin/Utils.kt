import org.jetbrains.skia.Image
import java.io.File


val resource = File("src/main/resources")
val testResource = File("src/test/resources")
val testOutput = testResource.resolve("output").apply {
    if(!exists()) this.mkdirs()
}

// 加载测试资源
fun loadTestResource(path: String = "", fileName: String) =
    testResource.resolve(path).resolve(fileName)
// 加载测试图片
fun loadTestImage(path: String = "", fileName: String) =
    Image.makeFromEncoded(loadTestResource(path, fileName).readBytes())
// 加载测试文本
fun loadTestText(path: String = "", fileName: String) =
    loadTestResource(path, fileName).readText()