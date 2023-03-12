package com.KDB.exam

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.graphics.values
import com.KDB.exam.CanvasManager.Companion.posX
import com.KDB.exam.CanvasManager.Companion.posY

class Image:Box {
    var bitmapImg:Bitmap
    lateinit var oriBitmapImg:Bitmap
    private var context:Context
    private var imgURI:Uri
    var pos=Pair(0f,0f)
    var isFocused=false
    var contentResolver:ContentResolver
    var matrix=Matrix()


    constructor(uri:Uri?, context: Context, focused:Boolean,contentResolver:ContentResolver,isImg:Boolean=true){
        this.context=context
        isFocused=focused
        isImgBox=isImg
        imgURI=uri!!
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
        setMatrix()
        swapPoints()
        pos=Pair(upperLPoint.first,upperLPoint.second)
        bitmapImg=Bitmap.createBitmap(oriBitmapImg,0,0,     // apply matrix
            oriBitmapImg.width, oriBitmapImg.height,matrix, false)
        bitmapImg=Bitmap.createScaledBitmap(bitmapImg,             // apply scale
            (upperRPoint.first-upperLPoint.first).toInt(),
            (underLPoint.second-upperLPoint.second).toInt(), true)
    }
    fun setImageRotate(degree:Float){
        matrix.postRotate(degree)
        bitmapImg=Bitmap.createBitmap(bitmapImg,pos.first.toInt(),pos.second.toInt(),bitmapImg.width,bitmapImg.height,matrix,true)
    }

    override fun setBox(){
        setPoint(pos.first,pos.second,pos.first+bitmapImg.width,pos.second+bitmapImg.height)
    }
    fun moveImg(dst:Pair<Float,Float>){
        pos= Pair(pos.first+dst.first,pos.second+dst.second)
    }

    override fun clearBox() {
        isFocused=false
    }
    private fun setMatrix(){ // 이미지 매트릭스 설정(적용은 구현 안 함)
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