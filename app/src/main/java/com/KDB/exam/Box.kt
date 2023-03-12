package com.KDB.exam

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.KDB.exam.CanvasManager.Companion.getDst
import kotlin.math.cos
import kotlin.math.sin

open class Box {

    protected var upperLPoint=Pair(0f,0f)// 박스의 좌표
    protected var upperRPoint=Pair(0f,0f)
    protected var underLPoint=Pair(0f,0f)
    protected var underRPoint=Pair(0f,0f)
    private var upperMPoint=Pair(0f,0f)
    private var underMPoint=Pair(0f,0f)
    private var midLPoint=Pair(0f,0f)
    private var midRPoint=Pair(0f,0f)
    private var rotatePoint=Pair(0f,0f)
    protected var isImgBox=false
    var clickedPoint:Int=0// 클릭된 포인트
    var degree:Float=0f
    var id:Int=1

    protected var boxBrush: Paint = Paint().apply {// 박스 브러쉬
        color= Color.RED
        alpha=100
        strokeWidth=3f
        style= Paint.Style.STROKE
    }
    protected var circleBrush: Paint = Paint().apply {// 박스 좌표 브러쉬1
        color= Color.RED
        strokeWidth=3f
        alpha=150
        style= Paint.Style.STROKE
        isAntiAlias=true
    }
    protected var circleFillBrush: Paint = Paint().apply {// 박스 좌표 브러쉬2
        color= Color.WHITE
        strokeWidth=3f
        style= Paint.Style.FILL
        isAntiAlias=true
    }

    protected fun setPoint(upperX:Float,upperY:Float,underX:Float,underY:Float){
        upperLPoint=Pair(upperX,upperY)
        upperRPoint=Pair(underX,upperY)
        underLPoint=Pair(upperX,underY)
        underRPoint=Pair(underX,underY)
        setMidPoint()
    }
    protected fun setMidPoint(){// 중간 포인트 설정
        upperMPoint=Pair((upperLPoint.first+upperRPoint.first)/2,upperLPoint.second)
        underMPoint=Pair((upperLPoint.first+upperRPoint.first)/2,underLPoint.second)
        midLPoint=Pair(upperLPoint.first,(upperLPoint.second+underLPoint.second)/2)
        midRPoint=Pair(upperRPoint.first,(upperRPoint.second+underRPoint.second)/2)
        rotatePoint=Pair(upperMPoint.first,upperMPoint.second+50f)
    }
    fun drawBox(canvas: Canvas){
        var paths=Path()
        paths.moveTo(upperLPoint.first,upperLPoint.second)// 선 그리기
        paths.lineTo(underLPoint.first,underLPoint.second)
        paths.lineTo(underRPoint.first,underRPoint.second)
        paths.lineTo(upperRPoint.first,upperRPoint.second)
        paths.lineTo(upperLPoint.first,upperLPoint.second)
        canvas.drawPath(paths,boxBrush)
        //canvas.drawRect(upperLPoint.first,upperLPoint.second,underRPoint.first,underRPoint.second,boxBrush)
        drawCircle(upperLPoint.first,upperLPoint.second,10f,canvas)    // edge point 그리기1
        drawCircle(upperRPoint.first,upperRPoint.second,10f,canvas)
        drawCircle(underLPoint.first,underLPoint.second,10f,canvas)
        drawCircle(underRPoint.first,underRPoint.second,10f,canvas)
        drawCircle(midLPoint.first,midLPoint.second,10f,canvas)    // mid point 그리기
        drawCircle(midRPoint.first,midRPoint.second,10f,canvas)
        drawCircle(upperMPoint.first,upperMPoint.second,10f,canvas)
        drawCircle(underMPoint.first,underMPoint.second,10f,canvas)
        if(isImgBox){drawCircle(rotatePoint.first,rotatePoint.second,10f,canvas)}
    }
    private fun drawCircle(x:Float, y:Float, rad:Float, canvas: Canvas){// 포인트를 그릴 동그라미
        canvas.drawCircle(x,y,rad,circleFillBrush)
        canvas.drawCircle(x,y,rad,circleBrush)
    }
    fun clickPosCheck(posX:Float,posY:Float):Int{// 클릭 좌표 확인
        val pos=Pair(posX,posY)
        return if(getDst(upperLPoint,pos)<=20f){
            1
        }    // set size of XY upperL
        else if(getDst(upperRPoint,pos)<=20f){
            2
        } // set size of XY upperR
        else if(getDst(underLPoint,pos)<=20f){
            3
        } // set size of XY underL
        else if(getDst(underRPoint,pos)<=20f){
            4
        } // set size of XY underR
        else if(getDst(midLPoint,pos)<=20f){
            5
        } // set size of X midL
        else if(getDst(midRPoint,pos)<=20f){
            6
        } // set size of X midR
        else if(getDst(underMPoint,pos)<=20f){
            7
        } //set size of Y underM
        else if(getDst(upperMPoint,pos)<=20f){
            8
        } // set size of Y upperM
        else if(pos.first>=upperLPoint.first&&// 범위 이외의 좌표 클릭 시
            pos.first<=upperRPoint.first&&
            pos.second>=upperLPoint.second&&
            pos.second<=underRPoint.second){
            9    // set pos
        }
        else if(isImgBox && getDst(rotatePoint,pos)<=20f){
            10  // set rotation
        }
        else {
            0
        }        // None
    }

