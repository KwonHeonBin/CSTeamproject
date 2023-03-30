package com.KDB.exam

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import com.KDB.exam.CanvasManager.Companion.getDst
import kotlin.math.*

open class Box {

    protected var upperLPoint=Pair(0f,0f)// 박스의 좌표
    protected var upperRPoint=Pair(0f,0f)
    protected var underLPoint=Pair(0f,0f)
    protected var underRPoint=Pair(0f,0f)
    protected var upperMPoint=Pair(0f,0f)
    protected var underMPoint=Pair(0f,0f)
    protected var midLPoint=Pair(0f,0f)
    protected var midRPoint=Pair(0f,0f)
    protected var rotationPoint=Pair(-100f,-100f)
    protected var deletePoint=Pair(-100f,-100f)
    public var midPoint=Pair(0f,0f)
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

    protected fun setPoint(upperL:Pair<Float,Float>, upperR:Pair<Float,Float>, underL:Pair<Float,Float>, underR:Pair<Float,Float>){// 박스 포인트 설정
        upperLPoint=upperL
        upperRPoint=upperR
        underLPoint=underL
        underRPoint=underR
        setMidPoint()
    }
    open fun setMidPoint(){// 중간 포인트 설정
        midPoint=Pair((upperLPoint.first+underRPoint.first)/2, (upperLPoint.second+underRPoint.second)/2)
        upperMPoint=Pair((upperLPoint.first+upperRPoint.first)/2,(upperLPoint.second+upperRPoint.second)/2)
        underMPoint=Pair((underLPoint.first+underRPoint.first)/2,(underLPoint.second+underRPoint.second)/2)
        midLPoint=Pair((upperLPoint.first+underLPoint.first)/2,(upperLPoint.second+underLPoint.second)/2)
        midRPoint=Pair((upperRPoint.first+underRPoint.first)/2,(upperRPoint.second+underRPoint.second)/2)
   }
    protected open fun setOptionPoint(){
//        deletePoint=Pair(upperRPoint.first-(50f),upperRPoint.second+(50f))
//        rotationPoint=Pair(upperMPoint.first,upperMPoint.second-(50f))
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
        if(isImgBox){
            drawCircle(rotationPoint.first,rotationPoint.second,10f,canvas)// rotate point
            drawCircle(deletePoint.first,deletePoint.second,10f,canvas)// delete point
        }
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
        else if(isImgBox && getDst(rotationPoint,pos)<=20f){
            10
        } // set rotation
        else if(isClicked(pos.first,pos.second)){
            9    // set pos
        }

        else {
            0
        }        // None
    }

    open fun moveBox(dst:Pair<Float,Float>,pos:Pair<Float,Float>){// 박스 조정
        val dx=dst.first*cos(degree)
        val dy=dst.second*sin(degree)
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
            10->{   // set degree
                degree=90+(Math.toDegrees(atan2(pos.second-midPoint.second,pos.first-midPoint.first).toDouble())).toFloat()
                //degree+=((dst.first*cos(degree))+(dst.second*sin(degree)))*0.35f
                //Log.d("asd",degree.toString())
            }
        }
        setMidPoint()
    }

    fun rotatePoint(degree:Float,point:Pair<Float,Float>,pivot:Pair<Float,Float>):Pair<Float,Float>{
        val dTheta=Math.toRadians(degree.toDouble()).toFloat()
        return Pair(pivot.first+((point.first-pivot.first)*cos(dTheta)-(point.second-pivot.second)*sin(dTheta)),
            pivot.second+((point.first-pivot.first)*sin(dTheta)+(point.second-pivot.second)*cos(dTheta)))
    }
    protected fun rotateBox(degree: Float){
        setPoint(rotatePoint(degree,upperLPoint,midPoint),
                rotatePoint(degree,upperRPoint,midPoint),
                rotatePoint(degree,underLPoint,midPoint),
                rotatePoint(degree,underRPoint,midPoint))
        deletePoint=rotatePoint(degree,deletePoint,midPoint)
        rotationPoint=rotatePoint(degree,rotationPoint,midPoint)
    }
    fun isClicked(posX:Float,posY:Float):Boolean{
        val ulX=rotatePoint(-degree,upperLPoint,midPoint).first
        val ulY=rotatePoint(-degree,upperLPoint,midPoint).second
        val urX=rotatePoint(-degree,upperRPoint,midPoint).first
        val unlY=rotatePoint(-degree,underLPoint,midPoint).second
        val posX2=rotatePoint(-degree,Pair(posX,posY),midPoint).first
        val posY2=rotatePoint(-degree,Pair(posX,posY),midPoint).second
        if(posX2 in ulX..urX &&posY2 in ulY..unlY){
            return true
        }
        return false
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