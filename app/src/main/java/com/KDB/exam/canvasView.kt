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
    var canvas:Canvas= Canvas()
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
        when(mode){     // draw after stroke
            2-> { eraser.drawEraser(canvas) }
            4->{ drawOutline(wrapAreaBox.checkedStroke) }
            5->{
                wrapAreaDrawing()
                drawOutline(wrapAreaBox.checkedStroke)
            }
        }
        invalidate()
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val dx:Float=event.x-posX
        val dy:Float=event.y-posY
        posX=event.x
        posY=event.y
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                canvasManager.focusedCanvas=this.canvas
                canvasManager.startPosX=event.x
                canvasManager.startPosY=event.y
                when(mode){
                    1->{ canvasManager.penDrawing(MotionEvent.ACTION_DOWN,this) }  // penMode
                    2->{ canvasManager.eraserDrawing(MotionEvent.ACTION_DOWN) }   // eraseMode
                    3->{ canvasManager.shapeDrawing(MotionEvent.ACTION_DOWN,this) }    // shapeMode
                    4->{                                            // cursorMode
                        if(wrapAreaBox.checkedStroke.isEmpty()&&focusedImg==null){
                            canvasManager.imgClick()
                            if(focusedImg==null){canvasManager.strokeClick(MotionEvent.ACTION_DOWN)}
                            else{ focusedImg!!.clickedPoint= focusedImg!!.clickPosCheck(canvasManager.startPosX,canvasManager.startPosY)}
                        }
                        else{
                            if(focusedImg!=null){
                                focusedImg!!.clickedPoint= focusedImg!!.clickPosCheck(canvasManager.startPosX,canvasManager.startPosY)
                                if(focusedImg!!.clickedPoint!=0){ return true}
                            }
                            else{
                                wrapAreaBox.clickedPoint=wrapAreaBox.clickPosCheck(canvasManager.startPosX,canvasManager.startPosY)
                                if(wrapAreaBox.clickedPoint!=0){ return true }
                            }
                            canvasManager.strokeClick(MotionEvent.ACTION_DOWN)
                            canvasManager.imgClick()
                        }
                    }
                    5->{                                            // wrapMode
                        if(wrapAreaBox.checkedStroke.isEmpty()){canvasManager.wrapDrawing(MotionEvent.ACTION_DOWN)}
                        else {
                            wrapAreaBox.clickedPoint = wrapAreaBox.clickPosCheck(posX, posY)
                            if (wrapAreaBox.clickedPoint == 0) {
                                wrapAreaBox.clearBox()
                                canvasManager.wrapDrawing(MotionEvent.ACTION_DOWN)
                            }
                            else{ for (i in pathList){ canvasManager.box.add(i.clone()) } }
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
                    4->{ canvasManager.stretchWrapAreaBox(Pair(dx,dy))}
                    5-> {
                        if(wrapAreaBox.checkedStroke.isNotEmpty()&& wrapAreaBox.clickedPoint!=0){canvasManager.stretchWrapAreaBox(Pair(dx,dy))}
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
                        canvasManager.setPointForCheckedStroke()
                        canvasManager.setImageScale()
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

    private fun drawStroke(){
        val list= pathList.iterator()
        while (list.hasNext()){
            val box=list.next()
            val path= Path()
            if(box.point.isNotEmpty()){
                path.moveTo(box.point.first().first,box.point.first().second)
                for (j in 1 until box.point.size){
                    path.lineTo(box.point[j].first,box.point[j].second)
                }
                if(box.id==page){ canvas.drawPath(path,box.brush) }
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
    private fun drawOutline(stroke:ArrayList<Stroke>){
        for (i in stroke){
            val path= Path()
            if(i.point.isNotEmpty()){
                path.moveTo(i.point.first().first,i.point.first().second)
                for (j in 1 until i.point.size){
                    path.lineTo(i.point[j].first,i.point[j].second)
                }
                canvasManager.outLineBrush.strokeWidth=(i.brush.strokeWidth+5f)
                if(i.id==page){canvas.drawPath(path,canvasManager.outLineBrush)}
            }
        }
        if(stroke.isNotEmpty()&&wrapAreaBox.id==page){
            wrapAreaBox.drawBox(canvas)
        }
    }
    private fun showImg(){
        for (i in imgList){
            if(i.id==page){
                canvas.drawBitmap(i.bitmapImg,i.pos.first,i.pos.second,null)
                if(focusedImg ==i){i.drawBox(canvas)}
            }
        }
    }
}


