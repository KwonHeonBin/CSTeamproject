package com.example.cud.ui.draw

import android.graphics.Color
import android.graphics.Paint
import kotlin.collections.ArrayList


class Stroke : Cloneable {
    var brush:Paint=Paint()
    var point=ArrayList<Pair<Float,Float>>()
    var maxDistPerPoint:Float=30f
    var id:Int=0

    constructor(point:ArrayList<Pair<Float,Float>>,id:Int,width:Float=8f, color:Int = Color.BLACK){
        this.point=point
        this.id=id
        setupBrush()
        brush.strokeWidth=width
        brush.color=color
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