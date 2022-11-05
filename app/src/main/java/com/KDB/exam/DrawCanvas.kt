package com.KDB.exam

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.KDB.exam.canvasView.Companion.currentBrush
import com.KDB.exam.canvasView.Companion.eraser
import com.KDB.exam.canvasView.Companion.mode
import com.KDB.exam.canvasView.Companion.pathList
import com.KDB.exam.canvasView.Companion.reStroke
import com.KDB.exam.canvasView.Companion.unStroke
import com.KDB.exam.databinding.DrawCanvasBinding


class DrawCanvas : AppCompatActivity() {

    companion object{
        var path:Path= Path()
        var paintBrush: Paint = Paint().apply {
            isAntiAlias=true // set paintBrush
            color= currentBrush
            style=Paint.Style.STROKE
            strokeJoin=Paint.Join.ROUND
            strokeWidth=8f
            strokeCap=Paint.Cap.ROUND
        }
        lateinit var drawCanvasBinding: DrawCanvasBinding
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawCanvasBinding = DrawCanvasBinding.inflate(layoutInflater)
        setContentView(drawCanvasBinding.root)
        supportActionBar?.hide()
    }

    override fun onStart() {
        super.onStart()

    }

    fun btn (view: View){       // set color fun
        when(view){
            drawCanvasBinding.redColor->{
                Toast.makeText(this,"red",Toast.LENGTH_SHORT).show()
                paintBrush.color= Color.RED
                currentColor(paintBrush.color)
                mode=1
            }
            drawCanvasBinding.blueColor->{
                Toast.makeText(this,"blue",Toast.LENGTH_SHORT).show()
                paintBrush.color= Color.BLUE
                currentColor(paintBrush.color)
                mode=1
            }
            drawCanvasBinding.blackColor->{
                Toast.makeText(this,"black",Toast.LENGTH_SHORT).show()
                paintBrush.color= Color.BLACK
                currentColor(paintBrush.color)
                mode=1
            }
            drawCanvasBinding.greenColor->{
                Toast.makeText(this,"green",Toast.LENGTH_SHORT).show()
                paintBrush.color= Color.GREEN
                currentColor(paintBrush.color)
                mode=1
            }
            drawCanvasBinding.clear->{        // reset list
                Toast.makeText(this,"clear",Toast.LENGTH_SHORT).show()
                //var box=pathList.clone() as ArrayList<Stroke>
                unStroke.add(pathList.clone() as ArrayList<Stroke>)
                btnActiveCheck()
                path.reset()
                pathList.clear()
            }
            drawCanvasBinding.undo->{
                if(unStroke.isNotEmpty()){
                    //var box=pathList.clone() as ArrayList<Stroke>
                    reStroke.add(pathList.clone() as ArrayList<Stroke>)
                    pathList.clear()
                    pathList=unStroke.removeLast().clone() as ArrayList<Stroke>
                    btnActiveCheck()
                }
            }
            drawCanvasBinding.redo->{
                if (reStroke.isNotEmpty()) {
                    //var box=pathList.clone() as ArrayList<Stroke>
                    unStroke.add(pathList.clone() as ArrayList<Stroke>)
                    pathList.clear()
                    pathList=reStroke.removeLast().clone() as ArrayList<Stroke>
                    btnActiveCheck()
                }
            }
            drawCanvasBinding.eraser->{
                mode=2
                when(eraser.mode){
                    0-> eraser.mode=1
                    1-> eraser.mode=0
                }
                Toast.makeText(this,if(eraser.mode==0) "stroke" else "area",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun currentColor(color:Int){
        currentBrush=color      // set currentBrush to color
        path=Path()     // reset path if don't use this code, change color -> change color of every line even already existed
    }
    fun btnActiveCheck(){
        when {
            unStroke.isNotEmpty() -> {drawCanvasBinding.undo.isEnabled=true}
            else -> {drawCanvasBinding.undo.isEnabled=false}
        }
        when {
            reStroke.isNotEmpty() -> {drawCanvasBinding.redo.isEnabled=true}
            else -> {drawCanvasBinding.redo.isEnabled=false}
        }
        //return btnActiveCheck()
    }

}