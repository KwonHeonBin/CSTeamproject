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

    var bgGap:Int=100 // 배경 선 간격


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
        pathEffect= DashPathEffect(floatArrayOf(10f, 20f), 0f)// 점선
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
        fun getSize(p1:Pair<Float,Float>):Float{
            return sqrt(p1.first.pow(2)+p1.second.pow(2))
        }
        fun saveCanvas(){ unStroke.add(pathList.clone() as ArrayList<Stroke>) }
    }


    fun penDrawing(action:Int,canvas: canvasView){
        when(action){
            0->{    // down 눌렀을 시 객체 생성 밑 포인트 저장
                for (i in pathList){ box.add(i.clone()) }
                stroke=Stroke()
                stroke.id=canvas.page
                stroke.brush.set(DrawCanvas.paintBrush)
                stroke.point.add(Pair(posX, posY))
                pathList.add(stroke)
            }
            2->{    // move 움직었을 시 좌표 저장
                stroke.point.add(Pair(posX, posY))
            }
            1->{    // up 땠을 시 선형보간 후 저장
                unStroke.add(box.clone() as ArrayList<Stroke>)
                if(reStroke.isNotEmpty()){ reStroke.clear()}
                box.clear()
                stroke.point.add(Pair(posX, posY))
                stroke.point=interpolation(stroke.point,stroke.maxDistPerPoint)
                resetPos()
                if(!DrawCanvas.drawCanvasBinding.undo.isEnabled){// 되돌리기 활성화
                    DrawCanvas.drawCanvasBinding.undo.isEnabled=true}
            }
        }
    }
    fun eraserDrawing(action:Int){
        when(action){
            0->{

                for (i in pathList){ box.add(i.clone()) }
                eraser.pos=Pair(posX, posY) // 지우개 좌표 저장
                eraser.erase(eraser.pos) // 좌표를 토대로 지우기 실행
            }
            2->{
                var box=ArrayList<Pair<Float,Float>>()
                box.add(eraser.pos) // 지우개를 빠르게 움직었을 시 좌표 저장이 원할하지 않으므로 지우개에 선형보간 적용
                box.add(Pair(posX, posY))
                eraser.pos=Pair(posX, posY)
                box=interpolation(box,30f)
                for (i in box){ eraser.erase(i)}
            }
            1->{
                eraser.erase(Pair(posX, posY))
                resetPos()
                eraser.pos=Pair(posX, posY)
                if(box!= pathList){// 되돌리기 초기화
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
        if(backgroundMode ==2 && isMagnetMode){// 자석모드 활성화 시 실행
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
                if(!wrapArea.contains(Pair(posX, posY))){wrapArea.add(Pair(// 중복 좌표 체크
                    posX,
                    posY
                ))}
            }
            1->{    // up
                wrapArea=unInterpolation(wrapArea,40f)// 최적화
                wrapArea.add(wrapArea[0])
                for (i in pathList){
                    if(isIn(wrapArea,i)&& !wrapAreaBox.checkedStroke.contains(i)){// 올가미에 들어가는 선들 체크
                        wrapAreaBox.checkedStroke.add(i)
                    }
                }
                if(wrapAreaBox.checkedStroke.isNotEmpty()){
                    wrapAreaBox.setBox()// 선들을 토대로 box 생성
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
                            if(getDst(Pair(posX, posY), j) <20f&&!wrapAreaBox.checkedStroke.contains(i)){// 클릭 좌표가 선택된 선들의 좌표에 들어가는지 확인
                                wrapAreaBox.checkedStroke.clear()// 새로운 선 포커스
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
                    wrapAreaBox.clickedPoint= wrapAreaBox.clickPosCheck(startPosX,startPosY)// 클릭된 선이 기존 포커스된 선일 시 실행
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
    fun setPointForCheckedStroke(){// 포커스된 선들의 크기 변경 시 최적화
        if(wrapAreaBox.checkedStroke.isNotEmpty()&&
            (wrapAreaBox.clickedPoint!=0&& wrapAreaBox.clickedPoint!=9)){
            for (i in wrapAreaBox.checkedStroke){
                unInterpolation(i.point,10f)
                interpolation(i.point,30f)
                wrapAreaBox.setStrokeScale()
            }
        }
        for (i in 0 until box.size){// 크기 바꾸기 이전 선들 되돌리기로 저장
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
            if(i.isClicked(posX, posY)){
                if(focusedImg !=null){ focusedImg!!.isFocused=false}    // swap focusedImg}
                focusedImg =i
                focusedImg!!.isFocused=true
                return
            }
        }
        focusedImg =null     // if click no image, set null
    }
    fun stretchWrapAreaBox(dst:Pair<Float,Float>,pos:Pair<Float,Float>){// 올가미 상자 크기 조정
        if(sqrt(dst.first.pow(2)+dst.second.pow(2))>3f){// 특정 거리(3) 이상 이동 시 실행
            if(wrapAreaBox.checkedStroke.isNotEmpty()){
                wrapAreaBox.moveBox(dst,pos)
                wrapAreaBox.applyScale()// 배울 적용
            }
            else if(focusedImg !=null){
                focusedImg!!.moveBox(dst,pos)
                when(focusedImg!!.clickedPoint){
                    9->{ focusedImg!!.moveImg(dst)}
                    10->{ focusedImg!!.setImageRotate(focusedImg!!.degree)}
                    0->{}
                    else->{}
                }
            }
        }
    }
    fun setImageScale(){
        if(focusedImg !=null && focusedImg!!.clickedPoint!=0&& focusedImg!!.clickedPoint!=9){ focusedImg!!.applyImageSize() }
    }
    fun setImagePosRange(){focusedImg?.setBox()}
    private fun magnetic(point:Float, isForced:Boolean=false, degree:Float=0.2f):Float{// 자석 효과
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
    private fun interpolation(point:ArrayList<Pair<Float,Float>>, gap:Float):ArrayList<Pair<Float,Float>>{// 선형보간
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