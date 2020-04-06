package io.autorune.utilities.resource

import io.autorune.utilities.resource.utils.RSByteBufferUtils
import org.jsoup.Jsoup
import java.net.URL
import java.nio.ByteBuffer

object WorldFetcher {

    /**
     * Gets the first and best world to use.
     * @param members Whether or not to include members worlds.
     * @return World The [World] instance.
     */
    fun getWorld(members: Boolean) : World
    {
        //todo also to use random world, or best world.

        //todo first use, unknown membership?

        val bestWorld = getBestWorld(members)

        return getWorldList().first() { it.id == bestWorld }

    }

    /**
     * Retrieves a world list from JaGeX's [https://www.runescape.com/g=oldscape/slr.ws?order=WMLPA].
     * @return The list of [World]'s and their properties.
     */
    private fun getWorldList(): MutableList<World> {

        val worldListConnection = URL("http://www.runescape.com/g=oldscape/slr.ws?order=WMLPA").openConnection()

        val worldListContent = ByteBuffer.wrap(worldListConnection.getInputStream().readAllBytes())

        val httpPort = worldListContent.int

        val worldCount = worldListContent.short

        val worldList = mutableListOf<World>()

        for(i in 1..worldCount) {

            val world = worldListContent.short.toInt()

            val members = worldListContent.int != 0

            val address = "http://"+RSByteBufferUtils.readRS2String(worldListContent)

            val activityType = RSByteBufferUtils.readRS2String(worldListContent)

            val countryFlag = worldListContent.get().toInt()

            val playerCount = worldListContent.short.toInt()

            worldList.add(World(world, members, address, activityType, countryFlag, playerCount))

        }

        return worldList

    }

    /**
     * Retrieves the best fit world for the current ip_address.
     * @param members Whether or not to use a members world.
     * @return The best world id.
     */
    private fun getBestWorld(members: Boolean): Int {

        val document = Jsoup.connect("http://oldschool.runescape.com/slu?order=LWMPA").get()

        val html = document.html()

        if (members) {
            return html.split("game?world=")[2].split("\"")[0].toInt()
        }
        return html.split("game?world=")[1].split("\"")[0].toInt()
    }

}