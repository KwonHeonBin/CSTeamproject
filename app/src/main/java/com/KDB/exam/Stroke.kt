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
    fun interploation(){
        var i=0
        while(i<point.size-1){
            //Log.d("asd","size: "+point.size.toString()+"    "+i.toString())
            var diff= sqrt(
                abs(point[i].first-point[i+1].first).pow(2)
                    +abs(point[i].second-point[i+1].second).pow(2))
            //Log.d("asd","before:  "+i.toString()+"   "+diff.toString())
            if(diff>maxDistPerPoint){
                var pos=Pair((point[i].first+point[i+1].first)/2,(point[i].second+point[i+1].second)/2)
                point.add(i+1,pos)
                continue
            }
            i++
        }
    }
}