    open fun moveBox(dst:Pair<Float,Float>){// 박스 조정
        when(clickedPoint){
            1->{    // set size of XY upperL
                upperLPoint=Pair(upperLPoint.first+dst.first,upperLPoint.second+dst.second)
                upperRPoint=Pair(upperRPoint.first,upperRPoint.second+dst.second)
                underLPoint=Pair(underLPoint.first+dst.first,underLPoint.second)
            }
            2->{    // set size of XY upperR
                upperRPoint=Pair(upperRPoint.first+dst.first,upperRPoint.second+dst.second)
                upperLPoint=Pair(upperLPoint.first,upperLPoint.second+dst.second)
                underRPoint=Pair(underRPoint.first+dst.first,underRPoint.second)
            }
            3->{    // set size of XY underL
                underLPoint=Pair(underLPoint.first+dst.first,underLPoint.second+dst.second)
                underRPoint=Pair(underRPoint.first,underRPoint.second+dst.second)
                upperLPoint=Pair(upperLPoint.first+dst.first,upperLPoint.second)
            }
            4->{    // set size of XY underR
                underRPoint=Pair(underRPoint.first+dst.first,underRPoint.second+dst.second)
                underLPoint=Pair(underLPoint.first,underLPoint.second+dst.second)
                upperRPoint=Pair(upperRPoint.first+dst.first,upperRPoint.second)
            }
            5->{    // set size of X midL
                upperLPoint=Pair(upperLPoint.first+dst.first,upperLPoint.second)
                underLPoint=Pair(underLPoint.first+dst.first,underLPoint.second)
            }
            6->{    // set size of X midR
                underRPoint=Pair(underRPoint.first+dst.first,underRPoint.second)
                upperRPoint=Pair(upperRPoint.first+dst.first,upperRPoint.second)
            }
            7->{    //set size of Y underM
                underRPoint=Pair(underRPoint.first,underRPoint.second+dst.second)
                underLPoint=Pair(underLPoint.first,underLPoint.second+dst.second)
            }
            8->{    // set size of Y upperM
                upperRPoint=Pair(upperRPoint.first,upperRPoint.second+dst.second)
                upperLPoint=Pair(upperLPoint.first,upperLPoint.second+dst.second)
            }
            9->{    // set pos
                upperLPoint=Pair(upperLPoint.first+dst.first,upperLPoint.second+dst.second)
                upperRPoint=Pair(upperRPoint.first+dst.first,upperRPoint.second+dst.second)
                underLPoint=Pair(underLPoint.first+dst.first,underLPoint.second+dst.second)
                underRPoint=Pair(underRPoint.first+dst.first,underRPoint.second+dst.second)
            }
        }
        setMidPoint()
    }

    open fun setBox(){

    }

    open fun clearBox(){
        clickedPoint=0
        upperLPoint=Pair(0f,0f)
        upperRPoint=Pair(0f,0f)
        underLPoint=Pair(0f,0f)
        underRPoint=Pair(0f,0f)
        upperMPoint=Pair(0f,0f)
        underMPoint=Pair(0f,0f)
        midLPoint=Pair(0f,0f)
        midRPoint=Pair(0f,0f)
    }
}