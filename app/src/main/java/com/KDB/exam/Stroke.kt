package com.KDB.exam

import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import kotlin.math.*


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
//    if(yPoint==points[j].second){
//        Log.d("asd","ss")
//        crossPoint+=1
//        if(j==0||j==points.size-1){
//            val dif2=(points[1].second-points[points.size-2].second)/(points[1].first-points[points.size-2].first)
//            val yPoint2=dif2*(stroke.point[i].first-points[1].first)+points[1].second
//            if(yPoint2>= min(points[1].second,points[points.size-2].second) &&
//                yPoint2<= max(points[1].second,points[points.size-2].second)
//            ){
//                //Log.d("asd","way: 1   "+crossPoint.toString())
//                crossPoint+=1
//            }
//            //else{Log.d("asd","way: 1  -> fail  "+crossPoint.toString())}
//        }
//        else{
//            val dif2=(points[j+1].second-points[j-1].second)/(points[j+1].first-points[j-1].first)
//            val yPoint2=dif2*(stroke.point[i].first-points[j+1].first)+points[j+1].second
//            if(yPoint2>= min(points[j+1].second,points[j-1].second) &&
//                yPoint2<= max(points[j+1].second,points[j-1].second)
//            ){
//                crossPoint+=1
//                //Log.d("asd","way: 2   "+crossPoint.toString()+"   YP2:  "+yPoint2.toString())
//            }
//            //else{Log.d("asd","way: 2  -> fail  "+crossPoint.toString())}
//        }
//    }
}