/*
 * Copyright (c) 2018 Abex
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.autorune.utilities.scene.coords

import io.autorune.utilities.scene.Perspective
import java.util.*

/**
 * A three-dimensional point representing the coordinate of a Tile.
 *
 *
 * WorldPoints are immutable. Methods that modify the properties create a new
 * instance.
 */
class WorldPoint(
		/**
		 * X-axis coordinate.
		 */
		val x : Int,
		/**
		 * Y-axis coordinate.
		 */
		val y : Int,
		/**
		 * The plane level of the Tile, also referred as z-axis coordinate.
		 *
		 * @see Client.getPlane
		 */
		val plane : Int)
{

	/**
	 * Offsets the x-axis coordinate by the passed value.
	 *
	 * @param dx the offset
	 * @return new instance
	 */
	fun dx(dx : Int) : WorldPoint
	{
		return WorldPoint(x + dx, y, plane)
	}

	/**
	 * Offsets the y-axis coordinate by the passed value.
	 *
	 * @param dy the offset
	 * @return new instance
	 */
	fun dy(dy : Int) : WorldPoint
	{
		return WorldPoint(x, y + dy, plane)
	}

	/**
	 * Offsets the plane by the passed value.
	 *
	 * @param dz the offset
	 * @return new instance
	 */
	fun dz(dz : Int) : WorldPoint
	{
		return WorldPoint(x, y, plane + dz)
	}

	/**
	 * Checks whether this tile is located in the current scene.
	 *
	 * @param client the client
	 * @return true if this tile is in the scene, false otherwise
	 */
	fun isInScene(plane: Int, baseX: Int, baseY: Int) : Boolean
	{
		return plane == this.plane && isInScene(baseX, baseY, x, y)
	}

	/**
	 * Gets the distance between this point and another.
	 *
	 *
	 * If the other point is not on the same plane, this method will return
	 * [Integer.MAX_VALUE]. If ignoring the plane is wanted, use the
	 * [.distanceTo2D] method.
	 *
	 * @param other other point
	 * @return the distance
	 */
	fun distanceTo(other : WorldPoint) : Int
	{
		return if(other.plane != plane)
		{
			Int.MAX_VALUE
		}
		else distanceTo2D(other)
	}

	/**
	 * Find the distance from this point to another point.
	 *
	 *
	 * This method disregards the plane value of the two tiles and returns
	 * the simple distance between the X-Z coordinate pairs.
	 *
	 * @param other other point
	 * @return the distance
	 */
	fun distanceTo2D(other : WorldPoint) : Int
	{
		return Math.max(Math.abs(x - other.x), Math.abs(y - other.y))
	}

	/**
	 * Gets the straight-line distance between this point and another.
	 *
	 *
	 * If the other point is not on the same plane, this method will return
	 * [Float.MAX_VALUE]. If ignoring the plane is wanted, use the
	 * [.distanceTo2DHypotenuse] method.
	 *
	 * @param other other point
	 * @return the straight-line distance
	 */
	fun distanceToHypotenuse(other : WorldPoint) : Float
	{
		return if(other.plane != plane)
		{
			Float.MAX_VALUE
		}
		else distanceTo2DHypotenuse(other)
	}

	/**
	 * Find the straight-line distance from this point to another point.
	 *
	 *
	 * This method disregards the plane value of the two tiles and returns
	 * the simple distance between the X-Z coordinate pairs.
	 *
	 * @param other other point
	 * @return the straight-line distance
	 */
	fun distanceTo2DHypotenuse(other : WorldPoint) : Float
	{
		return Math.hypot(x - other.x.toDouble(), y - other.y.toDouble()).toFloat()
	}

	/**
	 * Gets the ID of the region containing this tile.
	 *
	 * @return the region ID
	 */
	val regionID : Int
		get() = x shr 6 shl 8 or (y shr 6)

	/**
	 * Gets the X-axis coordinate of the region coordinate
	 */
	val regionX : Int
		get() = getRegionOffset(x)

	/**
	 * Gets the Y-axis coordinate of the region coordinate
	 */
	val regionY : Int
		get() = getRegionOffset(y)

	companion object
	{
		/**
		 * The width and length of a chunk (8x8 tiles).
		 */
		const val CHUNK_SIZE = 8
		/**
		 * The width and length of a map region (64x64 tiles).
		 */
		const val REGION_SIZE = 64

		/**
		 * Checks whether a tile is located in the current scene.
		 *
		 * @param client the client
		 * @param x      the tiles x coordinate
		 * @param y      the tiles y coordinate
		 * @return true if the tile is in the scene, false otherwise
		 */
		fun isInScene(baseX: Int, baseY: Int, x : Int, y : Int) : Boolean
		{
			val maxX : Int = baseX + Perspective.SCENE_SIZE
			val maxY : Int = baseY + Perspective.SCENE_SIZE
			return x >= baseX && x < maxX && y >= baseY && y < maxY
		}

		/**
		 * Gets the coordinate of the tile that contains the passed local point.
		 *
		 * @param client the client
		 * @param local  the local coordinate
		 * @return the tile coordinate containing the local point
		 */
		fun fromLocal(baseX: Int, baseY: Int, plane: Int, local : LocalPoint) : WorldPoint
		{
			return fromLocal(baseX, baseY, local.x, local.y, plane)
		}

		/**
		 * Gets the coordinate of the tile that contains the passed local point.
		 *
		 * @param client the client
		 * @param x      the local x-axis coordinate
		 * @param y      the local x-axis coordinate
		 * @param plane  the plane
		 * @return the tile coordinate containing the local point
		 */
		fun fromLocal(baseX: Int, baseY: Int, x : Int, y : Int, plane : Int) : WorldPoint
		{
			return WorldPoint(
					(x ushr Perspective.LOCAL_COORD_BITS) + baseX,
					(y ushr Perspective.LOCAL_COORD_BITS) + baseY,
					plane
			)
		}

		/**
		 * Gets the coordinate of the tile that contains the passed local point,
		 * accounting for instances.
		 *
		 * @param client     the client
		 * @param localPoint the local coordinate
		 * @return the tile coordinate containing the local point
		 */
		fun fromLocalInstance(baseX: Int, baseY: Int, plane: Int, localPoint : LocalPoint) : WorldPoint?
		{
			return if(true) //client.getIsInInstance())
			{ // get position in the scene
				val sceneX = localPoint.sceneX
				val sceneY = localPoint.sceneY
				// get chunk from scene
				val chunkX = sceneX / CHUNK_SIZE
				val chunkY = sceneY / CHUNK_SIZE
				if(chunkX >= 13 || chunkY >= 13)
				{
					return null
				}
				// get the template chunk for the chunk
				val instanceTemplateChunks = Array(0) { Array(0) { IntArray(0) } } //client.getInstanceChunkTemplates();
				val templateChunk = instanceTemplateChunks[plane][chunkX][chunkY]
				val rotation = templateChunk shr 1 and 0x3
				val templateChunkY = (templateChunk shr 3 and 0x7FF) * CHUNK_SIZE
				val templateChunkX = (templateChunk shr 14 and 0x3FF) * CHUNK_SIZE
				val plane = templateChunk shr 24 and 0x3
				// calculate world point of the template
				val x = templateChunkX + (sceneX and CHUNK_SIZE - 1)
				val y = templateChunkY + (sceneY and CHUNK_SIZE - 1)
				// create and rotate point back to 0, to match with template
				rotate(WorldPoint(x, y, plane), 4 - rotation)
			}
			else
			{
				fromLocal(baseX, baseY, plane, localPoint)
			}
		}

		/**
		 * Get occurrences of a tile on the scene, accounting for instances. There may be
		 * more than one if the same template chunk occurs more than once on the scene.
		 *
		 * @param client
		 * @param worldPoint
		 * @return
		 */
		fun toLocalInstance(baseX : Int, baseY : Int, inInstance: Boolean, instanceTemplateChunks: Array<Array<IntArray>>, worldPoint : WorldPoint) : Collection<WorldPoint>
		{
			if(!inInstance)
			{
				return setOf(worldPoint)
			}
			// find instance chunks using the template point. there might be more than one.
			val worldPoints : MutableList<WorldPoint> = ArrayList()
			val z = worldPoint.plane
			for(x in instanceTemplateChunks[z].indices)
			{
				for(y in instanceTemplateChunks[z][x].indices)
				{
					val chunkData = instanceTemplateChunks[z][x][y]
					val rotation = chunkData shr 1 and 0x3
					val templateChunkY = (chunkData shr 3 and 0x7FF) * CHUNK_SIZE
					val templateChunkX = (chunkData shr 14 and 0x3FF) * CHUNK_SIZE
					if(worldPoint.x >= templateChunkX && worldPoint.x < templateChunkX + CHUNK_SIZE && worldPoint.y >= templateChunkY && worldPoint.y < templateChunkY + CHUNK_SIZE)
					{
						var p = WorldPoint(baseX + x * CHUNK_SIZE + (worldPoint.x and CHUNK_SIZE - 1),
								baseY + y * CHUNK_SIZE + (worldPoint.y and CHUNK_SIZE - 1),
								worldPoint.plane)
						p = rotate(p, rotation)
						worldPoints.add(p)
					}
				}
			}
			return worldPoints
		}

		/**
		 * Rotate the coordinates in the chunk according to chunk rotation
		 *
		 * @param point    point
		 * @param rotation rotation
		 * @return world point
		 */
		private fun rotate(point : WorldPoint, rotation : Int) : WorldPoint
		{
			val chunkX = point.x and (CHUNK_SIZE - 1).inv()
			val chunkY = point.y and (CHUNK_SIZE - 1).inv()
			val x = point.x and CHUNK_SIZE - 1
			val y = point.y and CHUNK_SIZE - 1
			when(rotation)
			{
				1 -> return WorldPoint(chunkX + y, chunkY + (CHUNK_SIZE - 1 - x), point.plane)
				2 -> return WorldPoint(chunkX + (CHUNK_SIZE - 1 - x), chunkY + (CHUNK_SIZE - 1 - y), point.plane)
				3 -> return WorldPoint(chunkX + (CHUNK_SIZE - 1 - y), chunkY + x, point.plane)
			}
			return point
		}

		/**
		 * Converts the passed scene coordinates to a world space
		 */
		fun fromScene(baseX : Int, baseY : Int, x : Int, y : Int, plane : Int) : WorldPoint
		{
			return WorldPoint(
					x + baseX,
					y + baseY,
					plane
			)
		}

		/**
		 * Checks if user in within certain zone specified by upper and lower bound
		 *
		 * @param lowerBound
		 * @param upperBound
		 * @param userLocation
		 * @return
		 */
		fun isInZone(lowerBound : WorldPoint, upperBound : WorldPoint, userLocation : WorldPoint) : Boolean
		{
			return userLocation.x >= lowerBound.x && userLocation.x <= upperBound.x && userLocation.y >= lowerBound.y && userLocation.y <= upperBound.y && userLocation.plane >= lowerBound.plane && userLocation.plane <= upperBound.plane
		}

		/**
		 * Converts the passed region ID and coordinates to a world coordinate
		 */
		fun fromRegion(regionId : Int, regionX : Int, regionY : Int, plane : Int) : WorldPoint
		{
			return WorldPoint(
					(regionId ushr 8 shl 6) + regionX,
					(regionId and 0xff shl 6) + regionY,
					plane)
		}

		private fun getRegionOffset(position : Int) : Int
		{
			return position and REGION_SIZE - 1
		}
	}

}