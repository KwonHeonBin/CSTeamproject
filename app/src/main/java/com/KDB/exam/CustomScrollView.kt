package com.KDB.exam

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.setMargins
import com.KDB.exam.CanvasManager.Companion.pages
import com.KDB.exam.CanvasManager.Companion.pathList
import kotlin.math.roundToInt

class CustomScrollView: ScrollView {

    var isScrollable:Boolean=false
    var isAddingCanvas:Boolean=false
    var layout:LinearLayout?=null
    var startTime:Long=0
    var endTime:Long=0
    var focusedPageId:Int=1
    lateinit var canvasManager:CanvasManager

    constructor(context: Context?) : super(context){

    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val addBox:TextView= TextView(context).apply {
        text="Add Screen"
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (resources.displayMetrics.heightPixels*0.1).toInt())
        setPadding(0,0,0,getDP(10))
        gravity=Gravity.CENTER or Gravity.BOTTOM
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isScrollable && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        when(ev!!.action) {
            MotionEvent.ACTION_DOWN->{

            }
            MotionEvent.ACTION_MOVE->{
                if(!canScrollVertically(1)){
                    //Toast.makeText(context, "Scroll View bottom reached", Toast.LENGTH_SHORT).show()
                    if(!isAddingCanvas){
                        layout!!.addView(addBox)
                        isAddingCanvas=true
                        startTime=System.currentTimeMillis()
                    }
                }
            }
            MotionEvent.ACTION_UP->{
                focusedIdCheck()
                if(!canScrollVertically(1)&&isAddingCanvas){
                    endTime=System.currentTimeMillis()
                    if(endTime-startTime>500f){
                        Toast.makeText(context, "addView", Toast.LENGTH_SHORT).show()
                        addView()
                    }
                }
                layout!!.removeView(addBox)
                isAddingCanvas=false
            }
        }
        return isScrollable && super.onTouchEvent(ev)
    }
    fun addView(){
        val frame:FrameLayout=FrameLayout(context).apply {
            val layoutBox=LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,resources.displayMetrics.heightPixels-getDP(52))
            layoutParams=layoutBox

        }
        val textBox:CustomEditText=CustomEditText(context).apply {
            val layoutBox=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
            layoutBox.setMargins(getDP(10))
            layoutParams=layoutBox
            background=null
        }
        textBox.gravity=Gravity.TOP
        DrawCanvas.focusedEditText=textBox
        val view:canvasView=canvasView(context).apply {
            val layoutBox=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            layoutBox.setMargins(getDP(5))
            layoutParams = layoutBox
            background=resources.getDrawable(R.color.white,null)
        }
        frame.addView(view)
        frame.addView(textBox)
        canvasManager.addPage(view)
        layout!!.addView(frame)
        view.page=pages.size
        //Log.d("asd",canvasManager.pages.size.toString())
//        Log.d("asd", canvasManager.pages.size.toString())
//        if(canvasManager.pages.size==2){
//            if(canvasManager.pages[0].canvas==canvasManager.pages[1].canvas){
//                Log.d("asd", "same");
//            }
//            else{Log.d("asd", "diff");}
//        }
    }
    private fun addView(page:canvasView){
        layout!!.addView(page)
    }
    fun deleteView(canvasView: canvasView){
        layout!!.removeView(canvasView)
    }
    fun refreshView(page: Int){// 특정 페이지로 레이아웃을 모두 교체
        pages.clear()
        layout!!.removeAllViews()
        for(i in 0 until page){
            addView()
        }
    }
    private fun setPage():Int{
        return 0
    }
    private fun getDP(value:Int):Int{ return (value*resources.displayMetrics.density).roundToInt() }
    private fun focusedIdCheck(){
        focusedPageId=(scrollY.toFloat()/resources.displayMetrics.heightPixels.toFloat()).roundToInt()+1
    }
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
    }

}