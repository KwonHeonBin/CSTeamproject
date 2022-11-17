package com.KDB.exam

import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


class Stroke : Cloneable {
    var brush:Paint=Paint()
    var point=ArrayList<Pair<Float,Float>>()
    var maxDistPerPoint:Float=30f

    constructor(brush:Paint,distPerPoint:Float){
        this.brush=brush
        this.maxDistPerPoint=distPerPoint
        setupBrush()
    }
    constructor(){
        brush=Paint()
        maxDistPerPoint=30f
        setupBrush()
    }
    private fun setupBrush(){
        brush.isAntiAlias=true // set paintBrush
        brush.style=Paint.Style.STROKE
        brush.strokeJoin=Paint.Join.ROUND
        brush.strokeWidth=8f
        brush.strokeCap=Paint.Cap.ROUND
    }
    public override fun clone(): Stroke {
        val stroke = super.clone() as Stroke
        stroke.point = ArrayList<Pair<Float,Float>>().apply{ addAll(point) }
        return stroke
    }
}