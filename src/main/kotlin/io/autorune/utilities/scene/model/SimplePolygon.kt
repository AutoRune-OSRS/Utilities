/*
 * Copyright (c) 2019 Abex
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

import java.awt.*
import java.awt.geom.*
import java.util.*

/**
 * A simple list of vertices that can be append or prepended to
 */
class SimplePolygon constructor(var x : IntArray = IntArray(32), var y : IntArray = IntArray(32), var left : Int = 16, var right : Int = 15) : Shape
{

	constructor(x : IntArray, y : IntArray, length : Int) : this(x, y, 0, length - 1)

	fun pushLeft(xCoord : Int, yCoord : Int)
	{
		left--
		if(left < 0)
		{
			expandLeft(GROW)
		}
		x[left] = xCoord
		y[left] = yCoord
	}

	fun popLeft()
	{
		left++
	}

	fun expandLeft(grow : Int)
	{
		val nx = IntArray(x.size + grow)
		System.arraycopy(x, 0, nx, grow, x.size)
		x = nx
		val ny = IntArray(nx.size)
		System.arraycopy(y, 0, ny, grow, y.size)
		y = ny
		left += grow
		right += grow
	}

	fun pushRight(xCoord : Int, yCoord : Int)
	{
		right++
		if(right >= x.size)
		{
			expandRight(GROW)
		}
		x[right] = xCoord
		y[right] = yCoord
	}

	fun popRight()
	{
		right--
	}

	fun expandRight(grow : Int)
	{
		val nx = IntArray(x.size + grow)
		System.arraycopy(x, 0, nx, 0, x.size)
		x = nx
		val ny = IntArray(nx.size)
		System.arraycopy(y, 0, ny, 0, y.size)
		y = ny
	}

	fun getX(index : Int) : Int
	{
		return x[left + index]
	}

	fun getY(index : Int) : Int
	{
		return y[left + index]
	}

	fun size() : Int
	{
		return right - left + 1
	}

	fun toRuneLitePointList() : List<Point>
	{
		val out : MutableList<Point> = ArrayList(size())
		for(i in left..right)
		{
			out.add(Point(x[i], y[i]))
		}
		return out
	}

	fun copyTo(xDest : IntArray?, yDest : IntArray?, offset : Int)
	{
		System.arraycopy(x, left, xDest, offset, size())
		System.arraycopy(y, left, yDest, offset, size())
	}

	fun appendTo(other : SimplePolygon)
	{
		val size = size()
		if(size <= 0)
		{
			return
		}
		other.expandRight(size)
		copyTo(other.x, other.y, other.right + 1)
		other.right += size
	}

	fun reverse()
	{
		val half = size() / 2
		for(i in 0 until half)
		{
			val li = left + i
			val ri = right - i
			val tx = x[li]
			val ty = y[li]
			x[li] = x[ri]
			y[li] = y[ri]
			x[ri] = tx
			y[ri] = ty
		}
	}

	/**
	 * Clips the polygon with the passed convex polygon
	 */
	fun intersectWithConvex(convex : SimplePolygon)
	{ // Sutherland-Hodgman
		var tx = IntArray(size())
		var ty = IntArray(tx.size)
		var cx1 = convex.x[convex.right]
		var cy1 = convex.y[convex.right]
		for(ci in convex.left..convex.right)
		{
			if(size() < 3)
			{
				return
			}
			val tRight = right
			val tLeft = left
			val tmpX = x
			val tmpY = y
			x = tx
			y = ty
			left = 0
			right = -1
			tx = tmpX
			ty = tmpY
			val cx2 = convex.x[ci]
			val cy2 = convex.y[ci]
			var tx1 = tx[tRight]
			var ty1 = ty[tRight]
			for(ti in tLeft..tRight)
			{
				val tx2 = tx[ti]
				val ty2 = ty[ti]
				val p1 = (cx2 - cx1) * (ty1 - cy1) - (cy2 - cy1) * (tx1 - cx1)
				val p2 = (cx2 - cx1) * (ty2 - cy1) - (cy2 - cy1) * (tx2 - cx1)
				if(p1 < 0 && p2 < 0)
				{
					pushRight(tx2, ty2)
				}
				else if(p1 >= 0 != p2 >= 0)
				{
					val nota = cx1 * cy2 - cy1 * cx2.toLong()
					val clue = tx1 * ty2 - ty1 * tx2.toLong()
					val div = ((cx1 - cx2) * (ty1 - ty2) - (cy1 - cy2) * (tx1 - tx2)).toLong()
					pushRight(((nota * (tx1 - tx2) - (cx1 - cx2) * clue) / div).toInt(),
							((nota * (ty1 - ty2) - (cy1 - cy2) * clue) / div).toInt())
					if(p1 >= 0)
					{
						pushRight(tx2, ty2)
					}
				}
				tx1 = tx2
				ty1 = ty2
			}
			cx1 = cx2
			cy1 = cy2
		}
	}

