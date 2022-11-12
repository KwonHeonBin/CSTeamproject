package com.KDB.exam


import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.KDB.exam.canvasView.Companion.backgroundMode
import com.KDB.exam.canvasView.Companion.currentBrush
import com.KDB.exam.canvasView.Companion.eraser
import com.KDB.exam.canvasView.Companion.isMagnetMode
import com.KDB.exam.canvasView.Companion.mode
import com.KDB.exam.canvasView.Companion.pathList
import com.KDB.exam.canvasView.Companion.reStroke
import com.KDB.exam.canvasView.Companion.shapeMode
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
            strokeWidth=10f
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
            drawCanvasBinding.Color->{
                when(paintBrush.color){
                    Color.RED->{
                        Toast.makeText(this,"blue",Toast.LENGTH_SHORT).show()
                        paintBrush.color= Color.BLUE
                        drawCanvasBinding.Color.setBackgroundResource(R.drawable.blue_background)
                    }
                    Color.BLUE->{
                        Toast.makeText(this,"green",Toast.LENGTH_SHORT).show()
                        paintBrush.color= Color.GREEN
                        drawCanvasBinding.Color.setBackgroundResource(R.drawable.green_background)
                    }
                    Color.GREEN->{
                        Toast.makeText(this,"black",Toast.LENGTH_SHORT).show()
                        paintBrush.color= Color.BLACK
                        drawCanvasBinding.Color.setBackgroundResource(R.drawable.black_background)
                    }
                    Color.BLACK->{
                        Toast.makeText(this,"red",Toast.LENGTH_SHORT).show()
                        paintBrush.color= Color.RED
                        drawCanvasBinding.Color.setBackgroundResource(R.drawable.red_background)
                    }
                }
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
            drawCanvasBinding.shape->{
                when(shapeMode){
                    1->{
                        drawCanvasBinding.shape.setBackgroundResource(R.drawable.checkbox_blank_outline)
                        shapeMode=2
                    }
                    2->{
                        drawCanvasBinding.shape.setBackgroundResource(R.drawable.checkbox_blank)
                        shapeMode=3
                    }
                    3->{
                        drawCanvasBinding.shape.setBackgroundResource(R.drawable.circle_outline)
                        shapeMode=4
                    }
                    4->{
                        drawCanvasBinding.shape.setBackgroundResource(R.drawable.circle)
                        shapeMode=5
                    }
                    5->{
                        drawCanvasBinding.shape.setBackgroundResource(R.drawable.triangle_outline)
                        shapeMode=6
                    }
                    6->{
                        drawCanvasBinding.shape.setBackgroundResource(R.drawable.triangle)
                        shapeMode=7
                    }
                    7->{
                        drawCanvasBinding.shape.setBackgroundResource(R.drawable.minus_thick)
                        shapeMode=1
                    }
                }
                mode=3
            }
            drawCanvasBinding.background->{
                when(backgroundMode){
                    1->{
                        drawCanvasBinding.background.setBackgroundResource(R.drawable.grid)
                        backgroundMode=2
                    }
                    2->{
                        drawCanvasBinding.background.setBackgroundResource(R.drawable.reorder_horizontal)
                        backgroundMode=3
                    }
                    3->{
                        drawCanvasBinding.background.setBackgroundResource(R.drawable.checkbox_blank_outline)
                        backgroundMode=1
                    }
                }
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
            drawCanvasBinding.magnet->{
                isMagnetMode=!isMagnetMode
                Toast.makeText(this,if(isMagnetMode) "magnet" else "noMagnet",Toast.LENGTH_SHORT).show()

            }
            drawCanvasBinding.cursor->{
                mode=4
                Toast.makeText(this,"cursor",Toast.LENGTH_SHORT).show()
            }
            drawCanvasBinding.wrap->{
                mode=5
                Toast.makeText(this,"wrap",Toast.LENGTH_SHORT).show()
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
    }

}