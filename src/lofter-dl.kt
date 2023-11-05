import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern
import kotlin.concurrent.thread

fun main() {
    val link = readLine()
    val map = System.getenv()
    val userName = map["USER"]
    if(!link.isNullOrBlank()){
        loadData(link.toString(),userName)
    }

}


fun loadData(URL:String,userName:String?){
    val resultSet = HashSet<String>()
    thread {
        val url = URL(URL)
        val connection = url.openConnection() as HttpURLConnection
        try {
            val response = StringBuilder()
            connection.setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
            connection.setRequestProperty("Referer",URL)
            connection.setRequestProperty("Origin",URL)
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            val input = connection.inputStream
            val reader = BufferedReader(InputStreamReader(input))
            reader.apply {
                forEachLine {
                    response.append(it)
                }
            }
            val pattern = Pattern.compile("""bigimgsrc="(.*?)(?=\?imageView)""")
            val matcher = pattern.matcher(response.toString())
            while (matcher.find()) {
                resultSet.add(matcher.group(1))
            }
            for(items in resultSet){
                download(items,userName)
            }

        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            connection.disconnect()
        }
    }
}
fun download(imageURL:String,userName: String?) {
    val imageUrl = imageURL
    val url = URL(imageUrl)
    thread {
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36"
            )
            connection.setRequestProperty("Referer", imageURL)
            connection.setRequestProperty("Origin", imageURL)
            val inputStream: InputStream = connection.inputStream
            val result: Regex = Regex(pattern = "/([^/]+)\$")
            val outputPath = "/Users/${userName}/Pictures/lofter爬虫/${result.find(imageURL)?.value}"
            val outputStream = FileOutputStream(outputPath)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            outputStream.close()
            println("图片已成功下载到 $outputPath")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
    }
}