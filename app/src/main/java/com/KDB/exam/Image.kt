package com.KDB.exam

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.graphics.values
import androidx.core.math.MathUtils.clamp
import com.KDB.exam.CanvasManager.Companion.getDst
import com.KDB.exam.CanvasManager.Companion.posX
import com.KDB.exam.CanvasManager.Companion.posY
import kotlin.math.*

class Image:Box {
    var bitmapImg:Bitmap
    private lateinit var oriBitmapImg:Bitmap
    private var context:Context
    private var imgURI:Uri
    var pos=Pair(0f,0f)
    var isFocused=false
    private var contentResolver:ContentResolver
    var matrix=Matrix()
    var matrixDegree:Float=0f;
    // 이미지의 중간 지점
    var rad=0f// 이미지의 꼭지점으로 이루어진 원의 반지름
    var ratio:Float=0f // 이미지를 절반으로 나눴을때 각도

    constructor(uri:Uri?, context: Context, focused:Boolean,contentResolver:ContentResolver,pos:Pair<Float,Float>,isImg:Boolean=true){
        this.context=context
        isFocused=focused
        isImgBox=isImg
        imgURI=uri!!
        this.pos=pos
        matrix.setTranslate(pos.first,pos.second)
        this.contentResolver=contentResolver
        boxBrush.strokeWidth=6f
        circleBrush.strokeWidth=6f
        circleFillBrush.strokeWidth=6f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // 이미지 원본 저장
            oriBitmapImg= ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imgURI!!))
        }
        bitmapImg=oriBitmapImg
    }
    fun setImageSize(x:Int,y:Int){// 이미지 사이즈 설정
        bitmapImg=Bitmap.createScaledBitmap(oriBitmapImg, x, y, true)
        setBox()
    }
    fun applyImageSize(){ // 이미지 사이즈 적용
        //setMatrix()
//        swapPoints()
        if(degree==0f){
            pos=Pair(upperLPoint.first,upperLPoint.second)
            matrix.setTranslate(pos.first,pos.second)
        }
        bitmapImg=Bitmap.createBitmap(oriBitmapImg,0,0,     // apply matrix
            oriBitmapImg.width, oriBitmapImg.height,null,false)
        bitmapImg=Bitmap.createScaledBitmap(bitmapImg,             // apply scale
            getDst(upperLPoint,upperRPoint).toInt(),
            getDst(upperLPoint,underLPoint).toInt(), true)
//        bitmapImg=Bitmap.createBitmap(bitmapImg,0,0,     // drawbitmap에서 이미 매트릭스를 적용했기때문에 불필요
//            bitmapImg.width, bitmapImg.height,matrix, true)

    }
    fun setImageRotate(degree:Float){
        if(degree!=matrixDegree&&abs(degree-matrixDegree)>0.1f){
            matrix.postRotate((degree-matrixDegree),midPoint.first,midPoint.second)
            rotateBox(degree-matrixDegree)
            matrixDegree=degree
        }
        applyImageSize()
    }

    override fun setMidPoint() {
        super.setMidPoint()
        if(clickedPoint!=10){
            deletePoint=rotatePoint(degree,Pair(midPoint.first+(((bitmapImg.width*0.5f)-25f)),midPoint.second-((bitmapImg.height*0.5f)-25f)),midPoint)
            rotationPoint=rotatePoint(degree,Pair(midPoint.first,midPoint.second-(((bitmapImg.height*0.5f)+50f))),midPoint)
//            if(degree!=matrixDegree){
//
//                matrixDegree=degree
//            }


//            deletePoint=Pair(midPoint.first-(25f),midPoint.second+(25f))
//            rotationPoint=Pair(upperMPoint.first+(50f*sin(degree)),upperMPoint.second-(50f*cos(degree)))
        }
    }

    override fun setBox(){
        pos=Pair(clamp(pos.first,0f,1080f-bitmapImg.width),clamp(pos.second,0f,1806f-bitmapImg.height ))
        if(degree==0f){
            setPoint(Pair(pos.first,pos.second),
                Pair(pos.first+bitmapImg.width,pos.second),
                Pair(pos.first,pos.second+bitmapImg.height),
                Pair(pos.first+bitmapImg.width,pos.second+bitmapImg.height))
            matrix.setTranslate(pos.first,pos.second)
        }

    }

    fun moveImg(dst:Pair<Float,Float>){
        pos= Pair(pos.first+dst.first,pos.second+dst.second)
        matrix.setTranslate(pos.first,pos.second)
        matrix.postRotate(degree,midPoint.first,midPoint.second)
    }

    override fun clearBox() {
        isFocused=false
    }
    private fun setMatrix(){ // 이미지 매트릭스 설정
        var values=matrix.values()
        if(upperRPoint.first<upperLPoint.first){// 좌우 x좌표 매트릭스상 스왑
            if(values[0]==-1f){ values[0]=1f }
            else{ values[0]=-1f }
        }
        if(underLPoint.second<upperLPoint.second){// 좌우 y좌표 매트릭스상 스왑
            if(values[4]==-1f){ values[4]=1f }
            else{ values[4]=-1f }
        }
        matrix.setValues(values)
    }
    private fun swapPoints(){
        if(upperRPoint.first<upperLPoint.first){// 좌우 x좌표 스왑
            upperLPoint=upperRPoint.also { upperRPoint=upperLPoint }
            underLPoint=underRPoint.also { underRPoint=underLPoint }
            setMidPoint()
            clickedPoint=clickPosCheck(posX, posY)
        }
        if(underLPoint.second<upperLPoint.second){// 좌우 y좌표 스왑
            upperLPoint=underLPoint.also { underLPoint=upperLPoint }
            upperRPoint=underRPoint.also { underRPoint=upperRPoint }
            setMidPoint()
            clickedPoint=clickPosCheck(posX, posY)
        }

    }


}