	override fun getBounds() : Rectangle
	{
		var minX = Int.MAX_VALUE
		var minY = Int.MAX_VALUE
		var maxX = Int.MIN_VALUE
		var maxY = Int.MIN_VALUE
		for(i in left..right)
		{
			val xs = x[i]
			val ys = y[i]
			if(xs < minX)
			{
				minX = xs
			}
			if(xs > maxX)
			{
				maxX = xs
			}
			if(ys < minY)
			{
				minY = ys
			}
			if(ys > maxY)
			{
				maxY = ys
			}
		}
		return Rectangle(minX, minY, maxX - minX, maxY - minY)
	}

	override fun getBounds2D() : Rectangle2D
	{
		val b = bounds
		return Rectangle2D.Float(b.x.toFloat(), b.y.toFloat(), b.width.toFloat(), b.height.toFloat())
	}

	override fun contains(cx : Double, cy : Double) : Boolean
	{
		return if(size() < 3)
		{
			false
		}
		else crossings(cx, cy, false) and 1 != 0
	}

	private fun crossings(cx : Double, cy : Double, swap : Boolean) : Int
	{
		var collisions = 0
		var x = x
		var y = y
		if(swap)
		{
			y = this.x
			x = this.y
		}
		var x0 = x[right]
		var y0 = y[right]
		var x1 : Int
		var y1 : Int
		var i = left
		while(i <= right)
		{
			x1 = x[i]
			y1 = y[i]
			if(y0 == y1)
			{
				i++
				x0 = x1
				y0 = y1
				continue
			}
			val dy0 = y0.toDouble()
			val dy1 = y1.toDouble()
			if(cy <= dy0 == cy <= dy1)
			{
				i++
				x0 = x1
				y0 = y1
				continue
			}
			val dx0 = x0.toDouble()
			val dx1 = x1.toDouble()
			val left = cx < dx0
			if(left == cx < dx1)
			{
				if(!left)
				{
					collisions++
				}
				i++
				x0 = x1
				y0 = y1
				continue
			}
			if((dx1 - dx0) * (cy - dy0) - (cx - dx0) * (dy1 - dy0) > 0 == dy0 > dy1)
			{
				collisions++
			}
			i++
			x0 = x1
			y0 = y1
		}
		return collisions
	}

	override fun contains(p : Point2D) : Boolean
	{
		return contains(p.x, p.y)
	}

	override fun intersects(x0 : Double, y0 : Double, w : Double, h : Double) : Boolean
	{ // this is horribly inefficient, but I don't think it will be called anywhere
		val x1 = x0 + w
		val y1 = y0 + h
		return crossings(x0, y0, false) != crossings(x1, y0, false) // top
		       || crossings(x0, y1, false) != crossings(x1, y1, false) // bottom
		       || crossings(x0, y0, true) != crossings(x0, y1, true) // left
		       || crossings(x1, y0, true) != crossings(x1, y1, true) // right
	}

	override fun intersects(r : Rectangle2D) : Boolean
	{
		return intersects(r.x, r.y, r.width, r.height)
	}

	override fun contains(x : Double, y : Double, w : Double, h : Double) : Boolean
	{
		return if(!bounds.contains(x, y, w, h))
		{
			false
		}
		else !intersects(x, y, w, h)
	}

	override fun contains(r : Rectangle2D) : Boolean
	{
		return contains(r.x, r.y, r.width, r.height)
	}

	override fun getPathIterator(at : AffineTransform?) : PathIterator
	{
		return if(at == null) SimpleIterator() else TransformIterator(at)
	}

	override fun getPathIterator(at : AffineTransform, flatness : Double) : PathIterator
	{
		return getPathIterator(at)
	}

	private open inner class SimpleIterator : PathIterator
	{
		private var i = -1
		override fun getWindingRule() : Int
		{
			return PathIterator.WIND_EVEN_ODD
		}

		override fun isDone() : Boolean
		{
			return size() == 0 || i > right
		}

		override fun next()
		{
			if(i == -1)
			{
				i = left
			}
			else
			{
				i++
			}
		}

		override fun currentSegment(coords : FloatArray) : Int
		{
			if(i == -1)
			{
				coords[0] = x[right].toFloat()
				coords[1] = y[right].toFloat()
				return PathIterator.SEG_MOVETO
			}
			coords[0] = x[i].toFloat()
			coords[1] = y[i].toFloat()
			return PathIterator.SEG_LINETO
		}

		override fun currentSegment(coords : DoubleArray) : Int
		{
			if(i == -1)
			{
				coords[0] = x[right].toDouble()
				coords[1] = y[right].toDouble()
				return PathIterator.SEG_MOVETO
			}
			coords[0] = x[i].toDouble()
			coords[1] = y[i].toDouble()
			return PathIterator.SEG_LINETO
		}
	}

	private inner class TransformIterator internal constructor(private val transform : AffineTransform) : SimpleIterator()
	{
		override fun currentSegment(coords : FloatArray) : Int
		{
			val v = super.currentSegment(coords)
			transform.transform(coords, 0, coords, 0, 2)
			return v
		}

		override fun currentSegment(coords : DoubleArray) : Int
		{
			val v = super.currentSegment(coords)
			transform.transform(coords, 0, coords, 0, 2)
			return v
		}

	}

	companion object
	{
		private const val GROW = 16
	}

}