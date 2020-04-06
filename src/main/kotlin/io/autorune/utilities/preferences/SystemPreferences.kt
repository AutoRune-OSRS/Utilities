package io.autorune.utilities.preferences

import java.io.File
import java.nio.file.*

object SystemPreferences
{

	fun getAutoruneDirectory() : Path
	{

		val userHome = Paths.get(System.getProperty("user.home"))

		val autoruneDirectory = userHome.resolve(".autorune")

		autoruneDirectory.toFile().mkdirs()

		return autoruneDirectory

	}

	fun getGamepackDirectory() : Path
	{

		val autoruneDirectory = getAutoruneDirectory()

		val gamepackDirectory = autoruneDirectory.resolve("gamepacks")

		gamepackDirectory.toFile().mkdirs()

		return gamepackDirectory

	}

	fun getVanillaGamepackDirectory() : Path
	{
		val gamepackDirectory = getGamepackDirectory()

		val vanillaGamepackDirectory = gamepackDirectory.resolve("vanilla")

		vanillaGamepackDirectory.toFile().mkdirs()

		return vanillaGamepackDirectory
	}

	fun getScriptsDirectory() : Path
	{

		val autoruneDirectory = getAutoruneDirectory()

		val scriptsDirectory = autoruneDirectory.resolve("scripts")

		scriptsDirectory.toFile().mkdirs()

		return scriptsDirectory

	}

	fun getCharacteristicDirectory() : Path
	{

		val autoruneDirectory = getAutoruneDirectory()

		val characteristics = autoruneDirectory.resolve("characteristics")

		characteristics.toFile().mkdirs()

		return characteristics

	}

	fun getResourcesDirectory() : Path
	{

		val autoruneDirectory = getAutoruneDirectory()

		val resourcesDirectory = autoruneDirectory.resolve("resources")

		resourcesDirectory.toFile().mkdirs()

		return resourcesDirectory

	}

	fun getInjectedJarLocation() : Path
	{

		val gamepackDirectory = getGamepackDirectory()

		return gamepackDirectory.resolve("injected.jar")

	}

}