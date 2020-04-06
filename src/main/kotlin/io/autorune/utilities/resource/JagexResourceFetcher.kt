package io.autorune.utilities.resource

import org.jsoup.Jsoup
import java.util.regex.Pattern

object JagexResourceFetcher
{

    fun getJagexResource(members: Boolean): JagexResource {
        val bestWorld = WorldFetcher.getWorld(members)

        return generateResource(bestWorld)
    }

    private fun generateResource(world: World) : JagexResource
    {

        val address = world.address

        val connection = Jsoup.connect(address)

        val content = connection.get().html()

        val parameterPattern = Pattern.compile("<param name=\"([^\\s]+)\"\\s+value=\"([^>]*)\">")

        val matcher = parameterPattern.matcher(content)

        val parameters = mutableMapOf<String, String>()

        while (matcher.find())
        {
            parameters[matcher.group(1)] = matcher.group(2)
        }

        val gamePackHash = content.split("archive=gamepack_")[1].split(".jar")[0].toLong()

        val gamePackUrl = "$address/gamepack_$gamePackHash.jar"

        val gamePack = GamePack(gamePackHash, gamePackUrl)

        return JagexResource(address, content, gamePack, parameters)

    }

}