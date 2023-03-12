package com.KDB.exam

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.KDB.exam.CanvasManager.Companion.getDst
import com.KDB.exam.CanvasManager.Companion.pathList
import com.KDB.exam.CanvasManager.Companion.saveCanvas

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
    fun setRad(rad: Float){// 크기 설정
        radius=rad
    }
    fun erase(point:Pair<Float,Float>) {// 지우기
        for (st in pathList) {
            for (i in st.point) {
                var distance = getDst(point,i)
                if (distance <= radius) {
                    when (mode) {
                        0 -> {    // stroke delete
                            saveCanvas()
                            Log.d("asd","delete1")
                            pathList.remove(st)
                            return
                        }
                        1 -> {    // point delete
                            var index=st.point.indexOf(i)
                            if(index in 1..st.point.size-2){ // 점이 중간 지점에 있을 때
                                var path1=Stroke()// 선을 2개로 나눔
                                var path2=Stroke()
                                path1.id=st.id// 아이디 이식
                                path2.id=st.id
                                path1.brush=st.brush
                                path2.brush=st.brush
                                for (i in 0 until index){
                                    path1.point.add(st.point[i])
                                }
                                for (i in index+1 until st.point.size){
                                    path2.point.add(st.point[i])
                                }

                                pathList.add(path1)// 새롭게 생성된 선을 리스트에 추가
                                pathList.add(path2)
                                pathList.remove(st)// 기존 선 삭제
                                return
                            }
                            st.point.remove(i) // 지우개에 닿은 점 삭제
                            if(st.point.isEmpty()){
                                pathList.remove(st)
                            }
                            return
                        }
                    }
                }
            }
        }
    }
    fun drawEraser(canvas:Canvas) {// 지우개 범위 그리기
        canvas.drawCircle(pos.first, pos.second, radius,commonBrush)
    }
}