package com.KDB.exam



import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.KDB.exam.CanvasManager.Companion.backgroundMode
import com.KDB.exam.CanvasManager.Companion.currentBrush
import com.KDB.exam.CanvasManager.Companion.focusedImg
import com.KDB.exam.CanvasManager.Companion.imgList
import com.KDB.exam.CanvasManager.Companion.isMagnetMode
import com.KDB.exam.CanvasManager.Companion.mode
import com.KDB.exam.CanvasManager.Companion.pathList
import com.KDB.exam.CanvasManager.Companion.reStroke
import com.KDB.exam.CanvasManager.Companion.shapeMode
import com.KDB.exam.CanvasManager.Companion.unStroke
import com.KDB.exam.CanvasManager.Companion.wrapAreaBox
import com.KDB.exam.databinding.DrawCanvasBinding
import kotlin.math.min


class DrawCanvas : AppCompatActivity() {

    companion object{
        var paintBrush: Paint = Paint().apply {
            isAntiAlias=true // set paintBrush
            color= currentBrush
            style=Paint.Style.STROKE
            strokeJoin=Paint.Join.ROUND
            strokeWidth=10f
            strokeCap=Paint.Cap.ROUND
        }
        lateinit var drawCanvasBinding: DrawCanvasBinding
        lateinit var scrollView:CustomScrollView
        lateinit var linerLayout:CanvasManager
    }
    private val imageResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
        if(result.resultCode== RESULT_OK){

            val imgUrl=result?.data?.data
            var img=Image(imgUrl,this,true,contentResolver)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val display = this.applicationContext?.resources?.displayMetrics    // get device size
                var ratio=if(img.bitmapImg.width>img.bitmapImg.height){min(img.bitmapImg.width.toFloat(),display?.widthPixels!!*0.5f)/img.bitmapImg.width}
                            else{min(img.bitmapImg.height.toFloat(),display?.heightPixels!!*0.5f)/img.bitmapImg.height}
                img.setImageSize((img.bitmapImg.width*ratio).toInt(),(img.bitmapImg.height*ratio).toInt())
                img.setBox()
                img.id= scrollView.focusedPageId
            }
            imgList.add(img)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        drawCanvasBinding = DrawCanvasBinding.inflate(layoutInflater)
        scrollView= drawCanvasBinding.scrollView
        linerLayout= drawCanvasBinding.LL
        scrollView.layout= linerLayout
        scrollView.canvasManager= CanvasManager(this)
        scrollView.addView()
        setContentView(drawCanvasBinding.root)
        supportActionBar?.hide()
    }

    override fun onStart() {
        super.onStart()

    }

    fun btn (view: View){       // set color fun

        when(view){
            drawCanvasBinding.Color->{
                if(mode!=1){
                    mode=1
                    scrollView.isScrollable=false
                    return
                }
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
            }
            drawCanvasBinding.clear->{        // reset list
                Toast.makeText(this,"clear",Toast.LENGTH_SHORT).show()
                //var box=pathList.clone() as ArrayList<Stroke>
                unStroke.add(pathList.clone() as ArrayList<Stroke>)
                btnActiveCheck()
                pathList.clear()
            }
            drawCanvasBinding.shape->{
                if(mode!=3){
                    mode=3
                    scrollView.isScrollable=false
                    return
                }
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
                if(mode!=2){
                    mode=2
                    Toast.makeText(this,if(scrollView.canvasManager.eraser.mode==1) "area" else "stroke",Toast.LENGTH_SHORT).show()
                    scrollView.isScrollable=false
                    return
                }
                when(scrollView.canvasManager.eraser.mode){
                    0-> scrollView.canvasManager.eraser.mode=1
                    1-> scrollView.canvasManager.eraser.mode=0
                }
                Toast.makeText(this,if(scrollView.canvasManager.eraser.mode==1) "area" else "stroke",Toast.LENGTH_SHORT).show()
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
            drawCanvasBinding.image->{
                openGallery()
                Toast.makeText(this,"image",Toast.LENGTH_SHORT).show()
                mode=4
            }
            drawCanvasBinding.hand->{
                when(scrollView.isScrollable){
                    false->{
                        scrollView.isScrollable=true
                        Toast.makeText(this,"hand",Toast.LENGTH_SHORT).show()
                        mode=7
                    }
                    true->{
                        scrollView.isScrollable=false
                        Toast.makeText(this,"pen",Toast.LENGTH_SHORT).show()
                        mode=1
                    }
                }
            }
        }
        refreshState()
    }

    private fun refreshState(){
        if(mode!=7){scrollView.isScrollable=false}
        wrapAreaBox.clearBox()
        focusedImg?.clearBox()
        focusedImg=null
    }

    private fun currentColor(color:Int){
        currentBrush=color      // set currentBrush to color
    }
    private fun btnActiveCheck(){
        when {
            unStroke.isNotEmpty() -> {drawCanvasBinding.undo.isEnabled=true}
            else -> {drawCanvasBinding.undo.isEnabled=false}
        }
        when {
            reStroke.isNotEmpty() -> {drawCanvasBinding.redo.isEnabled=true}
            else -> {drawCanvasBinding.redo.isEnabled=false}
        }
    }
    private fun openGallery(){
        val writePermission=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        if(writePermission==PackageManager.PERMISSION_DENIED||readPermission==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }
        else{
            val intent =Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
            imageResult.launch(intent)
        }
    }
}