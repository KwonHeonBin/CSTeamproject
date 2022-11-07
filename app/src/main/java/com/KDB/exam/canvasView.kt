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
import kotlin.math.*

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
    var startPosX=-100f
    var startPosY=-100f
    var stroke=Stroke()
    var magneticDegree:Float=0.2f
    var box=ArrayList<Stroke>()
    var canvas:Canvas= Canvas()
//    var canvasWidth=-1
//    var canvasHeight=-1

    var commonBrush:Paint=Paint().apply {
        color=Color.BLACK
        strokeWidth=3f
        style=Paint.Style.STROKE
    }
    var backgroundBrush:Paint=Paint().apply {
        color=Color.BLACK
        strokeWidth=3f
        style=Paint.Style.STROKE
    }
    companion object{
        var pathList=ArrayList<Stroke>()
        var unStroke=ArrayList<ArrayList<Stroke>>()
        var reStroke=ArrayList<ArrayList<Stroke>>()
        var currentBrush=Color.BLACK
        var penManager: SpenUnitManager? = null
        var mode:Int=1      // 1-> penMode  2-> eraser  3-> shape
        //var canvasBitmap:Bitmap?=null
        var shapeMode=1     // 1-> line 2-> circle 3-> filledCircle 4-> rect 5-> filledRect 6-> triangle 7-> filledTriangle
        var backgroundMode=1    // 1-> none 2-> grid 3-> underBar 3
        var bgGap:Int=100
        var eraser=Eraser(20f, Pair(-100f,-100f),1)
        var isMagnetMode:Boolean=false
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
        drawBackGround(bgGap)
        when(mode){
            2-> {
                drawEraser()
                //erase()
            }
        }
        invalidate()

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        posX=event.x
        posY=event.y
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                startPosX=event.x
                startPosY=event.y
                when(mode){
                    1->{ penDrawing(MotionEvent.ACTION_DOWN) }
                    2->{ eraserDrawing(MotionEvent.ACTION_DOWN) }
                    3->{ shapeDrawing(MotionEvent.ACTION_DOWN) }
                }
                return true
            }
            MotionEvent.ACTION_MOVE->{
                when(mode){
                    1->{ penDrawing(MotionEvent.ACTION_MOVE) }
                    2->{ eraserDrawing(MotionEvent.ACTION_MOVE) }
                    3->{ shapeDrawing(MotionEvent.ACTION_MOVE) }
                }
            }
            MotionEvent.ACTION_UP->{
                when(mode){
                    1->{ penDrawing(MotionEvent.ACTION_UP) }
                    2->{ eraserDrawing(MotionEvent.ACTION_UP) }
                    3->{ shapeDrawing(MotionEvent.ACTION_UP) }
                }
                return false
            }
            else -> return false
        }
        postInvalidate()            // UI thread call invalidate()
        return false
    }
    private fun drawStroke(){
        val list= pathList.iterator()
        while (list.hasNext()){
            val box=list.next()
            var path=Path()
            if(box.point.isNotEmpty()){
                path.moveTo(box.point.first().first,box.point.first().second)
                for (j in 1..box.point.size-1){
                    path.lineTo(box.point[j].first,box.point[j].second)
                }
                canvas.drawPath(path,box.brush)
                invalidate()                //refresh View      -> if any line exist, call onDraw infinitely
            }
            //else{pathList.remove(box)}
        }

    }

    private fun penDrawing(action:Int){
        when(action){
            0->{    // down
                box= pathList.clone() as ArrayList<Stroke>
                stroke=Stroke()
                path.moveTo(posX,posY)        // start point
                stroke.brush.set(paintBrush)
                stroke.point.add(Pair(posX,posY))
                pathList.add(stroke)
            }
            2->{    // move
                stroke.point.add(Pair(posX,posY))
            }
            1->{    // up
                unStroke.add(box.clone() as ArrayList<Stroke>)
                if(reStroke.isNotEmpty()){ reStroke.clear()}
                box.clear()
                stroke.point.add(Pair(posX,posY))
                stroke.point=interpolation(stroke.point,stroke.maxDistPerPoint)
                resetPos()
                if(!drawCanvasBinding.undo.isEnabled){drawCanvasBinding.undo.isEnabled=true}
            }
        }
    }
    private fun eraserDrawing(action:Int){

        when(action){
            0->{
                if(eraser.mode==1){
                    box= pathList.clone() as ArrayList<Stroke>
                }
                eraser.pos=Pair(posX,posY)
                erase(eraser.pos)
            }
            2->{
                var box=ArrayList<Pair<Float,Float>>()
                box.add(eraser.pos)
                box.add(Pair(posX,posY))
                eraser.pos=Pair(posX,posY)
                box=interpolation(box,30f)
                for (i in box){erase(i)}
            }
            1->{
                erase(Pair(posX,posY))
                resetPos()
                eraser.pos=Pair(posX,posY)
                if(box!= pathList){
                    unStroke.add(box.clone() as ArrayList<Stroke>)
                    if(reStroke.isNotEmpty()){ reStroke.clear()}
                }
            }
        }
    }
    private fun shapeDrawing(action:Int){
        var magneticPosX:Float=0f
        var magneticPosY:Float=0f
        if(backgroundMode==2 && isMagnetMode){
            magneticPosX=magnetic(posX)
            magneticPosY=magnetic(posY)
            startPosX=magnetic(startPosX,true)
            startPosY=magnetic(startPosY,true)
        }
        else{
            magneticPosX=posX
            magneticPosY=posY
        }
        when(action){
            0->{
                box= pathList.clone() as ArrayList<Stroke>
                stroke=Stroke()
                path.moveTo(startPosX,startPosY)        // start point
                stroke.brush.set(paintBrush)
                stroke.point.add(Pair(startPosX,startPosY))
                when (shapeMode) {
                    1 -> {      // line
                        stroke.point.add(Pair(startPosX,startPosY))
                    }
                    2, 3 -> {   // rect
                        stroke.point.add(Pair(startPosX,startPosY))
                        stroke.point.add(Pair(startPosX,startPosY))
                        stroke.point.add(Pair(startPosX,startPosY))
                        stroke.point.add(Pair(startPosX,startPosY))
                        if(shapeMode==3){stroke.brush.style=Paint.Style.FILL}
                    }
                    4, 5 -> {   // circle
                        for (i in 0..50){stroke.point.add(Pair(startPosX,startPosY))}
                        if(shapeMode==5){stroke.brush.style=Paint.Style.FILL}
                    }
                    6, 7 -> {   //triangle
                        stroke.point.add(Pair(startPosX,startPosY))
                        stroke.point.add(Pair(startPosX,startPosY))
                        stroke.point.add(Pair(startPosX,startPosY))
                        if(shapeMode==7){stroke.brush.style=Paint.Style.FILL}
                    }
                }
                pathList.add(stroke)
            }
            2->{
                when(shapeMode){
                    1->{        // line
                        stroke.point[1]=Pair(magneticPosX,magneticPosY)
                    }
                    2,3->{      // rect
                        stroke.point[1]=Pair(startPosX,magneticPosY)
                        stroke.point[2]=Pair(magneticPosX,magneticPosY)
                        stroke.point[3]=Pair(magneticPosX,startPosY)
                        stroke.point[4]=Pair(startPosX,startPosY)
                    }
                    4,5->{      // circle
                        for (i in 0 until stroke.point.size){
                            stroke.point[i]=Pair(
                                startPosX+((magneticPosX-startPosX)* cos(i*(360/stroke.point.size).toDouble())).toFloat(),
                                startPosY+((magneticPosY-startPosY)* sin(i*(360/stroke.point.size).toDouble())).toFloat())
                        }
                    }
                    6,7->{      // triangle
                        stroke.point[1]=Pair((2*startPosX)-magneticPosX,magneticPosY)
                        stroke.point[2]=Pair(magneticPosX,magneticPosY)
                        stroke.point[3]=Pair(startPosX,startPosY)
                    }
                }
            }
            1->{
                if(stroke.point[0]!=stroke.point[1]){
                    unStroke.add(box.clone() as ArrayList<Stroke>)
                    if(reStroke.isNotEmpty()){ reStroke.clear()}
                    box.clear()
                    stroke.point=interpolation(stroke.point,stroke.maxDistPerPoint)
                    resetPos()
                    if(!drawCanvasBinding.undo.isEnabled){drawCanvasBinding.undo.isEnabled=true}
                }
            }
        }
    }
    private fun magnetic(point:Float, isForce:Boolean=false):Float{
        var magX:Float=0f;
        var degree:Float=magneticDegree
        if(isForce){degree=0.5f}
        if(abs(point% bgGap) <= bgGap*degree) {
            magX=((point/ bgGap).toInt()* bgGap).toFloat()
        }
        else if(abs(point% bgGap)> bgGap*(1f-degree)){
            magX=(((point/ bgGap).toInt()+1)* bgGap).toFloat()
        }
        else {magX=point}
        return magX
    }
    private fun resetPos(){
        posX=-100f
        posY=-100f
    }
    private fun saveCanvas(){
        unStroke.add(pathList.clone() as ArrayList<Stroke>)
    }
    private fun drawEraser() {

        canvas.drawCircle(eraser.pos.first,eraser.pos.second,eraser.radius,commonBrush)
    }
    private fun erase(point:Pair<Float,Float>) {
        for (st in pathList) {
            for ((i, j) in st.point) {
                var distance = sqrt(
                    (point.first - i).pow(2) +
                            (point.second - j).pow(2))
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
    private fun drawBackGround(gap:Int){
        when(backgroundMode){
            1->{

            }
            2->{
                for (i in 0..(canvas.height/gap)){   // horizontal line
                    canvas.drawLine(0f,(i*gap).toFloat(),canvas.width.toFloat(),(i*gap).toFloat(),backgroundBrush)
                }
                for (i in 0..(canvas.width/gap)){      //vertical line
                    canvas.drawLine((i*gap).toFloat(),0f,(i*gap).toFloat(),canvas.height.toFloat(),backgroundBrush)
                }
            }
            3->{
                for (i in 0..(canvas.height/gap)){   // horizontal line
                    canvas.drawLine(0f,(i*gap).toFloat(),canvas.width.toFloat(),(i*gap).toFloat(),backgroundBrush)
                }
            }
        }
    }
    fun interpolation(point:ArrayList<Pair<Float,Float>>, dist:Float):ArrayList<Pair<Float,Float>>{
        var i=0
        while(i<point.size-1){
            //Log.d("asd","size: "+point.size.toString()+"    "+i.toString())
            var diff= sqrt(
                abs(point[i].first-point[i+1].first).pow(2)
                        +abs(point[i].second-point[i+1].second).pow(2))
            //Log.d("asd","before:  "+i.toString()+"   "+diff.toString())
            if(diff>dist){
                var pos=Pair((point[i].first+point[i+1].first)/2,(point[i].second+point[i+1].second)/2)
                point.add(i+1,pos)
                continue
            }
            i++
        }
        return point
    }
}


