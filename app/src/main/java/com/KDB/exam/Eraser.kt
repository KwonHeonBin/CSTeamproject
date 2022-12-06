package com.KDB.exam

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Eraser {
    var radius:Float =5f
    var pos=Pair(-100f,-100f)
    var mode:Int=0      // 0-> stroke  1-> area
    var commonBrush:Paint=Paint().apply {
        color= Color.BLACK
        strokeWidth=3f
        style=Paint.Style.STROKE
    }
    constructor(rad:Float,pos:Pair<Float,Float>,mod:Int=0){
        radius=rad
        this.pos=pos
        mode=mod
    }
    fun setRad(rad: Float){
        radius=rad
    }
    fun erase(point:Pair<Float,Float>) {
        for (st in canvasView.pathList) {
            for (i in st.point) {
                var distance = canvasView.getDst(point,i)
                if (distance <= radius) {
                    when (mode) {
                        0 -> {    // stroke delete
                            canvasView.saveCanvas()
                            canvasView.pathList.remove(st)
                            return
                        }
                        1 -> {    // point delete
                            var index=st.point.indexOf(i)
                            if(index in 1..st.point.size-2){
                                var path1=Stroke()
                                var path2=Stroke()
                                path1.brush=st.brush
                                path2.brush=st.brush
                                for (i in 0 until index){
                                    path1.point.add(st.point[i])
                                }
                                for (i in index+1 until st.point.size){
                                    path2.point.add(st.point[i])
                                }
                                canvasView.pathList.add(path1)
                                canvasView.pathList.add(path2)
                                canvasView.pathList.remove(st)
                                return
                            }
                            st.point.remove(i)
                            if(st.point.isEmpty()){ canvasView.pathList.remove(st)}
                            return
                        }
                    }
                }
            }
        }
    }
    fun drawEraser(canvas:Canvas) {
        canvas.drawCircle(pos.first, pos.second, radius,commonBrush)
    }
}