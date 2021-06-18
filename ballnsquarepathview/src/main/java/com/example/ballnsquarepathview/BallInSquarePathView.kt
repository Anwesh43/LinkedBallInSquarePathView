package com.example.ballnsquarepathview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Canvas
import android.content.Context
import android.app.Activity

val colors : Array<Int> = arrayOf(
    "#f44336",
    "#01579B",
    "#00C853",
    "#FFC107",
    "#2962FF"
).map {
    Color.parseColor(it)
}.toTypedArray()
val lines : Int = 4
val parts : Int = 4 + lines
val scGap : Float = 0.02f / parts
val delay : Long = 20
val deg : Float = 90f
val strokeFactor : Float = 90f
val rFactor : Float = 18.2f
val boxFactor : Float = 3.4f
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawBallInSquare(scale : Float, w : Float, h : Float, paint : Paint) {
    val size : Float = Math.min(w, h) / boxFactor
    val r : Float = Math.min(w, h) / rFactor
    val sc1 : Float = scale.divideScale(0, parts)
    val sc2 : Float = scale.divideScale(1, parts)
    val sc7 : Float = scale.divideScale(6, parts)
    val sc8 : Float = scale.divideScale(7, parts)
    val a : Float = size * (sc1 - sc8)
    save()
    translate(w / 2, h / 2)
    paint.style = Paint.Style.STROKE
    drawRect(RectF(-a / 2, -a / 2, a / 2, a /2), paint)
    paint.style = Paint.Style.FILL
    var rot : Float = 0f
    var y : Float = -size / 2 + r
    for (j in 0..(lines - 1)) {
        val scj : Float = scale.divideScale(2 + j, parts)
        rot += deg * Math.floor(scj.toDouble()).toFloat()
        y += (size - 2 * r) * scj
    }
    save()
    rotate(rot)
    drawCircle(size / 2 - r, y, r * (sc2 - sc7), paint)
    restore()
    restore()
}

fun Canvas.drawBISPNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawBallInSquare(scale, w, h, paint)
}

class BallInSquarePathView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BISPNode(var i : Int, val state : State = State()) {

        private var next : BISPNode? = null
        private var prev : BISPNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = BISPNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBISPNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BISPNode {
            var curr : BISPNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BallInSquarePath(var i : Int) {

        private var curr : BISPNode = BISPNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BallInSquarePathView) {

        private val bisp : BallInSquarePath = BallInSquarePath(0)
        private val animator : Animator = Animator(view)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            bisp.draw(canvas, paint)
            animator.animate {
                bisp.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bisp.startUpdating {
                animator.start()
            }
        }
    }

    companion object {
        fun create(activity : Activity) : BallInSquarePathView {
            val view : BallInSquarePathView = BallInSquarePathView(activity)
            activity.setContentView(view)
            return view
        }
    }
}