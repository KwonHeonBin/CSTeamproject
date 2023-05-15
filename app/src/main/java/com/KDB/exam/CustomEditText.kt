package com.KDB.exam

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatEditText

class CustomEditText: AppCompatEditText{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(DrawCanvas.basicMode){
            this.clearFocus()
            //DrawCanvas.focusedEditText=null
            return false
        }
        DrawCanvas.focusedEditText=this
        return super.onTouchEvent(event)
    }



}