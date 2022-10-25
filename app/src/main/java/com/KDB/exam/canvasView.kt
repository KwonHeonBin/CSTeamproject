package com.KDB.exam

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.KDB.exam.DrawCanvas.Companion.paintBrush
import com.KDB.exam.DrawCanvas.Companion.path
import kotlin.math.log
import kotlin.reflect.typeOf

class canvasView : View {


    constructor(context: Context) : this(context, null){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0){
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
    var params:ViewGroup.LayoutParams?=null
    companion object{
        var pathList= ArrayList<Path>()
        var colorList=ArrayList<Int>()
        var currentBrush=Color.BLACK
    }

    private fun init(){
        paintBrush.isAntiAlias=true
        paintBrush.color= currentBrush
        paintBrush.style=Paint.Style.STROKE
        paintBrush.strokeJoin=Paint.Join.ROUND
        paintBrush.strokeWidth=8f
        params=ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in pathList.indices){
            paintBrush.color= colorList[i]      // set color
            canvas.drawPath(pathList[i], paintBrush)    // draw
            invalidate()                //refresh View      -> if any line exist, call onDraw infinitely
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var x=event.x
        var y=event.y
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                path.moveTo(x,y)        // start point
                return true
            }
            MotionEvent.ACTION_MOVE->{
                path.lineTo(x,y)        //add path road
                pathList.add(path)      // put path to list
                colorList.add(currentBrush) // save color
            }
            else -> return false
        }
        postInvalidate()            // UI thread call invalidate()
        return false
    }
}


