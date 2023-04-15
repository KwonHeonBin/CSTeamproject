package com.KDB.exam

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.KDB.exam.CanvasManager.Companion.focusedImg
import com.KDB.exam.CanvasManager.Companion.imgList
import com.KDB.exam.CanvasManager.Companion.mode
import com.KDB.exam.CanvasManager.Companion.pathList
import com.KDB.exam.CanvasManager.Companion.posX
import com.KDB.exam.CanvasManager.Companion.posY
import com.KDB.exam.CanvasManager.Companion.wrapAreaBox
import com.KDB.exam.DrawCanvas.Companion.drawCanvasBinding
import com.samsung.android.sdk.penremote.SpenUnitManager


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
    private var canvas:Canvas= Canvas()
    var page:Int=1
    private var canvasManager:CanvasManager= drawCanvasBinding.scrollView.canvasManager
    //private var params:ViewGroup.LayoutParams?=null
    private var eraser=canvasManager.eraser
    private var wrapArea=canvasManager.wrapArea
//    var canvasWidth=-1
//    var canvasHeight=-1

    companion object{
        var penManager: SpenUnitManager? = null
    }

    private fun init(){
        //params=ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
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
        drawBackGround(canvasManager.bgGap)
        showImg()
        drawStroke()
        when(mode){     // draw after stroke(선을 그린 후 실행)
            2-> { eraser.drawEraser(canvas) }
            4->{ drawOutline(wrapAreaBox.checkedStroke) }
            5->{
                wrapAreaDrawing()
                drawOutline(wrapAreaBox.checkedStroke)
            }
        }
        invalidate()    // 업데이트
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val dx:Float=event.x-posX   // x,y 좌표의 변화률
        val dy:Float=event.y-posY
        posX=event.x    // x,y 좌표
        posY=event.y
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                canvasManager.focusedCanvas=this.canvas // 현재 클릭중인 캔버스 번호 저장
                canvasManager.startPosX=event.x         // 클릭 좌표 저장
                canvasManager.startPosY=event.y
                when(mode){
                    1->{ canvasManager.penDrawing(MotionEvent.ACTION_DOWN,this) }  // penMode
                    2->{ canvasManager.eraserDrawing(MotionEvent.ACTION_DOWN) }   // eraseMode
                    3->{ canvasManager.shapeDrawing(MotionEvent.ACTION_DOWN,this) }    // shapeMode
                    4->{                                            // cursorMode
                        if(wrapAreaBox.checkedStroke.isEmpty()&&focusedImg==null){ // 현재 포커스 된 객체가 없을 시
                            canvasManager.imgClick()    // 이미지 클릭 여부 확인
                            if(focusedImg==null){canvasManager.strokeClick(MotionEvent.ACTION_DOWN)} // 선 클릭 여부 확인
                            else{ focusedImg!!.clickedPoint= focusedImg!!.clickPosCheck(canvasManager.startPosX,canvasManager.startPosY)}// 이미지 박스 중 클릭한 점 확인
                        }
                        else{
                            if(focusedImg!=null){// 선이 포커스 됐을 시
                                focusedImg!!.clickedPoint= focusedImg!!.clickPosCheck(canvasManager.startPosX,canvasManager.startPosY) // 이미지 박스 중 클릭한 점 확인
                                if(focusedImg!!.clickedPoint!=0){ return true}
                            }
                            else{// 이미지가 포커스 됐을 시
                                wrapAreaBox.clickedPoint=wrapAreaBox.clickPosCheck(canvasManager.startPosX,canvasManager.startPosY) // 선 박스 중 클릭한 점 확인
                                if(wrapAreaBox.clickedPoint!=0){ return true }
                            }
                            canvasManager.strokeClick(MotionEvent.ACTION_DOWN) // 클릭 좌표 업데이트
                            canvasManager.imgClick()
                        }
                    }
                    5->{                                            // wrapMode
                        if(wrapAreaBox.checkedStroke.isEmpty()){canvasManager.wrapDrawing(MotionEvent.ACTION_DOWN)} // 묶음 상자 비활성화 시
                        else {
                            wrapAreaBox.clickedPoint = wrapAreaBox.clickPosCheck(posX, posY) // 묶음상자 활성화 시
                            if (wrapAreaBox.clickedPoint == 0) {    // 범위에서 벗어난 점 클릭 시 상자 해제
                                wrapAreaBox.clearBox()
                                canvasManager.wrapDrawing(MotionEvent.ACTION_DOWN)
                            }
                            else{ for (i in pathList){ canvasManager.box.add(i.clone()) } } // 범위 내부 점 클릭 시
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE->{
                when(mode){
                    1->{ canvasManager.penDrawing(MotionEvent.ACTION_MOVE,this) }
                    2->{ canvasManager.eraserDrawing(MotionEvent.ACTION_MOVE) }
                    3->{ canvasManager.shapeDrawing(MotionEvent.ACTION_MOVE,this) }
                    4->{ canvasManager.stretchWrapAreaBox(Pair(dx,dy),Pair(posX, posY))}
                    5-> {   // 올가미 모드
                        if(wrapAreaBox.checkedStroke.isNotEmpty()&& wrapAreaBox.clickedPoint!=0){
                            canvasManager.stretchWrapAreaBox(Pair(dx,dy),Pair(posX, posY))} // 올가미 모드 활성화 시
                        else{canvasManager.wrapDrawing(MotionEvent.ACTION_MOVE)}
                    }

                }
            }
            MotionEvent.ACTION_UP->{
                when(mode){
                    1->{ canvasManager.penDrawing(MotionEvent.ACTION_UP,this) }
                    2->{ canvasManager.eraserDrawing(MotionEvent.ACTION_UP) }
                    3->{ canvasManager.shapeDrawing(MotionEvent.ACTION_UP,this) }
                    4->{
                        canvasManager.setPointForCheckedStroke() // stroke의 vertex 최적화
                        canvasManager.setImageScale()           // 이미지 크기 조정
                        canvasManager.setImagePosRange()        // 이미지 위치 조정
                    }
                    5->{
                        if(wrapAreaBox.checkedStroke.isNotEmpty()&& wrapAreaBox.clickedPoint!=0){canvasManager.setPointForCheckedStroke()}
                        else{canvasManager.wrapDrawing(MotionEvent.ACTION_UP)}
                    }
                }
                return false
            }
            else -> return false
        }
        postInvalidate()            // UI thread call invalidate()
        return false
    }

    private fun drawStroke(){   // 선 그리시
        val list= pathList.iterator()   // 읽기 전용
        while (list.hasNext()){
            val box=list.next()
            val path= Path()
            if(box.point.isNotEmpty()){
                path.moveTo(box.point.first().first,box.point.first().second)
                for (j in 1 until box.point.size){
                    path.lineTo(box.point[j].first,box.point[j].second)
                }
                if(box.id==page){ canvas.drawPath(path,box.brush) } // path에 있는 점에 맞춰 선을 그림
                invalidate()                //refresh View      -> if any line exist, call onDraw infinitely
            }
        }
    }

    private fun wrapAreaDrawing(){
        val list= wrapArea.iterator()
        val path= Path()
        if(wrapArea.isNotEmpty()){path.moveTo(wrapArea.first().first,wrapArea.first().second)}
        else{return}
        while (list.hasNext()){
            val box=list.next()
            path.lineTo(box.first,box.second)
            canvas.drawPath(path,canvasManager.wrapBrush)
        }
    }
    private fun drawBackGround(gap:Int){
        when(CanvasManager.backgroundMode){
            1->{

            }
            2->{
                for (i in 0..(canvas.height/gap)){   // horizontal line
                    canvas.drawLine(0f,(i*gap).toFloat(),canvas.width.toFloat(),(i*gap).toFloat(),canvasManager.backgroundBrush)
                }
                for (i in 0..(canvas.width/gap)){      //vertical line
                    canvas.drawLine((i*gap).toFloat(),0f,(i*gap).toFloat(),canvas.height.toFloat(),canvasManager.backgroundBrush)
                }
            }
            3->{
                for (i in 0..(canvas.height/gap)){   // horizontal line
                    canvas.drawLine(0f,(i*gap).toFloat(),canvas.width.toFloat(),(i*gap).toFloat(),canvasManager.backgroundBrush)
                }
            }
        }
    }
    private fun drawOutline(stroke:ArrayList<Stroke>){ // 선택된 선 바깥선 그리기
        for (i in stroke){
            if(wrapAreaBox.id!=i.id)wrapAreaBox.id= i.id
            val path= Path()
            if(i.point.isNotEmpty()){
                path.moveTo(i.point.first().first,i.point.first().second)
                for (j in 1 until i.point.size){
                    path.lineTo(i.point[j].first,i.point[j].second)
                }
                canvasManager.outLineBrush.strokeWidth=(i.brush.strokeWidth+5f)// 선택된 선보다 굵은 굵기로 그림
                if(i.id==page){canvas.drawPath(path,canvasManager.outLineBrush)}
            }
        }
        if(stroke.isNotEmpty()&&wrapAreaBox.id==page){
            wrapAreaBox.drawBox(canvas)
        }
    }
    private fun showImg(){  // 이미지 출력
        for (i in imgList){
            if(i.id==page){
                canvas.drawBitmap(i.bitmapImg,i.matrix,null)
                if(focusedImg ==i){i.drawBox(canvas)}
            }
        }
    }
}


