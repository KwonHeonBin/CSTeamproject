package com.KDB.exam

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import com.bumptech.glide.Glide


class Image:Box {
    private var url:ActivityResult?=null
    private var context:Context
    private var frame= FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT,
    )

    constructor(url: ActivityResult,context: Context){
        this.url=url
        this.context=context
    }

    fun showImage(){
        val imgUrl=url?.data?.data
        var img=ImageView(context)
        imgUrl?.let {
            Glide.with(context)
                .load(imgUrl)
                .centerCrop()
                .into(img)

        }
        img.layoutParams=frame
        DrawCanvas.drawCanvasBinding.RL.addView(img)

    }

}