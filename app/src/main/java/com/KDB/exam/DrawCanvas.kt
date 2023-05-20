package com.KDB.exam



import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.KDB.exam.CanvasManager.Companion.backgroundMode
import com.KDB.exam.CanvasManager.Companion.currentBrush
import com.KDB.exam.CanvasManager.Companion.focusedImg
import com.KDB.exam.CanvasManager.Companion.imageDeepCopy
import com.KDB.exam.CanvasManager.Companion.imgList
import com.KDB.exam.CanvasManager.Companion.isMagnetMode
import com.KDB.exam.CanvasManager.Companion.mode
import com.KDB.exam.CanvasManager.Companion.pages
import com.KDB.exam.CanvasManager.Companion.pathList
import com.KDB.exam.CanvasManager.Companion.reImg
import com.KDB.exam.CanvasManager.Companion.rePage
import com.KDB.exam.CanvasManager.Companion.reStroke
import com.KDB.exam.CanvasManager.Companion.reText
import com.KDB.exam.CanvasManager.Companion.shapeMode
import com.KDB.exam.CanvasManager.Companion.strokeDeepCopy
import com.KDB.exam.CanvasManager.Companion.textList
import com.KDB.exam.CanvasManager.Companion.unImg
import com.KDB.exam.CanvasManager.Companion.unPage
import com.KDB.exam.CanvasManager.Companion.unStroke
import com.KDB.exam.CanvasManager.Companion.unText
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

        var basicMode:Boolean=true// 1-> draw, 0-> text
        var width:Float=0f
        var height:Float=0f
        @SuppressLint("StaticFieldLeak")
        lateinit var drawCanvasBinding: DrawCanvasBinding
        @SuppressLint("StaticFieldLeak")
        lateinit var scrollView:CustomScrollView
        lateinit var linerLayout:CanvasManager
        var focusedEditText:CustomEditText?=null
    }
    var colors = arrayOf("Black", "Red", "Yellow", "Green", "Cyan", "Blue", "Magenta")                  // 색상 선택 리스트
    var bg = arrayOf("None", "Black", "Red", "Yellow", "Green", "Cyan", "Blue", "Magenta")              // 색상 선택 리스트
    var fonts = arrayOf("조선신명조", "조선가는고딕", "조선굴림체", "조선궁서체", "조선일보명조")                 // 폰트 선택 리스트
    var sizes = arrayOf("20pt", "24pt", "28pt", "32pt", "36pt", "40pt")                                 // 글자 크기 선택 리스트
    private val imageResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
        if(result.resultCode== RESULT_OK){

            val imgUrl=result?.data?.data
            val display = this.applicationContext?.resources?.displayMetrics    // get device size
            val img=Image(imgUrl,this,true,contentResolver,Pair(20f,20f))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val ratio=if(img.bitmapImg.width>img.bitmapImg.height){min(img.bitmapImg.width.toFloat(),display?.widthPixels!!*0.5f)/img.bitmapImg.width}
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
        width= this.applicationContext?.resources?.displayMetrics?.widthPixels!!.toFloat()-30// margin 으로 인한 수치 조정
        height=this.applicationContext?.resources?.displayMetrics?.heightPixels!!.toFloat()-195
        setContentView(drawCanvasBinding.root)
        supportActionBar?.hide()
        drawCanvasBinding.boldBtn.setOnClickListener(View.OnClickListener {
            val start = focusedEditText!!.selectionStart
            val end = focusedEditText!!.selectionEnd

            if (start == end)                                                                           // 드래그된 텍스트가 없으면 함수 종료
                return@OnClickListener

            val spannable: Spannable = focusedEditText!!.text as Spannable
            val styleSpans = spannable.getSpans( start, end, StyleSpan::class.java )
            var isBoldSelected = false
            for (styleSpan in styleSpans) {
                if (styleSpan.style == Typeface.BOLD) {
                    isBoldSelected = true
                }
            }
            if (isBoldSelected) {                                                                       // 드래그된 텍스트가 모두 bold 스타일이 적용되어 있으면 bold 스타일 제거
                for (styleSpan in styleSpans)
                    spannable.removeSpan(styleSpan)
            }
            else {                                                                                      // 드래그된 텍스트 중 bold 스타일이 적용되지 않은 글자가 있으면 bold 스타일 적용
                if (styleSpans.size == end - start)
                    spannable.setSpan( StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                else {
                    for (i in start until end) {
                        val styleSpansSingle = spannable.getSpans( i, i + 1, StyleSpan::class.java )
                        var isBoldSingle = false
                        for (styleSpan in styleSpansSingle) {
                            if (styleSpan.style == Typeface.BOLD) {
                                isBoldSingle = true
                                break
                            }
                        }
                        if (!isBoldSingle)
                            spannable.setSpan( StyleSpan(Typeface.BOLD), i, i + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    }
                }
            }
        })
        drawCanvasBinding.italicBtn.setOnClickListener(View.OnClickListener {
            val start = focusedEditText!!.selectionStart
            val end = focusedEditText!!.selectionEnd

            if (start == end)                                                                       // 드래그된 텍스트가 없으면 함수 종료
                return@OnClickListener

            val spannable: Spannable = focusedEditText!!.text as Spannable
            val styleSpans = spannable.getSpans( start, end, StyleSpan::class.java )
            var isItalicSelected = false
            for (styleSpan in styleSpans) {
                if (styleSpan.style == Typeface.ITALIC) {
                    isItalicSelected = true
                    break
                }
            }
            if (isItalicSelected) {                                                                 // 드래그된 텍스트가 모두 bold 스타일이 적용되어 있으면 bold 스타일 제거
                for (styleSpan in styleSpans)
                    spannable.removeSpan(styleSpan)
            }
            else {                                                                                  // 드래그된 텍스트 중 bold 스타일이 적용되지 않은 글자가 있으면 bold 스타일 적용
                if (styleSpans.size == end - start) {
                    spannable.setSpan( StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                }
                else {
                    for (i in start until end) {
                        val styleSpansSingle = spannable.getSpans( i, i + 1, StyleSpan::class.java )
                        var isItalicSingle = false
                        for (styleSpan in styleSpansSingle) {
                            if (styleSpan.style == Typeface.ITALIC) {
                                isItalicSingle = true
                                break
                            }
                        }
                        if (!isItalicSingle) {
                            spannable.setSpan( StyleSpan(Typeface.ITALIC), i, i + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                        }
                    }
                }
            }
        })
        drawCanvasBinding.underlineBtn.setOnClickListener(View.OnClickListener {
            val start = focusedEditText!!.selectionStart
            val end = focusedEditText!!.selectionEnd

            if (start == end)                                                                           // 드래그된 텍스트가 없으면 함수 종료
                return@OnClickListener

            val spannable: Spannable = focusedEditText!!.text as Spannable
            val styleSpans = spannable.getSpans( start, end, UnderlineSpan::class.java )
            var isUnderSelected = false
            for (styleSpan in styleSpans) {
                if (spannable.getSpans<UnderlineSpan>( start, end, UnderlineSpan::class.java ).isNotEmpty() ) {
                    isUnderSelected = true
                    break
                }
            }
            if (isUnderSelected) {                                                                      // 드래그된 텍스트가 모두 bold 스타일이 적용되어 있으면 underline 스타일 제거
                for (UnderlineSpan in styleSpans)
                    spannable.removeSpan(UnderlineSpan)
            }
            else {                                                                                      // 드래그된 텍스트 중 underline 스타일이 적용되지 않은 글자가 있으면 underline 스타일 적용
                if (styleSpans.size == end - start)
                    spannable.setSpan( UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                else {
                    for (i in start until end) {
                        val styleSpansSingle = spannable.getSpans( i, i + 1, UnderlineSpan::class.java )
                        var isUnderLineSingle = false
                        for (styleSpan in styleSpansSingle) {
                            if (spannable.getSpans<UnderlineSpan>( start, end, UnderlineSpan::class.java ).isNotEmpty() ) {
                                isUnderLineSingle = true
                                break
                            }
                        }
                        if (!isUnderLineSingle)
                            spannable.setSpan( UnderlineSpan(), i, i + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    }
                }
            }
        })
        drawCanvasBinding.lineBtn.setOnClickListener(View.OnClickListener {
            val start = focusedEditText!!.selectionStart
            val end = focusedEditText!!.selectionEnd

            if (start == end)                                                                       // 드래그된 텍스트가 없으면 함수 종료
                return@OnClickListener

            val spannable: Spannable = focusedEditText!!.text as Spannable
            val styleSpans = spannable.getSpans( start, end, StrikethroughSpan::class.java )
            var isStrikeSelected = false
            for (styleSpan in styleSpans) {
                if (spannable.getSpans<StrikethroughSpan>( start, end, StrikethroughSpan::class.java ).isNotEmpty() ) {
                    isStrikeSelected = true
                    break
                }
            }
            if (isStrikeSelected) {                                                                 // 드래그된 텍스트가 모두 strike 스타일이 적용되어 있으면 strike 스타일 제거
                for (StrikethroughSpan in styleSpans)
                    spannable.removeSpan(StrikethroughSpan)
            }
            else {                                                                                  // 드래그된 텍스트 중 strike 스타일이 적용되지 않은 글자가 있으면 strike 스타일 적용
                if (styleSpans.size == end - start)
                    spannable.setSpan( StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                else {
                    for (i in start until end) {
                        val styleSpansSingle = spannable.getSpans( i, i + 1, StrikethroughSpan::class.java )
                        var isStrikeSingle = false
                        for (styleSpan in styleSpansSingle) {
                            if (spannable.getSpans<StrikethroughSpan>( start, end, StrikethroughSpan::class.java ).isNotEmpty() ) {
                                isStrikeSingle = true
                                break
                            }
                        }
                        if (!isStrikeSingle)
                            spannable.setSpan( StrikethroughSpan(), i, i + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    }
                }
            }
        })
        drawCanvasBinding.alignLeft.setOnClickListener {
            focusedEditText!!.gravity = Gravity.LEFT
        }
        drawCanvasBinding.alignCenter.setOnClickListener {
            focusedEditText!!.gravity = Gravity.CENTER_HORIZONTAL
        }
        drawCanvasBinding.alignRight.setOnClickListener {
            focusedEditText!!.gravity = Gravity.RIGHT
        }
        val colorAdapt = ArrayAdapter(                                                                  // 컬러 선택 스피너와 리스트를 연결 할 어댑터 생성
            this, android.R.layout.simple_spinner_dropdown_item, colors )
        colorAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)               // 어댑터에 심플 스피너 아이템 레이아웃 적용
        drawCanvasBinding.colorSpin.adapter = colorAdapt                                                                  // 스피너에 어댑터 연결
        drawCanvasBinding.colorSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {                // 스피너에서 리스트 선택 시 동작 설정
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long ) {
                val start = focusedEditText!!.selectionStart
                val end = focusedEditText!!.selectionEnd
                val builder = SpannableStringBuilder(focusedEditText!!.text)

                when (colors[position]) {
                    "Black" -> builder.setSpan( ForegroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Red" -> builder.setSpan( ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Yellow" -> builder.setSpan( ForegroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Green" -> builder.setSpan( ForegroundColorSpan(Color.GREEN), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Cyan" -> builder.setSpan( ForegroundColorSpan(Color.CYAN), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Blue" -> builder.setSpan( ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Magenta" -> builder.setSpan( ForegroundColorSpan(Color.MAGENTA), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                }
                focusedEditText!!.text = builder
                focusedEditText!!.setSelection(start, end)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {                                   // 아무것도 선택되지 않았을 때
                // 따로 동작하는 건 없지만 onNothingSelected가 있어야 오류가 발생하지 않음
            }
        }
        val fontAdapt = ArrayAdapter(                                                                   // 폰트 선택 스피너와 리스트를 연결 할 어댑터 생성
            this, android.R.layout.simple_spinner_dropdown_item, fonts )
        fontAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)                // 어댑터에 심플 스피너 아이템 레이아웃 적용
        drawCanvasBinding.fontSpin.adapter = fontAdapt                                                                    // 스피너에 어댑터 연결
        drawCanvasBinding.fontSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {                 // 스피너에서 리스트 선택 시 동작 설정
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long ) {
                val start = focusedEditText!!.selectionStart
                val end = focusedEditText!!.selectionEnd
                val builder = SpannableStringBuilder(focusedEditText!!.text)
                when (fonts[position]) {
                    "조선신명조" -> {
                        val typeFace = Typeface.createFromAsset(assets, "ChosunSm.ttf")
                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                    "조선가는고딕" -> {
                        val typeFace = Typeface.createFromAsset(assets, "ChosunSg.ttf")
                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                    "조선굴림체" -> {
                        val typeFace = Typeface.createFromAsset(assets, "ChosunGu.ttf")
                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                    "조선궁서체" -> {
                        val typeFace = Typeface.createFromAsset(assets, "ChosunGs.ttf")
                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                    "조선일보명조" -> {
                        val typeFace = Typeface.createFromAsset(assets, "ChosunNm.ttf")
                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                }
                focusedEditText!!.text = builder
                focusedEditText!!.setSelection(start, end)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {                                   // 아무것도 선택되지 않았을 때
                // 따로 동작하는 건 없지만 onNothingSelected가 있어야 오류가 발생하지 않음
            }
        }
        val sizeAdapt = ArrayAdapter(                                                                   // 글자 크기 선택 스피너와 리스트를 연결 할 어댑터 생성
            this, android.R.layout.simple_spinner_dropdown_item, sizes )
        sizeAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)                // 어댑터에 심플 스피너 아이템 레이아웃 적용
        drawCanvasBinding.sizeSpin.adapter = sizeAdapt                                                                    // 스피너에 어댑터 연결
        drawCanvasBinding.sizeSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {                 // 스피너에서 리스트 선택 시 동작 설정
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long ) {
                val start = focusedEditText!!.selectionStart
                val end = focusedEditText!!.selectionEnd
                val builder = SpannableStringBuilder(focusedEditText!!.text)
                when (sizes[position]) {
                    "20pt" -> builder.setSpan( AbsoluteSizeSpan(48), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "24pt" -> builder.setSpan( AbsoluteSizeSpan(58), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "28pt" -> builder.setSpan( AbsoluteSizeSpan(67), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "32pt" -> builder.setSpan( AbsoluteSizeSpan(77), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "36pt" -> builder.setSpan( AbsoluteSizeSpan(86), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "40pt" -> builder.setSpan( AbsoluteSizeSpan(96), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                }
                focusedEditText!!.text = builder
                focusedEditText!!.setSelection(start, end)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }                                 // 아무것도 선택되지 않았을 때
        }
        val bgAdapt = ArrayAdapter(                                                                     // 컬러 선택 스피너와 리스트를 연결 할 어댑터 생성
            this, android.R.layout.simple_spinner_dropdown_item, bg )
        bgAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)                  // 어댑터에 심플 스피너 아이템 레이아웃 적용
        drawCanvasBinding.bgSpin.adapter = bgAdapt                                                                        // 스피너에 어댑터 연결
        drawCanvasBinding.bgSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {                   // 스피너에서 리스트 선택 시 동작 설정
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long ) {
                val start = focusedEditText!!.selectionStart
                val end = focusedEditText!!.selectionEnd
                val builder = SpannableStringBuilder(focusedEditText!!.text)
                when (bg[position]) {
                    "None" -> {
                        builder.setSpan( BackgroundColorSpan(Color.parseColor("#00000000")), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        focusedEditText!!.text = builder }
                    "Black" -> {
                        builder.setSpan( BackgroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        focusedEditText!!.text = builder }
                    "Red" -> {
                        builder.setSpan( BackgroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        focusedEditText!!.text = builder }
                    "Yellow" -> {
                        builder.setSpan( BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        focusedEditText!!.text = builder }
                    "Green" -> {
                        builder.setSpan( BackgroundColorSpan(Color.GREEN), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        focusedEditText!!.text = builder }
                    "Cyan" -> {
                        builder.setSpan( BackgroundColorSpan(Color.CYAN), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        focusedEditText!!.text = builder }
                    "Blue" -> {
                        builder.setSpan( BackgroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        focusedEditText!!.text = builder }
                    "Magenta" -> {
                        builder.setSpan( BackgroundColorSpan(Color.MAGENTA), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        focusedEditText!!.text = builder }
                }
                focusedEditText!!.setSelection(start, end)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }                                 // 아무것도 선택되지 않았을 때
        }
        btnActiveCheck()
    }

    override fun onStart() {
        super.onStart()

    }

    fun btn (view: View){       // set color fun

        when(view){
            drawCanvasBinding.mode->{
                basicMode = if(basicMode){// change to textMode
                                drawCanvasBinding.mode.setBackgroundResource(R.drawable.format_text)
                                false
                        } else{// change to penMode
                                drawCanvasBinding.mode.setBackgroundResource(R.drawable.pen)
                                mode=1// 05.20 추가
                                if(!scrollView.isScrollable){ scrollView.isScrollable=true}
                                true
                }
                Toast.makeText(this,if(basicMode) "pen" else "text",Toast.LENGTH_SHORT).show()
            }
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
                scrollView.canvasManager.addUnState(pathList, pages)// 05.20 추가
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
                    setList(true)// 05.20 추가
                    btnActiveCheck()
                }
            }
            drawCanvasBinding.redo->{
                if (reStroke.isNotEmpty()) {
                    setList(false)// 05.20 추가
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

    private fun setList(isUn:Boolean){// 05.20 추가
        if(isUn){
            scrollView.canvasManager.addReState(pathList, pages.clone() as ArrayList<canvasView>)
            pathList.clear()
            imgList.clear()
            textList.clear()
            pathList= strokeDeepCopy(unStroke.removeLast())
            imgList= imageDeepCopy(unImg.removeLast())
            textList= unText.removeLast().clone() as ArrayList<CustomEditText>
            scrollView.refreshView(unPage.removeLast(), textList)
        }
        else{
            scrollView.canvasManager.addUnState(pathList, pages.clone() as ArrayList<canvasView>)
            pathList.clear()
            imgList.clear()
            textList.clear()
            pathList= strokeDeepCopy(reStroke.removeLast())
            imgList= imageDeepCopy(reImg.removeLast())
            textList= reText.removeLast().clone() as ArrayList<CustomEditText>
            scrollView.refreshView(rePage.removeLast(), textList)
        }
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
            unStroke.isNotEmpty() -> {
                //drawCanvasBinding.undo.setBackgroundColor(Color.RED)
                drawCanvasBinding.undo.isEnabled=true
            }
            else -> {
                //drawCanvasBinding.undo.setBackgroundColor(Color.BLACK)
                drawCanvasBinding.undo.isEnabled=false
            }
        }
        when {
            reStroke.isNotEmpty() -> {
                //drawCanvasBinding.redo.setBackgroundColor(Color.RED)
                drawCanvasBinding.redo.isEnabled=true
            }
            else -> {
                //drawCanvasBinding.redo.setBackgroundColor(Color.BLACK)
                drawCanvasBinding.redo.isEnabled=false
            }
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