package com.KDB.exam

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class Image:Box {
    lateinit var bitmapImg:Bitmap
    private var context:Context
    var pos=Pair(0f,0f)

    constructor(context: Context){
        this.context=context
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
        bitmapImg=Bitmap.createScaledBitmap(bitmapImg, x, y, true);
        setBox()
    }
    fun setImageRotate(degree:Float){
        var matrix=Matrix()
        matrix.postRotate(degree)
        bitmapImg=Bitmap.createBitmap(bitmapImg,pos.first.toInt(),pos.second.toInt(),bitmapImg.width,bitmapImg.height,matrix,true)
    }
    override fun setBox(){
        setPoint(pos.first,pos.second,pos.first+bitmapImg.width,pos.second+bitmapImg.height)
    }


}