package io.autorune.utilities.scene

import io.autorune.utilities.scene.coords.LocalPoint
import kotlin.experimental.and

object Perspective
{

	private const val UNIT: Double = Math.PI / 1024.0 // How much of the circle each unit of SINE/COSINE is

	private val SINE = IntArray(2048) // sine angles for each of the 2048 units, * 65536 and stored as an int

	private val COSINE = IntArray(2048) // cosine

	const val LOCAL_COORD_BITS = 7
	const val LOCAL_TILE_SIZE = 1 shl LOCAL_COORD_BITS // 128 - size of a tile in local coordinates

	const val LOCAL_HALF_TILE_SIZE = LOCAL_TILE_SIZE / 2

	const val SCENE_SIZE : Int = 104// in tiles

	const val MAX_Z = 4

	const val TILE_FLAG_BRIDGE = 2

	init
	{
		for(i in 0..2047)
		{
			SINE[i] = (65536.0 * Math.sin(i.toDouble() * UNIT)).toInt()
			COSINE[i] = (65536.0 * Math.cos(i.toDouble() * UNIT)).toInt()
		}
	}

	fun modelToCanvas(cameraPitch: Int, cameraYaw: Int, cameraX: Int, cameraY: Int, cameraZ: Int,
	                  viewportWidth: Int, viewportHeight: Int, viewportOffsetX: Int, viewportOffsetY: Int, viewportZoom: Int,
	                  end : Int, x3dCenter : Int, y3dCenter : Int, z3dCenter : Int, rotate : Int, x3d : IntArray, y3d : IntArray, z3d : IntArray, x2d : IntArray, y2d : IntArray)
	{

		val cameraPitch : Int = cameraPitch

		val cameraYaw : Int = cameraYaw

		val pitchSin : Int = SINE[cameraPitch]

		val pitchCos : Int = COSINE[cameraPitch]

		val yawSin : Int = SINE[cameraYaw]

		val yawCos : Int = COSINE[cameraYaw]

		val rotateSin : Int = SINE[rotate]

		val rotateCos : Int = COSINE[rotate]

		val cx : Int = x3dCenter - cameraX

		val cy : Int = y3dCenter - cameraY

		val cz : Int = z3dCenter - cameraZ

		val viewportXMiddle : Int = viewportWidth / 2

		val viewportYMiddle : Int = viewportHeight / 2

		val viewportXOffset : Int = viewportOffsetX

		val viewportYOffset : Int = viewportOffsetY

		val zoom3d : Int = viewportZoom

		var vCount = end

		if (vCount > x3d.size || vCount > y3d.size || vCount > z3d.size)
			vCount = minOf(x3d.size, y3d.size, z3d.size)

		for(i in 0 until vCount)
		{

			var x = x3d[i]
			var y = y3d[i]
			var z = z3d[i]
			if(rotate != 0)
			{
				val x0 = x
				x = x0 * rotateCos + y * rotateSin shr 16
				y = y * rotateCos - x0 * rotateSin shr 16
			}
			x += cx
			y += cy
			z += cz
			val x1 = x * yawCos + y * yawSin shr 16
			val y1 = y * yawCos - x * yawSin shr 16
			val y2 = z * pitchCos - y1 * pitchSin shr 16
			val z1 = y1 * pitchCos + z * pitchSin shr 16
			var viewX : Int
			var viewY : Int
			if(z1 < 50)
			{
				viewX = Int.MIN_VALUE
				viewY = Int.MIN_VALUE
			}
			else
			{
				viewX = viewportXMiddle + x1 * zoom3d / z1 + viewportXOffset
				viewY = viewportYMiddle + y2 * zoom3d / z1 + viewportYOffset
			}
			x2d[i] = viewX
			y2d[i] = viewY
		}
	}

	/**
	 * Calculates the above ground height of a tile point.
	 *
	 * @param client the game client
	 * @param point the local ground coordinate
	 * @param plane the client plane/ground level
	 * @return the offset from the ground of the tile
	 */
	fun fetchTileHeightFromLocal(tileHeights: Array<Array<IntArray>>, tileSettings: Array<Array<ByteArray>>, point : LocalPoint, plane : Int) : Int
	{
		val sceneX : Int = point.sceneX
		val sceneY : Int = point.sceneY
		val tileHeights : Array<Array<IntArray>> = tileHeights
		if(sceneX >= 0 && sceneY >= 0 && sceneX < SCENE_SIZE && sceneY < SCENE_SIZE && plane >= 0 && plane < tileHeights.size)
		{
			val tileSettings : Array<Array<ByteArray>> = tileSettings
			var z1 = plane
			if(plane < MAX_Z - 1 && (tileSettings[1][sceneX][sceneY] and TILE_FLAG_BRIDGE.toByte()) == TILE_FLAG_BRIDGE.toByte())
			{
				z1 = plane + 1
			}
			val x : Int = point.x and LOCAL_TILE_SIZE - 1
			val y : Int = point.y and LOCAL_TILE_SIZE - 1
			val var8 = x * tileHeights[z1][sceneX + 1][sceneY] + (LOCAL_TILE_SIZE - x) * tileHeights[z1][sceneX][sceneY] shr LOCAL_COORD_BITS
			val var9 = tileHeights[z1][sceneX][sceneY + 1] * (LOCAL_TILE_SIZE - x) + x * tileHeights[z1][sceneX + 1][sceneY + 1] shr LOCAL_COORD_BITS
			return (LOCAL_TILE_SIZE - y) * var8 + y * var9 shr LOCAL_COORD_BITS
		}
		return 0
	}

}