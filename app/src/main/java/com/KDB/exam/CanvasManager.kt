package com.KDB.exam


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.LinearLayout
import com.samsung.android.sdk.penremote.SpenUnitManager
import kotlin.math.*

class CanvasManager:LinearLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    var pages= ArrayList<canvasView>()
    var focusedCanvas:Canvas?=null
    var startPosX=-100f
    var startPosY=-100f
    var stroke=Stroke()
    var box=ArrayList<Stroke>()
    var wrapArea=ArrayList<Pair<Float,Float>>()
    var eraser=Eraser(20f, Pair(-100f,-100f),1)

    //var canvasBitmap:Bitmap?=null

    var bgGap:Int=100


    var outLineBrush: Paint = Paint().apply {
        color= Color.RED
        alpha=80
        strokeWidth=3f
        isAntiAlias=true
        style= Paint.Style.STROKE
    }
    var backgroundBrush: Paint = Paint().apply {
        color= Color.BLACK
        strokeWidth=3f
        style= Paint.Style.STROKE
    }
    var wrapBrush: Paint = Paint().apply {
        color= Color.BLACK
        strokeWidth=3f
        style= Paint.Style.STROKE
        pathEffect= DashPathEffect(floatArrayOf(10f, 20f), 0f)
    }
    companion object{
        var mode:Int=1      // 1-> penMode  2-> eraser  3-> shape  4-> cursor  5-> wrap  6-> image 7-> hand
        var pathList=ArrayList<Stroke>()
        var imgList=ArrayList<Image>()
        var unStroke=ArrayList<ArrayList<Stroke>>()
        var reStroke=ArrayList<ArrayList<Stroke>>()
        var penManager: SpenUnitManager? = null
        var shapeMode=1     // 1-> line 2-> circle 3-> filledCircle 4-> rect 5-> filledRect 6-> triangle 7-> filledTriangle
        var backgroundMode=1    // 1-> none 2-> grid 3-> underBar 3
        var currentBrush=Color.BLACK
        var wrapAreaBox=SelectedBox(0f,0f,0f,0f)
        var focusedImg:Image?=null
        var isMagnetMode:Boolean=false
        var posX:Float=-100f
        var posY:Float=-100f
        fun getDst(p1: Pair<Float, Float>, p2: Pair<Float, Float>): Float {
            return sqrt(
                abs(p1.first - p2.first).pow(2)
                        + abs(p1.second - p2.second).pow(2))
        }
        fun saveCanvas(){ unStroke.add(pathList.clone() as ArrayList<Stroke>) }
    }


    fun penDrawing(action:Int,canvas: canvasView){
        when(action){
            0->{    // down
                for (i in pathList){ box.add(i.clone()) }
                stroke=Stroke()
                stroke.id=canvas.page
                stroke.brush.set(DrawCanvas.paintBrush)
                stroke.point.add(Pair(posX, posY))
                pathList.add(stroke)
            }
            2->{    // move
                stroke.point.add(Pair(posX, posY))
            }
            1->{    // up
                unStroke.add(box.clone() as ArrayList<Stroke>)
                if(reStroke.isNotEmpty()){ reStroke.clear()}
                box.clear()
                stroke.point.add(Pair(posX, posY))
                stroke.point=interpolation(stroke.point,stroke.maxDistPerPoint)
                resetPos()
                if(!DrawCanvas.drawCanvasBinding.undo.isEnabled){
                    DrawCanvas.drawCanvasBinding.undo.isEnabled=true}
            }
        }
    }
    fun eraserDrawing(action:Int){
        when(action){
            0->{
                for (i in pathList){ box.add(i.clone()) }
                eraser.pos=Pair(posX, posY)
                eraser.erase(eraser.pos)
            }
            2->{
                var box=ArrayList<Pair<Float,Float>>()
                box.add(eraser.pos)
                box.add(Pair(posX, posY))
                eraser.pos=Pair(posX, posY)
                box=interpolation(box,30f)
                for (i in box){ eraser.erase(i)}
            }
            1->{
                eraser.erase(Pair(posX, posY))
                resetPos()
                eraser.pos=Pair(posX, posY)
                if(box!= pathList){
                    unStroke.add(box.clone() as ArrayList<Stroke>)
                    if(reStroke.isNotEmpty()){ reStroke.clear()}
                }
                box.clear()
            }
        }
    }
    fun shapeDrawing(action:Int,canvas: canvasView){
        val magneticPosX:Float
        val magneticPosY:Float
        if(backgroundMode ==2 && isMagnetMode){
            magneticPosX=magnetic(posX)
            magneticPosY=magnetic(posY)
            startPosX=magnetic(startPosX,true)
            startPosY=magnetic(startPosY,true)
        }
        else{
            magneticPosX= posX
            magneticPosY= posY
        }
        when(action){
            0->{
                box= pathList.clone() as ArrayList<Stroke>
                stroke=Stroke()
                DrawCanvas.path.moveTo(startPosX,startPosY)        // start point
                stroke.brush.set(DrawCanvas.paintBrush)
                stroke.id=canvas.page
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
                        if(shapeMode ==3){stroke.brush.style=Paint.Style.FILL}
                    }
                    4, 5 -> {   // circle
                        for (i in 0..50){stroke.point.add(Pair(startPosX,startPosY))}
                        if(shapeMode ==5){stroke.brush.style=Paint.Style.FILL}
                    }
                    6, 7 -> {   //triangle
                        stroke.point.add(Pair(startPosX,startPosY))
                        stroke.point.add(Pair(startPosX,startPosY))
                        stroke.point.add(Pair(startPosX,startPosY))
                        if(shapeMode ==7){stroke.brush.style=Paint.Style.FILL}
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
                    if(!DrawCanvas.drawCanvasBinding.undo.isEnabled){
                        DrawCanvas.drawCanvasBinding.undo.isEnabled=true}
                }
            }
        }
    }
    fun wrapDrawing(action: Int){
        when(action){
            0->{    // down

                wrapAreaBox.clearBox()
                wrapArea.add(Pair(posX, posY))
            }
            2->{    // move
                if(!wrapArea.contains(Pair(posX, posY))){wrapArea.add(Pair(
                    posX,
                    posY
                ))}
            }
            1->{    // up
                wrapArea=unInterpolation(wrapArea,40f)
                wrapArea.add(wrapArea[0])
                for (i in pathList){
                    if(isIn(wrapArea,i)&& !wrapAreaBox.checkedStroke.contains(i)){
                        wrapAreaBox.checkedStroke.add(i)
                    }
                }
                if(wrapAreaBox.checkedStroke.isNotEmpty()){
                    wrapAreaBox.setBox()
                }
                wrapArea.clear()
            }
        }
    }

    fun strokeClick(action:Int){    // check which stroke clicked
        when(action){
            0->{    // down
                if(wrapAreaBox.checkedStroke.isEmpty()){
                    for (i in pathList){
                        for(j in i.point){
                            if(getDst(
                                    Pair(posX, posY),
                                    j
                                ) <20f&&!wrapAreaBox.checkedStroke.contains(i)){
                                wrapAreaBox.checkedStroke.clear()
                                wrapAreaBox.checkedStroke.add(i)
                                wrapAreaBox.setBox()
                                wrapAreaBox.clickedPoint=9
                                for (i in pathList){ box.add(i.clone()) }
                                return
                            }
                        }
                    }
                }
                else{
                    wrapAreaBox.clickedPoint= wrapAreaBox.clickPosCheck(startPosX,startPosY)
                    if(wrapAreaBox.clickedPoint==0){
                        wrapAreaBox.clearBox()
                        strokeClick(MotionEvent.ACTION_DOWN)
                    }
                }
            }
            2->{    // move

            }
            1->{    // up

            }
        }
    }
    fun setPointForCheckedStroke(){
        if(wrapAreaBox.checkedStroke.isNotEmpty()&&
            (wrapAreaBox.clickedPoint!=0&& wrapAreaBox.clickedPoint!=9)){
            for (i in wrapAreaBox.checkedStroke){
                unInterpolation(i.point,10f)
                interpolation(i.point,30f)
                wrapAreaBox.setStrokeScale()
            }
        }
        for (i in 0 until box.size){
            if(box[i].point != pathList[i].point){
                unStroke.add(box.clone() as ArrayList<Stroke>)
                break
            }
        }
        box.clear()
        if(reStroke.isNotEmpty()){ reStroke.clear()}
    }
    fun imgClick(){
        for (i in imgList){
            when(posX){
                in i.pos.first..i.pos.first+i.bitmapImg.width->{
                    when(posY){
                        in i.pos.second..i.pos.second+i.bitmapImg.height->{
                            if(focusedImg !=null){ focusedImg!!.isFocused=false}    // swap focusedImg}
                            focusedImg =i
                            focusedImg!!.isFocused=true
                            return
                        }
                    }
                }
            }
        }
        focusedImg =null     // if click no image, set null
    }
    fun stretchWrapAreaBox(dst:Pair<Float,Float>){
        if(sqrt(dst.first.pow(2)+dst.second.pow(2))>3f){
            if(wrapAreaBox.checkedStroke.isNotEmpty()){
                wrapAreaBox.moveBox(dst)
                wrapAreaBox.applyScale()
            }
            else if(focusedImg !=null){
                focusedImg!!.moveBox(dst)
                when(focusedImg!!.clickedPoint){
                    9->{ focusedImg!!.moveImg(dst)}
                    0->{}
                    else->{}
                }
            }
        }
    }
    fun setImageScale(){
        if(focusedImg !=null && focusedImg!!.clickedPoint!=0&& focusedImg!!.clickedPoint!=9){ focusedImg!!.applyImageSize() }
    }
    private fun magnetic(point:Float, isForced:Boolean=false, degree:Float=0.2f):Float{
        val degree:Float=if(isForced){0.5f}else{degree}
        val magX:Float = if(abs(point% bgGap) <= bgGap *degree) {
            ((point/ bgGap).toInt()* bgGap).toFloat()
        } else if(abs(point% bgGap)> bgGap *(1f-degree)){
            (((point/ bgGap).toInt()+1)* bgGap).toFloat()
        } else {
            point
        }
        return magX
    }
    private fun resetPos(){
        posX =-100f
        posY =-100f
    }

    private fun isIn(points: ArrayList<Pair<Float, Float>>, stroke:Stroke):Boolean{
        for (i in 0 until stroke.point.size-1 step (2)){
            var crossReps=0
            var dif: Float
            var crossPoint: Float
            for(j in 0 until points.size-1){
                dif=(points[j+1].second-points[j].second)/(points[j+1].first-points[j].first)
                crossPoint=dif*(stroke.point[i].first-points[j].first)+points[j].second
                if(crossPoint>=stroke.point[i].second&&
                    stroke.point[i].first>= min(points[j].first,points[j+1].first) &&
                    stroke.point[i].first<= max(points[j].first,points[j+1].first) &&
                    crossPoint>= min(points[j].second,points[j+1].second) &&
                    crossPoint<= max(points[j].second,points[j+1].second)
                ){
                    crossReps+=1
                }
            }
            if(crossReps%2==1){
                crossReps=0         // double check
                for(j in 0 until points.size-1 step (2)) {
                    dif = (points[j + 1].second - points[j].second) / (points[j + 1].first - points[j].first)
                    crossPoint = ((stroke.point[i].second - points[j].second) / dif) + points[j].first
                    if (crossPoint >= stroke.point[i].first &&
                        stroke.point[i].second >= min(points[j].second, points[j + 1].second) &&
                        stroke.point[i].second <= max(points[j].second, points[j + 1].second) &&
                        crossPoint >= min(points[j].first, points[j + 1].first) &&
                        crossPoint <= max(points[j].first, points[j + 1].first)
                    ) {
                        crossReps += 1
                    }
                }
                if(crossReps%2==1){return true}
            }
        }
        return false
    }
    private fun interpolation(point:ArrayList<Pair<Float,Float>>, gap:Float):ArrayList<Pair<Float,Float>>{
        var i=0
        while(i<point.size-1){
            if(getDst(point[i], point[i + 1]) >gap){
                val pos=Pair((point[i].first+point[i+1].first)/2,(point[i].second+point[i+1].second)/2)
                point.add(i+1,pos)
                continue
            }
            i++
        }
        return point
    }
    private fun unInterpolation(points: ArrayList<Pair<Float,Float>>, gap:Float=40f):ArrayList<Pair<Float,Float>> {
        var i=0
        while(i < points.size-1){
            while(getDst(points[i], points[i + 1]) <gap){
                points.remove(points[i+1])
                if(i==points.size-1){break}
            }
            i++
        }
        return points
    }

    fun refreshState(){
        if(mode !=4&& mode !=5){ wrapAreaBox.clearBox()}
    }


    private fun init(){

    }
}