import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashSet
import kotlin.concurrent.thread

fun main() {
    val link = readLine()
    val userName = System.getenv()["USER"]
    val path = when (checkOS()) {
        OS.WINDOWS -> System.getenv("SystemDrive") + "\\Users\\${System.getProperty("user.name")}\\Downloads\\lofter爬虫"
        OS.MAC -> "/Users/$userName/Pictures/lofter爬虫/"
        else -> ""
    }
    if (!File(path).isDirectory) File(path).mkdir()
    link?.let { loadData(it, path) }
}

enum class OS {
    WINDOWS, MAC
}

fun checkOS(): OS? = when {
    System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win") -> OS.WINDOWS
    System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac") -> OS.MAC
    else -> null
}

fun loadData(url: String, path: String) {
    val resultSet = HashSet<String>()
    thread {
        val connection = createConnection(url)
        try {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            val pattern = Pattern.compile("""bigimgsrc="(.*?)(?=\?imageView)""")
            val matcher = pattern.matcher(response)
            while (matcher.find()) {
                resultSet.add(matcher.group(1))
            }
            resultSet.forEach { imageUrl ->
                download(imageUrl, path)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
    }
}

fun download(imageURL: String, path: String) {
    thread {
        val connection = createConnection(imageURL)
        try {
            val inputStream: InputStream = connection.inputStream
            val fileName = imageURL.substringAfterLast("/")
            val outputPath = "$path$fileName"
            FileOutputStream(outputPath).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            println("图片已成功下载到 $outputPath")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
    }
}

fun createConnection(URL: String): HttpURLConnection = (URL(URL).openConnection() as HttpURLConnection).apply {
    setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
    setRequestProperty("Referer", URL)
    requestMethod = "GET"
    connectTimeout = 8000
    readTimeout = 8000
}