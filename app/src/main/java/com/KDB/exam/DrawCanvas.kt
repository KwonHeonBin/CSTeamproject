package com.KDB.exam

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.KDB.exam.canvasView.Companion.colorList
import com.KDB.exam.canvasView.Companion.currentBrush
import com.KDB.exam.canvasView.Companion.pathList
import com.KDB.exam.databinding.DrawCanvasBinding


class DrawCanvas : AppCompatActivity() {

    private lateinit var drawCanvasBinding: DrawCanvasBinding
    companion object{
        var path:Path= Path()
        var paintBrush: Paint = Paint()
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
    fun setColor(view: View){       // set color fun
        when(view){
            drawCanvasBinding.redColor->{
                Toast.makeText(this,"red",Toast.LENGTH_SHORT).show()
                paintBrush.color= Color.RED
                currentColor(paintBrush.color)
            }
            drawCanvasBinding.blueColor->{
                Toast.makeText(this,"blue",Toast.LENGTH_SHORT).show()
                paintBrush.color= Color.BLUE
                currentColor(paintBrush.color)
            }
            drawCanvasBinding.blackColor->{
                Toast.makeText(this,"black",Toast.LENGTH_SHORT).show()
                paintBrush.color= Color.BLACK
                currentColor(paintBrush.color)
            }
            drawCanvasBinding.greenColor->{
                Toast.makeText(this,"green",Toast.LENGTH_SHORT).show()
                paintBrush.color= Color.GREEN
                currentColor(paintBrush.color)
            }
            drawCanvasBinding.clear->{        // reset list
                Toast.makeText(this,"clear",Toast.LENGTH_SHORT).show()
                pathList.clear()
                colorList.clear()
                path.reset()
            }
        }
    }
    private fun currentColor(color:Int){
        currentBrush=color      // set currentBrush to color
        path=Path()     // reset path if don't use this code, change color -> change color of every line even already existed
    }
}