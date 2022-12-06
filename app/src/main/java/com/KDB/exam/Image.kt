package com.KDB.exam

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix

class Image:Box {
    lateinit var bitmapImg:Bitmap
    private var context:Context
    var pos=Pair(0f,0f)
    var isFocused=false


    constructor(context: Context,focused:Boolean){
        this.context=context
        isFocused=focused
        boxBrush.strokeWidth=6f
        circleBrush.strokeWidth=6f
        circleFillBrush.strokeWidth=6f
//        Glide.with(context)
//                .asBitmap()
//                .load(imgUrl)
//                .into(object: CustomTarget<Bitmap>() {
//                    override fun onLoadCleared(placeholder: Drawable?) {
//
//                    }
//                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                        bitmapImg=resource
//                        num=5
//                    }
//                })
    }
    fun setImageSize(x:Int,y:Int){
        bitmapImg=Bitmap.createScaledBitmap(bitmapImg, x, y, true)
        setBox()
    }
    fun setImageRotate(degree:Float){
        val matrix=Matrix()
        matrix.postRotate(degree)
        bitmapImg=Bitmap.createBitmap(bitmapImg,pos.first.toInt(),pos.second.toInt(),bitmapImg.width,bitmapImg.height,matrix,true)
    }
    override fun setBox(){
        setPoint(pos.first,pos.second,pos.first+bitmapImg.width,pos.second+bitmapImg.height)
    }


}