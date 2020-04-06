/*
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
package io.autorune.utilities.scene.model

import java.awt.Point

/**
 * Provides utility methods for computing the convex hull of a list of
 * *n* points.
 *
 *
 * The implementation uses the Jarvis march algorithm and runs in O(nh)
 * time in the worst case, where n is the number of points and h the
 * number of points on the convex hull.
 */
object Jarvis
{
	/**
	 * Computes and returns the convex hull of the passed points.
	 *
	 *
	 * The size of the list must be at least 3, otherwise this method will
	 * return null.
	 *
	 * @param points list of points
	 * @return list containing the points part of the convex hull
	 */
	@Deprecated("") fun convexHull(points : List<Point>) : List<Point>?
	{
		val xs = IntArray(points.size)
		val ys = IntArray(xs.size)
		for(i in xs.indices)
		{
			val p = points[i]
			xs[i] = p.getX().toInt()
			ys[i] = p.getY().toInt()
		}
		val poly = convexHull(xs, ys) ?: return null
		return poly.toRuneLitePointList()
	}

	/**
	 * Computes and returns the convex hull of the passed points.
	 *
	 *
	 * The size of the list must be at least 3, otherwise this method will
	 * return null.
	 *
	 * @return a shape the points part of the convex hull
	 */
	fun convexHull(xs : IntArray, ys : IntArray) : SimplePolygon?
	{
		var length = xs.size
		// remove any invalid entries
		run {
			var i = 0
			var offset = 0
			while(i < length)
			{
				if(xs[i] == Int.MIN_VALUE)
				{
					offset++
					i++
					break
				}
				i++
			}
			while(i < length)
			{
				if(xs[i] == Int.MIN_VALUE)
				{
					offset++
					i++
					continue
				}
				xs[i - offset] = xs[i]
				ys[i - offset] = ys[i]
				i++
			}
			length -= offset
		}
		if(length < 3)
		{
			return null
		}
		// find the left most point
		val left = findLeftMost(xs, ys, length)
		// current point we are on
		var current = left
		val out = SimplePolygon(IntArray(16), IntArray(16), 0)
		do
		{
			val cx = xs[current]
			val cy = ys[current]
			out.pushRight(cx, cy)
			if(out.size() > length)
			{
				return null
			}
			// the next point - all points are to the right of the
// line between current and next
			var next = 0
			var nx = xs[next]
			var ny = ys[next]
			for(i in 1 until length)
			{
				val cp = crossProduct(cx, cy, xs[i], ys[i], nx, ny)
				if(cp > 0 || cp == 0L && square(cx - xs[i]) + square(cy - ys[i]) > square(cx - nx) + square(cy - ny))
				{
					next = i
					nx = xs[next]
					ny = ys[next]
				}
			}
			current = next
		}
		while(current != left)
		return out
	}

	private fun square(x : Int) : Int
	{
		return x * x
	}

	private fun findLeftMost(xs : IntArray, ys : IntArray, length : Int) : Int
	{
		var idx = 0
		var x = xs[idx]
		var y = ys[idx]
		for(i in 1 until length)
		{
			val ix = xs[i]
			if(ix < x || ix == x && ys[i] < y)
			{
				idx = i
				x = xs[idx]
				y = ys[idx]
			}
		}
		return idx
	}

	private fun crossProduct(px : Int, py : Int, qx : Int, qy : Int, rx : Int, ry : Int) : Long
	{
		return ((qy - py).toLong() * (rx - qx)
		        - (qx - px).toLong() * (ry - qy))
	}
}