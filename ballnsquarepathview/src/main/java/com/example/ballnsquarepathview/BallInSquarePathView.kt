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
val rFactor : Float = 11.2f
val boxFactor : Float = 3.4f
