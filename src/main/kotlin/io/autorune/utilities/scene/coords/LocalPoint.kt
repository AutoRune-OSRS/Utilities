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
import kotlin.math.hypot

/**
 * A two-dimensional point in the local coordinate space.
 *
 *
 * Local points are immutable, however since the local coordinate space moves,
 * it is not safe to keep a LocalPoint after a loading zone.
 *
 *
 * The unit of a LocalPoint is 1/128th of a tile.
 */
class LocalPoint(
		/**
		 * X and Y axis coordinates.
		 */
		val x : Int, val y : Int)
{

	/**
	 * Gets the distance between this point and another.
	 *
	 * @param other other point
	 * @return the distance
	 */
	fun distanceTo(other : LocalPoint) : Int
	{
		return hypot(x - other.x.toDouble(), y - other.y.toDouble()).toInt()
	}

	/**
	 * Gets the x-axis coordinate in scene space (tiles).
	 *
	 * @return x-axis coordinate
	 */
	val sceneX : Int
		get() = x ushr Perspective.LOCAL_COORD_BITS

	/**
	 * Gets the y-axis coordinate in scene space (tiles).
	 *
	 * @return y-axis coordinate
	 */
	val sceneY : Int
		get() = y ushr Perspective.LOCAL_COORD_BITS

	companion object
	{
		/**
		 * Gets the local coordinate at the center of the passed tile.
		 *
		 * @param plane the client plane
		 * @param baseX the base x of the scene/region
		 * @param baseY the base y of the scene/region
		 * @param world  the passed tile
		 * @return coordinate if the tile is in the current scene, otherwise null
		 */
		fun fromWorld(plane: Int, baseX: Int, baseY: Int, world : WorldPoint) : LocalPoint?
		{
			return if(plane != world.plane)
			{
				null
			}
			else fromWorld(baseX, baseY, world.x, world.y)
		}

		/**
		 * Gets the local coordinate at the center of the passed tile.
		 *
		 * @param baseX the base x of the scene/region
		 * @param baseY the base y of the scene/region
		 * @param x      x-axis coordinate of the tile
		 * @param y      y-axis coordinate of the tile
		 * @return coordinate if the tile is in the current scene, otherwise null
		 */
		fun fromWorld(baseX: Int, baseY: Int, x : Int, y : Int) : LocalPoint?
		{
			if(!WorldPoint.isInScene(baseX, baseY, x, y))
			{
				return null
			}
			return fromScene(x - baseX, y - baseY)
		}

		/**
		 * Gets the coordinate at the center of the passed tile.
		 *
		 * @param x      x-axis coordinate of the tile in Scene coords
		 * @param y      y-axis coordinate of the tile in Scene coords
		 * @return true coordinate of the tile
		 */
		fun fromScene(x : Int, y : Int) : LocalPoint
		{
			return LocalPoint(
					(x shl Perspective.LOCAL_COORD_BITS) + (1 shl Perspective.LOCAL_COORD_BITS - 1) - 1,
					(y shl Perspective.LOCAL_COORD_BITS) + (1 shl Perspective.LOCAL_COORD_BITS - 1) - 1
			)
		}
	}

}