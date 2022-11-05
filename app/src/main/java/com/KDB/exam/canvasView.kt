package com.KDB.exam

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.KDB.exam.DrawCanvas.Companion.drawCanvasBinding
import com.KDB.exam.DrawCanvas.Companion.paintBrush
import com.KDB.exam.DrawCanvas.Companion.path
import com.samsung.android.sdk.penremote.SpenUnitManager
import kotlin.math.pow
import kotlin.math.sqrt

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
    var posX:Float=-100f
    var posY:Float=-100f
    var stroke=Stroke()

    var box=ArrayList<Stroke>()
    var canvas:Canvas= Canvas()
    var canvasWidth=-1
    var canvasHeight=-1

    var commonBrush:Paint=Paint().apply {
        color=Color.BLACK
        strokeWidth=3f
        style=Paint.Style.STROKE
    }
    var eraserBrush:Paint= Paint().apply {
        color=Color.WHITE
        strokeWidth=eraser.radius
        style=Paint.Style.STROKE
        isAntiAlias=true
        xfermode=PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    companion object{
        var pathList=ArrayList<Stroke>()
        var unStroke=ArrayList<ArrayList<Stroke>>()
        var reStroke=ArrayList<ArrayList<Stroke>>()
        var currentBrush=Color.BLACK
        var penManager: SpenUnitManager? = null
        var mode:Int=1      // 1-> penMode  2-> eraser
        //var canvasBitmap:Bitmap?=null
        var eraser=Eraser(20f, Pair(-100f,-100f),1)
    }

    private fun init(){
        setupPen()
        params=ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)

    }
    private fun setupPen(){

    }

//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
//        canvasWidth=w
//        canvasHeight=h
//        canvasBitmap= Bitmap.createBitmap(canvasWidth,canvasHeight,Bitmap.Config.ARGB_8888)
//        canvasBitmap?.let { canvas=Canvas(it) }
//    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas=canvas
        //canvasBitmap?.let { canvas.drawBitmap(it,0F,0F, paintBrush) }
        drawStroke()
        when(mode){
            2-> {
                drawEraser()
                erase()
            }
        }
        invalidate()

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        posX=event.x
        posY=event.y
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                actionForMode(mode,MotionEvent.ACTION_DOWN)
                return true
            }
            MotionEvent.ACTION_MOVE->{
                actionForMode(mode,MotionEvent.ACTION_MOVE)
            }
            MotionEvent.ACTION_UP->{
                actionForMode(mode,MotionEvent.ACTION_UP)
                return false
            }
            else -> return false
        }
        postInvalidate()            // UI thread call invalidate()
        return false
    }

    private fun drawStroke(){
        for (i in pathList){
            var path=Path()
            path.moveTo(i.point.first().first,i.point.first().second)
            for (j in 1..i.point.size-1){
                path.lineTo(i.point[j].first,i.point[j].second)
            }
            canvas.drawPath(path,i.brush)
            invalidate()                //refresh View      -> if any line exist, call onDraw infinitely
        }
    }

    fun actionForMode(mode:Int,action:Int){
        when(action){
            0->{        // action_down
                when(mode){
                    1->{
                        box= pathList.clone() as ArrayList<Stroke>
                        stroke=Stroke()
                        path.moveTo(posX,posY)        // start point
                        stroke.brush.set(paintBrush)
                        stroke.point.add(Pair(posX,posY))
                        pathList.add(stroke)
                    }
                    2->{
                        if(eraser.mode==1){
                            box= pathList.clone() as ArrayList<Stroke>
                        }
                    }

                }


            }
            2->{        // action_move
                when(mode){
                    1->{
                        stroke.point.add(Pair(posX,posY))
                    }
                    2->{
                        //erase()
                    }
                }

            }
            1->{        //action_up
                when(mode){
                    1->{

                        //if(!reStroke.isEmpty()){ unStroke.add(reStroke.removeLast().clone() as ArrayList<Stroke>) }
                        unStroke.add(box.clone() as ArrayList<Stroke>)
                        box.clear()
                        stroke.point.add(Pair(posX,posY))
                        stroke.interploation()
                        resetPos()
                        if(!drawCanvasBinding.undo.isEnabled){drawCanvasBinding.undo.isEnabled=true}
                    }
                    2->{
                        resetPos()
                        if(box!= pathList){ unStroke.add(box.clone() as ArrayList<Stroke>) }
                    }
                }

            }
        }

    }

    fun drawEraser() {
        eraser.pos=Pair(posX,posY)
        canvas.drawCircle(eraser.pos.first,eraser.pos.second,eraser.radius,commonBrush)
    }
    fun resetPos(){
        posX=-100f
        posY=-100f
    }
    private fun saveCanvas(){
        unStroke.add(pathList.clone() as ArrayList<Stroke>)
    }
    private fun erase() {
        for (st in pathList) {
            for ((i, j) in st.point) {
                var distance = sqrt(
                    (eraser.pos.first - i).pow(2) +
                            (eraser.pos.second - j).pow(2))
                if (distance <= eraser.radius) {
                    when (eraser.mode) {
                        0 -> {    // stroke delete
                            saveCanvas()
                            pathList.remove(st)
                            return
                        }
                        1 -> {    // point delete
                            var index=st.point.indexOf(Pair(i,j))
                            if(index in 1..st.point.size-2){
                                var path1=Stroke()
                                var path2=Stroke()
                                path1.brush=st.brush
                                path2.brush=st.brush
                                for (i in 0..index-1){
                                    path1.point.add(st.point[i])
                                }
                                for (i in index+1..st.point.size-1){
                                    path2.point.add(st.point[i])
                                }
                                pathList.add(path1)
                                pathList.add(path2)
                                pathList.remove(st)
                                return
                            }
                            st.point.remove(Pair(i,j))
                            if(st.point.isEmpty()){ pathList.remove(st)}
                            return
                        }
                    }
                }
            }
        }
    }
}


