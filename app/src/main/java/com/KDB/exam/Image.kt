package com.KDB.exam

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import kotlin.math.abs

class Image:Box {
    var bitmapImg:Bitmap
    lateinit var oriBitmapImg:Bitmap
    private var context:Context
    var imgURI:Uri
    var pos=Pair(0f,0f)
    var isFocused=false
    var contentResolver:ContentResolver
    var matrix=Matrix()


    constructor(uri:Uri?, context: Context, focused:Boolean,contentResolver:ContentResolver){
        this.context=context
        isFocused=focused
        imgURI=uri!!
        this.contentResolver=contentResolver
        boxBrush.strokeWidth=6f
        circleBrush.strokeWidth=6f
        circleFillBrush.strokeWidth=6f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            oriBitmapImg= ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imgURI!!))
        }
        bitmapImg=oriBitmapImg
    }
    fun setImageSize(x:Int,y:Int){
        bitmapImg=Bitmap.createScaledBitmap(oriBitmapImg, x, y, false)
        setBox()
    }

    fun applyImageSize(){
        pos=Pair(upperLPoint.first,upperLPoint.second)
        bitmapImg=Bitmap.createScaledBitmap(oriBitmapImg,
            abs(upperRPoint.first-upperLPoint.first).toInt(),
            abs(underLPoint.second-upperLPoint.second).toInt(), false)
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
    fun setMatrix(){

        if(upperRPoint.first-upperLPoint.first<0){
            if(underLPoint.second-upperLPoint.second<0){matrix.setScale(-1f,-1f)}
            else{matrix.setScale(-1f,1f)}
        }
        else{
            if(underLPoint.second-upperLPoint.second<0){matrix.setScale(1f,-1f)}
            else{matrix.setScale(1f,1f)}
        }

    }

}