import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashSet
import kotlin.concurrent.thread

fun main() {
    val link = readLine()
    val map = System.getenv()
    val userName = map["USER"]
    val mac_path = "/Users/${userName}/Pictures/lofter爬虫/"
    val windows_path = "%SystemDrive%\\Users\\%USERPROFILE%\\Downloads\\lofter爬虫"
    val path = when{
        checkOS() == OS.WINDOWS -> windows_path
        checkOS() == OS.MAC ->mac_path
        else -> ""
    }
    if(!checkDir(userName,path)){File(path).mkdir()}
    if(!link.isNullOrBlank()){
        loadData(link.toString(),userName,path)
    }
    for(items in map){
        println(items)}

}
enum class OS {
    WINDOWS, MAC
}
fun checkOS():OS?{
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    return when {
        os.contains("win") -> {
            OS.WINDOWS
        }
        os.contains("mac") -> {
            OS.MAC
        }
        else -> null
    }
}
fun checkDir(userName: String?,path:String)= File(path).isDirectory

fun loadData(URL:String,userName:String?,path: String){
    val resultSet = HashSet<String>()
    thread {
        val url = URL(URL)
        val connection = url.openConnection() as HttpURLConnection
        try {
            val response = StringBuilder()
            connection.apply {
                setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
                setRequestProperty("Referer",URL)
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
            }
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
                download(items,userName,path)
            }

        }catch (e:Exception){
            e.printStackTrace()
        }finally {
            connection.disconnect()
        }
    }
}
fun download(imageURL:String,userName: String?,path: String) {
    val imageUrl = imageURL
    val url = URL(imageUrl)
    thread {
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.apply {
                setRequestProperty("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36")
                setRequestProperty("Referer",imageURL)
                setRequestProperty("Origin", imageURL)
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
            }
            val inputStream: InputStream = connection.inputStream
            val result: Regex = Regex(pattern = "/([^/]+)\$")
            val outputPath = "${path}"+"${result.find(imageURL)?.value}"
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