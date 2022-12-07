package com.KDB.exam

import android.util.Log

class SelectedBox:Box {


    var checkedStroke=ArrayList<Stroke>()
    private var scaleOfPoints=ArrayList<ArrayList<Pair<Float,Float>>>()

    constructor(upperX:Float,upperY:Float,underX:Float,underY:Float){
        setPoint(upperX,upperY,underX,underY)
    }

    fun setStrokeScale(){
        scaleOfPoints.clear()
        for (i in checkedStroke){
            val strokeBox=ArrayList<Pair<Float,Float>>()
            for (j in i.point){
                val scaleX=(j.first-upperLPoint.first)/(upperRPoint.first-upperLPoint.first)
                val scaleY=(j.second-upperRPoint.second)/(underRPoint.second-upperRPoint.second)
                strokeBox.add(Pair(scaleX,scaleY))
            }
            scaleOfPoints.add(strokeBox)
        }

    }
    fun applyScale(){
        for (i in 0 until checkedStroke.size){
            for (j in 0 until checkedStroke[i].point.size){
                checkedStroke[i].point[j]=Pair(((upperRPoint.first-upperLPoint.first)*scaleOfPoints[i][j].first)+upperLPoint.first,
                    ((underRPoint.second-upperRPoint.second)*scaleOfPoints[i][j].second)+upperRPoint.second)
            }
        }
    }

    override fun setBox(){
        setPoint(checkedStroke.minOf { it.point.minOf { it.first }},
                 checkedStroke.minOf { it.point.minOf { it.second }},
                 checkedStroke.maxOf{it.point.maxOf { it.first }},
                 checkedStroke.maxOf{it.point.maxOf { it.second }})
        setStrokeScale()
    }

    override fun clearBox(){
        super.clearBox()
        checkedStroke.clear()
        scaleOfPoints.clear()

    }
}