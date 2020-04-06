package io.autorune.utilities.jar

import io.autorune.utilities.preferences.SystemPreferences
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.nio.file.*
import java.util.jar.JarInputStream

object JarDownloader {

    fun fetchJar(uri: String): Pair<Int, Path> {

        val url = URL(uri)

        val hashCode = getJarHash(url)

        val referer = url.toExternalForm()

        val connection = url.openConnection()

        addRequestHeaders(connection, referer)

        val path = downloadJar(connection, uri)

        return Pair(hashCode, path)

    }

    private fun downloadJar(connection: URLConnection, uri: String) : Path {

        var tempFile = SystemPreferences.getVanillaGamepackDirectory().resolve(uri.split("_")[1])

        val path = Files.createFile(tempFile)

        val inStream = BufferedInputStream(connection.getInputStream())
        val outStream = BufferedOutputStream( FileOutputStream(path.toFile()), 1024)
        val data = ByteArray(1024)
        var x: Int
        while (true) {
            x = inStream.read(data, 0, 1024)
            if (x < 0)
                break
            outStream.write(data, 0, x)
        }
        outStream.close()
        inStream.close()

        return path

    }

    private fun getJarHash(location: URL): Int {
        val stream = JarInputStream(location.openStream())
        val hashCode = stream.manifest.hashCode()
        stream.close()
        return hashCode
    }

    private fun addRequestHeaders(connection: URLConnection, referer: String) {
        connection.addRequestProperty("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5")
        connection.addRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7")
        connection.addRequestProperty("Accept-Encoding", "gzip,deflate")
        connection.addRequestProperty("Accept-Language", "en-gb,en;q=0.5")
        connection.addRequestProperty("Connection", "keep-alive")
        connection.addRequestProperty("Host", "www.runescape.com")
        connection.addRequestProperty("Keep-Alive", "300")
        connection.addRequestProperty("Referer", referer)
        connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.8.0.6) Gecko/20060728 Firefox/1.5.0.6")
    }

}