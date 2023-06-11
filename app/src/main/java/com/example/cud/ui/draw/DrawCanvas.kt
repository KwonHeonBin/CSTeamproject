package com.example.cud.ui.draw

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpannable
import androidx.core.text.toSpanned
import androidx.core.widget.PopupWindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.cud.MainActivity
import com.example.cud.R
import com.example.cud.Tool.DateChange
import com.example.cud.ui.draw.CanvasManager.Companion.currentBrush
import com.example.cud.ui.draw.CanvasManager.Companion.focusedImg
import com.example.cud.ui.draw.CanvasManager.Companion.imgList
import com.example.cud.ui.draw.CanvasManager.Companion.mode
import com.example.cud.ui.draw.CanvasManager.Companion.pages
import com.example.cud.ui.draw.CanvasManager.Companion.pathList
import com.example.cud.ui.draw.CanvasManager.Companion.rePage
import com.example.cud.ui.draw.CanvasManager.Companion.reStroke
import com.example.cud.ui.draw.CanvasManager.Companion.unPage
import com.example.cud.ui.draw.CanvasManager.Companion.unStroke
import com.example.cud.ui.draw.CanvasManager.Companion.wrapAreaBox
import com.example.cud.databinding.DrawCanvasBinding
import com.example.cud.dataclass.NoteDataClass
import com.example.cud.entity.Note
import com.example.cud.ui.draw.CanvasManager.Companion.backgroundMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min
import kotlin.math.roundToInt
import com.example.cud.ui.draw.CanvasManager.Companion.imageDeepCopy
import com.example.cud.ui.draw.CanvasManager.Companion.isMagnetMode
import com.example.cud.ui.draw.CanvasManager.Companion.posX
import com.example.cud.ui.draw.CanvasManager.Companion.posY
import com.example.cud.ui.draw.CanvasManager.Companion.reImg
import com.example.cud.ui.draw.CanvasManager.Companion.reText
import com.example.cud.ui.draw.CanvasManager.Companion.shapeMode
import com.example.cud.ui.draw.CanvasManager.Companion.strokeDeepCopy
import com.example.cud.ui.draw.CanvasManager.Companion.textList
import com.example.cud.ui.draw.CanvasManager.Companion.unImg
import com.example.cud.ui.draw.CanvasManager.Companion.unText
import com.example.cud.viewModel.NotesViewModel
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.skydoves.balloon.*
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.rememberBalloonBuilder


class DrawCanvas : AppCompatActivity() {
    val editNoteExtra = "edit_note_extra"
    private lateinit var note: Note
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var balloon: Balloon
    private lateinit var pageSetting: Balloon
    private var isUpdate = false
    private var dateChange = DateChange()
    private lateinit var NoteData: NoteDataClass


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
        lateinit var scrollView: CustomScrollView
        lateinit var linerLayout: CanvasManager
        var focusedEditText: CustomEditText?=null

    }
    var fonts = arrayOf("조선신명조", "조선가는고딕", "조선굴림체", "조선궁서체", "조선일보명조")                 // 폰트 선택 리스트
    var sizes = arrayOf("20pt", "24pt", "28pt", "32pt", "36pt", "40pt")                                 // 글자 크기 선택 리스트
    private val imageResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->
        if(result.resultCode== RESULT_OK){
            val imgUrl=result?.data?.data
            contentResolver.takePersistableUriPermission(imgUrl!!,Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val display = this.applicationContext?.resources?.displayMetrics    // get device size
            val img= Image(imgUrl,this,true,contentResolver,Pair(20f,20f),id = scrollView.focusedPageId )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val ratio=if(img.bitmapImg.width>img.bitmapImg.height){min(img.bitmapImg.width.toFloat(),display?.widthPixels!!*0.4f)/img.bitmapImg.width}
                            else{min(img.bitmapImg.height.toFloat(),display?.heightPixels!!*0.4f)/img.bitmapImg.height}
                img.setImageSize((img.bitmapImg.width*ratio).toInt(),(img.bitmapImg.height*ratio).toInt())
                img.setImgWH()
                // TODO 일부 사진의 경우 크기가 이상하게 표시됨
            }
            imgList.add(img)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clearCompanion()
        drawCanvasBinding = DrawCanvasBinding.inflate(layoutInflater)
        scrollView = drawCanvasBinding.scrollView
        linerLayout = drawCanvasBinding.LL
        scrollView.layout= linerLayout
        scrollView.canvasManager= CanvasManager(this)
        width = this.applicationContext?.resources?.displayMetrics?.widthPixels!!.toFloat()-30// margin 으로 인한 수치 조정
        height =this.applicationContext?.resources?.displayMetrics?.heightPixels!!.toFloat()-195
        setContentView(drawCanvasBinding.root)
        setSupportActionBar(drawCanvasBinding.drawToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        balloon = createBalloon(context = this){
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setIsVisibleArrow(false)
            setCornerRadius(10f)
            setPadding(10)
            setLayout(R.layout.popup_draw)
            setBackgroundColor(getResources().getColor(R.color.white))
            build()
        }

        pageSetting = createBalloon(this){
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setIsVisibleArrow(false)
            setCornerRadius(10f)
            setPadding(10)
            setLayout(R.layout.popuppage)
            setBackgroundColor(getResources().getColor(R.color.white))
            build()
        }

        initView()
        initViewModel()
        initListener()

        // do while문을 이용해서 받은 page 수만큼 반복
        // note는 사용하면 안됨

        val test = focusedEditText?.text

        drawCanvasBinding.drawView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppCompatTheme{
                    val start = focusedEditText!!.selectionStart
                    val end = focusedEditText!!.selectionEnd
                    val builder = SpannableStringBuilder(focusedEditText!!.text)

                    val ctx = LocalContext.current
                    val configuration = LocalConfiguration.current
                    val screenDensity = configuration.densityDpi/ 160f
                    val screenHeight = configuration.screenHeightDp.toFloat() * screenDensity
                    val screenWidth = configuration.screenWidthDp.toFloat() * screenDensity

                    val toggleCustomColorDialog = remember{ mutableStateOf(false)}

                    if(toggleCustomColorDialog.value) {
                        ColorPickerDialog(setShowDialog = {toggleCustomColorDialog.value = it})
                    }
                    // 텍스트 세팅 팝업
                    val toggleTextSettingPopup = remember{ mutableStateOf(false)}
                    // 펜 색상 팝업
                    val toggleDrawColorPopup = remember{ mutableStateOf(false)}
                    // 글 색상 팝업
                    val toggleTextColorPopup = remember{ mutableStateOf(false)}
                    // 커스텀 색상 팝업
                    // 메뉴 팝업

                    var toggleTextFont by remember { mutableStateOf(false)}

                    var toggleTextSize by remember { mutableStateOf(false)}


                    var toolCheck = remember {
                        mutableStateOf(false)
                    }

                    var offsetX by remember {
                        mutableStateOf(0f)
                    }

                    var offsetY by remember {
                        mutableStateOf( screenHeight - 200f )
                    }

                    var cursorIcon by remember{
                        mutableStateOf(false)
                    }

                    val balloonBuilder = rememberBalloonBuilder{
                        setWidth(BalloonSizeSpec.WRAP)
                        setHeight(BalloonSizeSpec.WRAP)
                        setPadding(10)
                        setCornerRadius(15f)
                        setIsVisibleArrow(false)
                        setBackgroundColor(getResources().getColor(R.color.white))
                        setLifecycleOwner(lifecycleOwner)
                }
                    val dropdwonBuilder = rememberBalloonBuilder{
                        setWidth(BalloonSizeSpec.WRAP)
                        setHeight(BalloonSizeSpec.WRAP)
                        setPadding(10)
                        setCornerRadius(15f)
                        setIsVisibleArrow(false)
                        setBackgroundColor(getResources().getColor(R.color.white))
                        setLifecycleOwner(lifecycleOwner)
                    }

                    // 텍스트 세팅
                    if(toggleTextSettingPopup.value)
                        if(offsetY > (screenHeight/2))
                        {
                            TextSettingPopup(offsetX = offsetX + 25, offsetY = offsetY - 320, togglePopup = {toggleTextSettingPopup.value = it})
                        }else{
                            TextSettingPopup(offsetX = offsetX + 25, offsetY = offsetY + 100, togglePopup = {toggleTextSettingPopup.value = it})

                        }
                    // 펜 설정 오프셋은 나중에 실습용 폰에 맞춰 설정
                    if(toggleDrawColorPopup.value)
                        if(offsetY > (screenHeight/2))
                        {
                            DrawColorPopup(offsetX = offsetX + 25, offsetY = offsetY - 190, togglePopup = {toggleDrawColorPopup.value = it})
                        }else{
                            DrawColorPopup(offsetX = offsetX + 25, offsetY = offsetY + 100, togglePopup = {toggleDrawColorPopup.value = it})

                        }

                    if(toggleTextColorPopup.value)
                        if(offsetY > (screenHeight/2))
                        {
                            TextColorPopup(offsetX = offsetX + 25, offsetY = offsetY - 190, togglePopup = {toggleTextColorPopup.value = it})
                        }else{
                            TextColorPopup(offsetX = offsetX + 25, offsetY = offsetY + 100, togglePopup = {toggleTextColorPopup.value = it})

                        }

                    BoxWithConstraints(
                        Modifier
                            .width(screenWidth.dp)
                            .padding(9.dp)) {
                        val pWidth = constraints.maxWidth
                        val pHeight = constraints.maxHeight + 600f
                        Card(Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max)
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            .pointerInput(Unit) {
                                val boxSize = this.size
                                detectDragGesturesAfterLongPress { _, dragAmount ->
                                    offsetX = (offsetX + dragAmount.x).coerceIn(
                                        0f,
                                        pWidth - boxSize.width.toFloat()
                                    )
                                    offsetY = (offsetY + dragAmount.y).coerceIn(
                                        200f,
                                        pHeight - boxSize.height.toFloat()
                                    )
                                }
                            },elevation = 5.dp,shape = RoundedCornerShape(5.dp)
                        ) {
                            Row(Modifier.width(IntrinsicSize.Max), verticalAlignment = CenterVertically) {
                                IconButton(onClick = {
                                    if(mode!=1){
                                        mode=1
                                    }else{
                                        basicMode = if (basicMode) {// change to textMode
                                            toolCheck.value = true
                                            false
                                        } else {// change to penMode
                                            toolCheck.value = false
                                            mode=1
                                            true
                                        }
                                    }
                                },
                                    Modifier
                                        .clip(CircleShape)
                                ) {
                                    if(toolCheck.value){
                                        Icon(painter = painterResource(id = R.drawable.edit_note), contentDescription = "")
                                    }else
                                    {
                                        Icon(painter = painterResource(id = R.drawable.draw_check), contentDescription = "")
                                    }
                                }
                                if(toolCheck.value){
                                    //Text Setting
                                    Balloon(
                                        Modifier,
                                        balloonBuilder,
                                        balloonContent = {
                                            Box(
                                                modifier = Modifier
                                            ){
                                                Column(
                                                    modifier = Modifier.padding(10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(10.dp)
                                                    ) {
                                                        Text(text = "텍스트 옵션")
                                                    }
                                                    Row(
                                                        modifier = Modifier
                                                    ){
                                                        Box(
                                                            modifier = Modifier
                                                                .width(IntrinsicSize.Max)
                                                                .padding(1.dp)
                                                        ){
                                                            // 차후 토글 버튼으로 모두 변경
                                                            Row(
                                                                Modifier
                                                            ){
                                                                ToolButton(image = R.drawable.format_align_left) {
                                                                    focusedEditText!!.gravity = Gravity.LEFT
                                                                }
                                                                ToolButton(image = R.drawable.format_align_center) {
                                                                    focusedEditText!!.gravity = Gravity.CENTER_HORIZONTAL
                                                                }
                                                                ToolButton(image = R.drawable.format_align_right) {
                                                                    focusedEditText!!.gravity = Gravity.RIGHT
                                                                }
                                                            }
                                                        }

                                                    }
                                                    Spacer(modifier = Modifier.height(7.dp))
                                                    Row(
                                                        Modifier
                                                    ){
                                                        Box(
                                                            modifier = Modifier
                                                                .width(IntrinsicSize.Max)
                                                                .padding(1.dp)
                                                        ){
                                                            Row(Modifier)
                                                            {
                                                                ToolButton(image = R.drawable.format_bold) {
                                                                    val start = focusedEditText!!.selectionStart
                                                                    val end = focusedEditText!!.selectionEnd
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
                                                                }
                                                                ToolButton(image = R.drawable.format_italic) {
                                                                    val start = focusedEditText!!.selectionStart
                                                                    val end = focusedEditText!!.selectionEnd

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
                                                                }
                                                                ToolButton(image = R.drawable.format_underlined) {
                                                                    val start = focusedEditText!!.selectionStart
                                                                    val end = focusedEditText!!.selectionEnd

                                                                    val spannable: Spannable = focusedEditText!!.text as Spannable
                                                                    val styleSpans = spannable.getSpans( start, end, UnderlineSpan::class.java )
                                                                    var isUnderSelected = false
                                                                    for (styleSpan in styleSpans) {
                                                                        if (spannable.getSpans<UnderlineSpan>( start, end, UnderlineSpan::class.java ).isNotEmpty() ) {
                                                                            isUnderSelected = true
                                                                            break
                                                                        }
                                                                    }
                                                                    if (isUnderSelected) {
                                                                        for (UnderlineSpan in styleSpans)
                                                                            spannable.removeSpan(UnderlineSpan)
                                                                    }
                                                                    else {
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
                                                                }
                                                                ToolButton(image = R.drawable.format_strikethrough) {
                                                                    val start = focusedEditText!!.selectionStart
                                                                    val end = focusedEditText!!.selectionEnd

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
                                                                }
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(7.dp))
                                                    // TODO Text Color
                                                    Column(Modifier.padding(10.dp)) {
                                                        Text(text = "텍스트 색상")
                                                        Row(Modifier.padding(top = 10.dp)){
                                                            //# == 0xFF
                                                            // 빨강
                                                            TextColorButton(paintcolor = 0xFFFF0000,"#FF0000")
                                                            // 주황
                                                            TextColorButton(paintcolor = 0xFFfc9803,"#fc9803")
                                                            // 노랑
                                                            TextColorButton(paintcolor = 0xFFfcf403,"#fcf403")
                                                            // 연두
                                                            TextColorButton(paintcolor = 0xFFa1fc03,"#a1fc03")
                                                            // 초록
                                                            TextColorButton(paintcolor = 0xFF03fc1c,"#03fc1c")
                                                            // 파랑
                                                            TextColorButton(paintcolor = 0xFF0000FF,"#0000FF")
                                                            // 보라
                                                            TextColorButton(paintcolor = 0xFF6203fc,"#6203fc")
                                                            // 검정
                                                            TextColorButton(paintcolor = 0xFF000000,"#000000")
                                                            // 추가 색상
                                                            Button(onClick = { toggleCustomColorDialog.value = true },
                                                                shape = RoundedCornerShape(5.dp),
                                                                modifier = Modifier.size(20.dp))
                                                            {}
                                                        }

                                                        DashedDivider(thickness = 1.dp, modifier = Modifier.padding(top = 10.dp))

                                                        Text(text = "텍스트 배경 색상")
                                                        Row(Modifier.padding(top = 10.dp)){
                                                            //# == 0xFF
                                                            // 빨강
                                                            TextBGColorButton(paintcolor = 0xFFFF0000,"#FF0000")
                                                            // 주황
                                                            TextBGColorButton(paintcolor = 0xFFfc9803,"#fc9803")
                                                            // 노랑
                                                            TextBGColorButton(paintcolor = 0xFFfcf403,"#fcf403")
                                                            // 연두
                                                            TextBGColorButton(paintcolor = 0xFFa1fc03,"#a1fc03")
                                                            // 초록
                                                            TextBGColorButton(paintcolor = 0xFF03fc1c,"#03fc1c")
                                                            // 파랑
                                                            TextBGColorButton(paintcolor = 0xFF0000FF,"#0000FF")
                                                            // 보라
                                                            TextBGColorButton(paintcolor = 0xFF6203fc,"#6203fc")
                                                            // 검정
                                                            TextBGColorButton(paintcolor = 0xFF000000,"#000000")
                                                            // 투명 색 임의로 넣었음
                                                            TextBGColorButton(paintcolor = 0xFFFc001c , xmlcolor = "#00000000")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    ){
                                        IconButton(onClick = {
                                            if(offsetY > (screenHeight/2))
                                            {
                                                it.showAlignTop()
                                            }else{
                                                it.showAlignBottom()
                                            }}) {
                                            Icon(painter = painterResource(id = R.drawable.title),
                                                modifier = Modifier.padding(2.dp),
                                                contentDescription = null)
                                        }
                                    }

                                    //글자크기
                                    ToolButton(image = R.drawable.format_size) {
                                        toggleTextSize = true
                                    }
                                    //폰트
                                    ToolButton(image = R.drawable.format_text){
                                        toggleTextFont = true
                                    }

                                    ToolButton(image = R.drawable.hand_back_left_outline) {
                                        when(scrollView.isScrollable){
                                            false->{
                                                scrollView.isScrollable=true
                                                Toast.makeText(ctx,"드래그 기능 활성화",Toast.LENGTH_SHORT).show()
                                                mode=7
                                                cursorIcon = true
                                            }
                                            true->{
                                                scrollView.isScrollable=false
                                                Toast.makeText(ctx,"커서 기능 활성화",Toast.LENGTH_SHORT).show()
                                                mode=4
                                                cursorIcon = false
                                            }
                                        }
                                    }
                                    ToolButton(image = R.drawable.arrow_u_left_top) {
                                        if(unStroke.isNotEmpty()){
                                            setList(true)// 05.20 추가
                                        }
                                    }
                                    ToolButton(image = R.drawable.arrow_u_right_top) {
                                        if (reStroke.isNotEmpty()) {
                                            setList(false)// 05.20 추가
                                        }
                                    }
                                }else{
                                    IconButton(onClick = {
                                        if(mode!=2){
                                            mode=2
                                            scrollView.canvasManager.eraser.mode=0
                                            scrollView.isScrollable=false
                                        }
                                        if(scrollView.canvasManager.eraser.mode == 0){
                                            scrollView.canvasManager.eraser.mode=1
                                        }else if(scrollView.canvasManager.eraser.mode == 1){
                                            scrollView.canvasManager.eraser.mode=0
                                        }
                                    },Modifier.pointerInput(Unit){
                                        detectTapGestures(onDoubleTap = {
                                            Toast.makeText(ctx,"clear",Toast.LENGTH_SHORT).show()
                                            //var box=pathList.clone() as ArrayList<Stroke>
                                            scrollView.canvasManager.addUnState(pathList.clone() as ArrayList<Stroke>, pages)
                                            //unStroke.add(pathList.clone() as ArrayList<Stroke>)
                                            pathList.clear()
                                        })
                                    }){
                                        Icon(painter = painterResource(id = R.drawable.eraser),
                                            modifier = Modifier.padding(2.dp),
                                            contentDescription = null)
                                    }
                                    //PenSetting
                                    Balloon(
                                        Modifier,
                                        balloonBuilder,
                                        balloonContent = {
                                            Box {
                                                Column(Modifier.padding(10.dp)) {
                                                    Row(Modifier.padding(10.dp)) {
                                                        Text(text = "펜 설정")
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .width(IntrinsicSize.Max)
                                                            .padding(10.dp)
                                                    ) {
                                                        Column {
                                                            Text(text = "도형 그리기")
                                                            Row {
                                                                ToolButton(image = R.drawable.minus_thick,1) {
                                                                    mode=3
                                                                    scrollView.isScrollable=false
                                                                    shapeMode=1
                                                                }
                                                                ToolButton(image = R.drawable.checkbox_blank_outline,1) {
                                                                    mode=3
                                                                    scrollView.isScrollable=false
                                                                    shapeMode=2
                                                                }
                                                                ToolButton(image = R.drawable.checkbox_blank,1) {
                                                                    mode=3
                                                                    scrollView.isScrollable=false
                                                                    shapeMode=3
                                                                }
                                                                ToolButton(image = R.drawable.circle_outline,1) {
                                                                    mode=3
                                                                    scrollView.isScrollable=false
                                                                    shapeMode=4
                                                                }
                                                            }
                                                            Row{
                                                                ToolButton(image = R.drawable.circle,1) {
                                                                    mode=3
                                                                    scrollView.isScrollable=false
                                                                    shapeMode=5
                                                                }
                                                                ToolButton(image = R.drawable.triangle_outline,1) {
                                                                    mode=3
                                                                    scrollView.isScrollable=false
                                                                    shapeMode=6
                                                                }
                                                                ToolButton(image = R.drawable.triangle,1) {
                                                                    mode=3
                                                                    scrollView.isScrollable=false
                                                                    shapeMode=7
                                                                }
                                                                ToolButton(image = R.drawable.magnet,1) {
                                                                    isMagnetMode=!isMagnetMode
                                                                    Toast.makeText(ctx,if(isMagnetMode) "자석 기능 활성화" else "자석 기능 비활성화",Toast.LENGTH_SHORT).show()
                                                                }
                                                            }
                                                            // 크기 조정 슬라이더
                                                            Row{
                                                                Column{
                                                                    Text("펜 크기")
                                                                    var sliderPosition by remember {
                                                                        mutableStateOf(paintBrush.strokeWidth)
                                                                    }
                                                                    Slider(
                                                                        value = sliderPosition,
                                                                        onValueChange = {
                                                                            sliderPosition = it
                                                                            paintBrush.strokeWidth = it
                                                                        },
                                                                        Modifier.size(150.dp,15.dp),
                                                                        valueRange = 1f..60f)
                                                                }
                                                            }
                                                            DashedDivider(thickness = 1.dp, modifier = Modifier.padding(top = 10.dp))
                                                            Text(text = "펜 색상")
                                                            Row {
                                                                //# == 0xFF
                                                                // 빨강
                                                                ColorButton(
                                                                    paintcolor = 0xFFFF0000,
                                                                    "#FF0000"
                                                                )
                                                                // 주황
                                                                ColorButton(
                                                                    paintcolor = 0xFFfc9803,
                                                                    "#fc9803"
                                                                )
                                                                // 노랑
                                                                ColorButton(
                                                                    paintcolor = 0xFFfcf403,
                                                                    "#fcf403"
                                                                )
                                                                // 연두
                                                                ColorButton(
                                                                    paintcolor = 0xFFa1fc03,
                                                                    "#a1fc03"
                                                                )
                                                                // 초록
                                                                ColorButton(
                                                                    paintcolor = 0xFF03fc1c,
                                                                    "#03fc1c"
                                                                )
                                                                // 파랑
                                                                ColorButton(
                                                                    paintcolor = 0xFF0000FF,
                                                                    "#0000FF"
                                                                )
                                                                // 보라
                                                                ColorButton(
                                                                    paintcolor = 0xFF6203fc,
                                                                    "#6203fc"
                                                                )
                                                                // 검정
                                                                ColorButton(
                                                                    paintcolor = 0xFF000000,
                                                                    "#000000"
                                                                )
                                                                // 추가 색상
                                                                Button(
                                                                    onClick = {
                                                                        toggleCustomColorDialog.value =
                                                                            true
                                                                    },
                                                                    shape = RoundedCornerShape(5.dp),
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                                {}
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    ){
                                        IconButton(onClick = {
                                            if(mode==1||mode==3){
                                                if(offsetY > (screenHeight/2))
                                                {
                                                    it.showAlignTop()
                                                }else{
                                                    it.showAlignBottom()
                                                }
                                            }else
                                            {
                                                mode=1
                                                Toast.makeText(ctx,"펜 기능 활성화",Toast.LENGTH_SHORT).show()
                                                scrollView.isScrollable=false
                                            }
                                        }) {
                                            Icon(painter = painterResource(id = R.drawable.pen),
                                                modifier = Modifier.padding(2.dp),
                                                contentDescription = null)
                                        }
                                    }
                                    IconButton(onClick = {
                                        if(mode == 4){
                                            when(scrollView.isScrollable){
                                                false->{
                                                    scrollView.isScrollable=true
                                                    Toast.makeText(ctx,"hand",Toast.LENGTH_SHORT).show()
                                                    mode=7
                                                    cursorIcon = true
                                                }
                                                true->{
                                                    scrollView.isScrollable=false
                                                    Toast.makeText(ctx,"pen",Toast.LENGTH_SHORT).show()
                                                    mode=1
                                                    cursorIcon = false
                                                }
                                            }
                                        }else{
                                            scrollView.isScrollable=false
                                            mode=4
                                            Toast.makeText(ctx,"cursor",Toast.LENGTH_SHORT).show()
                                            cursorIcon = false
                                        }
                                    }){
                                        if(cursorIcon){
                                            Icon(painter = painterResource(id = R.drawable.hand_back_left_outline),
                                                modifier = Modifier.padding(2.dp),
                                                contentDescription = null)
                                        }else{
                                            Icon(painter = painterResource(id = R.drawable.cursor_default_outline),
                                                modifier = Modifier.padding(2.dp),
                                                contentDescription = null)
                                        }
                                    }
                                    ToolButton(image = R.drawable.border_none_variant) {
                                        mode=5
                                        Toast.makeText(ctx,"wrap",Toast.LENGTH_SHORT).show()
                                    }
                                    ToolButton(image = R.drawable.folder_image) { // 왠지 안됨
                                        openGallery()
                                        Toast.makeText(ctx,"image",Toast.LENGTH_SHORT).show()
                                        mode=4
                                    }
                                    // 도형
                                    ToolButton(image = R.drawable.arrow_u_left_top) {
                                        if(unStroke.isNotEmpty()){
                                            setList(true)// 05.20 추가
                                        }
                                    }
                                    ToolButton(image = R.drawable.arrow_u_right_top) {
                                        if (reStroke.isNotEmpty()) {
                                            setList(false)// 05.20 추가
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // 폰트 사이즈
                    DropdownMenu(expanded = toggleTextSize, onDismissRequest = { toggleTextSize = false }) {
                        sizes.forEach { 
                            DropdownMenuItem(onClick = {
                                    when (it) {
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
                            ) {
                                Text(text = it)
                            }
                        }
                    }
                    // 폰트 설정
                    DropdownMenu(expanded = toggleTextFont, onDismissRequest = { toggleTextFont = false }) {
                        fonts.forEach{
                            DropdownMenuItem(onClick = {
                                when (it) {
                                    "조선신명조" ->{
                                        val typeFace = Typeface.createFromAsset(assets, "ChosunSm.ttf")
                                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                                    "조선가는고딕" ->{
                                        val typeFace = Typeface.createFromAsset(assets, "ChosunSg.ttf")
                                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                                    "조선굴림체" ->{
                                        val typeFace = Typeface.createFromAsset(assets, "ChosunGu.ttf")
                                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                                    "조선궁서체" ->{
                                        val typeFace = Typeface.createFromAsset(assets, "ChosunGs.ttf")
                                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                                    "조선일보명조" ->{
                                        val typeFace = Typeface.createFromAsset(assets, "ChosunNm.ttf")
                                        builder.setSpan( TypefaceSpan(typeFace), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )}
                                }
                                focusedEditText!!.text = builder
                                focusedEditText!!.setSelection(start, end)
                            }) {
                                Text(text = it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun deleteNote(note: Note) {
        notesViewModel.deleteNote(note)
        Toast.makeText(this@DrawCanvas, "노트 지워짐", Toast.LENGTH_SHORT).show()
    }

    private fun initView(){
        if(intent.getParcelableExtra<Note>(editNoteExtra) != null){
            isUpdate = true

            val delete: Button = balloon.getContentView().findViewById(R.id.dodeletebt)

            delete.isClickable = true

            note = intent.getParcelableExtra(editNoteExtra)!!
            drawCanvasBinding.title.setText(note.title)
            //focusedEditText.setText()
            for(page: Int in 0 until note.pageSize!!){
                scrollView.addView(note.span[page], note.gravity[page])
            }
            addPathData()
            addImgData()
            addTextData()
            // TODO
        }else{
            scrollView.addView()
        }
    }

    private fun initViewModel(){
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
    }

    private fun initListener(){

        val save: Button = balloon.getContentView().findViewById(R.id.dosavebt)
        val delete: Button = balloon.getContentView().findViewById(R.id.dodeletebt)
        val broom: Button = balloon.getContentView().findViewById(R.id.broomButton)
        val pSetting: Button = balloon.getContentView().findViewById(R.id.pageButton)
        val blankbt: Button = pageSetting.getContentView().findViewById(R.id.nolinebt)
        val onelinebt: Button = pageSetting.getContentView().findViewById(R.id.onelinebt)
        val gridbt: Button = pageSetting.getContentView().findViewById(R.id.linebt)
        val okButton: Button = pageSetting.getContentView().findViewById(R.id.okbt)

        save.setOnClickListener{
            var title = drawCanvasBinding.title.text.toString()
            val date = dateChange.getToday()
            val time = dateChange.getTime()
            NoteData = NoteDataClass()
            //getTextDateClass()
            getImgDateClass()
            getPathDateClass()
            getTextDateClass()

            if (title.isEmpty()){
                title = "제목 없는 노트"
            }else {
                if(isUpdate){
                    notesViewModel.updateNote(
                        Note(
                            id = note.id,
                            title = title,
                            date = note.date,
                            time = note.time,
                            width = NoteData.width,
                            pColor = NoteData.pColor,
                            pPos_X = NoteData.pPos_X,
                            pPos_Y = NoteData.pPos_Y,
                            pId = NoteData.pId,
                            uri = NoteData.uri,
                            iPos_X = NoteData.iPos_X,
                            iPos_Y = NoteData.iPos_Y,
                            isXFlip = NoteData.isXFlip,
                            isYFlip = NoteData.isYFlip,
                            imageDegree = NoteData.imageDegree,
                            iXSize = NoteData.iXSize,
                            iYSize = NoteData.iYSize,
                            iId = NoteData.iId,
                            span = NoteData.span,
                            gravity = NoteData.gravity,
                            pageSize = pages.size
                            // TODO
                        )
                    )
                }else{
                    notesViewModel.insertnote(
                        Note(
                            title = title,
                            date = date,
                            time = time,
                            width = NoteData.width,
                            pColor = NoteData.pColor,
                            pPos_X = NoteData.pPos_X,
                            pPos_Y = NoteData.pPos_Y,
                            pId = NoteData.pId,
                            uri = NoteData.uri,
                            iPos_X = NoteData.iPos_X,
                            iPos_Y = NoteData.iPos_Y,
                            isXFlip = NoteData.isXFlip,
                            isYFlip = NoteData.isYFlip,
                            imageDegree = NoteData.imageDegree,
                            iXSize = NoteData.iXSize,
                            iYSize = NoteData.iYSize,
                            iId = NoteData.iId,
                            span = NoteData.span,
                            gravity = NoteData.gravity,
                            pageSize = pages.size,
                            // TODO
                        )
                    )
                }
                Toast.makeText(this@DrawCanvas,"노트 저장됨",Toast.LENGTH_SHORT).show()
                intent = Intent(this,MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }
        if(isUpdate){
            delete.setOnClickListener {
                deleteNote(note)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            }
        }
        drawCanvasBinding.menubt.setOnClickListener {
            balloon.showAlignRight(drawCanvasBinding.menubt)
        }

        broom.setOnClickListener {
            Toast.makeText(this,"화면을 청소했습니다",Toast.LENGTH_SHORT).show()
            scrollView.canvasManager.addUnState(pathList, pages)// 05.20 추가
            pathList.clear()
            balloon.dismiss()
        }

        pSetting.setOnClickListener {
            pageSetting.showAtCenter(pSetting)
        }

        blankbt.setOnClickListener {
            backgroundMode=1
        }

        onelinebt.setOnClickListener {
            backgroundMode=3
        }

        gridbt.setOnClickListener {
            backgroundMode=2
        }

        okButton.setOnClickListener {
            pageSetting.dismiss()
        }

    }

    @Composable
    fun ToolButton(image : Int,padding: Int = 2,onButtonClicked:() -> Unit){
        IconButton(onClick = onButtonClicked){
            Icon(painter = painterResource(id = image),
                modifier = Modifier.padding(padding.dp),
                contentDescription = null)
        }
    }

    @Composable
    fun TextSettingPopup(offsetX:Float,offsetY:Float,togglePopup:(Boolean) -> Unit){
        Popup(
            offset = IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
            onDismissRequest = {togglePopup(false)},
            properties = PopupProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true,
                focusable = true
            )
        ) {
            Surface(
                modifier = Modifier
                    .width(intrinsicSize = IntrinsicSize.Max)
                    .height(IntrinsicSize.Max),
                shape = RoundedCornerShape(16.dp),
                elevation = 5.dp
            ) {
                Box(
                    modifier = Modifier
                ){
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(text = "텍스트 옵션")
                        }
                        Row(
                            modifier = Modifier
                        ){
                            Box(
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                                    .padding(1.dp)
                            ){
                                // 차후 토글 버튼으로 모두 변경
                                Row(
                                    Modifier
                                ){
                                    ToolButton(image = R.drawable.format_align_left) {
                                        focusedEditText!!.gravity = Gravity.LEFT
                                    }
                                    ToolButton(image = R.drawable.format_align_center) {
                                        focusedEditText!!.gravity = Gravity.CENTER_HORIZONTAL
                                    }
                                    ToolButton(image = R.drawable.format_align_right) {
                                        focusedEditText!!.gravity = Gravity.RIGHT
                                    }
                                }
                            }

                        }
                        Spacer(modifier = Modifier.height(7.dp))
                        Row(Modifier){
                            Box(
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                                    .padding(1.dp)
                            ){
                                Row(Modifier)
                                {
                                    ToolButton(image = R.drawable.format_bold) {
                                        val start = focusedEditText!!.selectionStart
                                        val end = focusedEditText!!.selectionEnd
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
                                    }
                                    ToolButton(image = R.drawable.format_italic) {
                                        val start = focusedEditText!!.selectionStart
                                        val end = focusedEditText!!.selectionEnd

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
                                    }
                                    ToolButton(image = R.drawable.format_underlined) {
                                        val start = focusedEditText!!.selectionStart
                                        val end = focusedEditText!!.selectionEnd

                                        val spannable: Spannable = focusedEditText!!.text as Spannable
                                        val styleSpans = spannable.getSpans( start, end, UnderlineSpan::class.java )
                                        var isUnderSelected = false
                                        for (styleSpan in styleSpans) {
                                            if (spannable.getSpans<UnderlineSpan>( start, end, UnderlineSpan::class.java ).isNotEmpty() ) {
                                                isUnderSelected = true
                                                break
                                            }
                                        }
                                        if (isUnderSelected) {
                                            for (UnderlineSpan in styleSpans)
                                                spannable.removeSpan(UnderlineSpan)
                                        }
                                        else {
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
                                    }
                                    ToolButton(image = R.drawable.format_strikethrough) {
                                        val start = focusedEditText!!.selectionStart
                                        val end = focusedEditText!!.selectionEnd

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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 이름 바꾸기
    @Composable
    fun TextColorPopup(offsetX: Float,offsetY: Float,togglePopup: (Boolean) -> Unit){
        val toggleCustomColorDialog = remember{ mutableStateOf(false)}

        if(toggleCustomColorDialog.value) {
            ColorPickerDialog(setShowDialog = {toggleCustomColorDialog.value = it})
        }

        Popup(
            offset = IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
            onDismissRequest = { togglePopup(false) },
            properties = PopupProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true,
                focusable = true)
        ) {
            Surface(
                modifier = Modifier
                    .width(intrinsicSize = IntrinsicSize.Max)
                    .height(IntrinsicSize.Max),
                shape = RoundedCornerShape(16.dp),
                elevation = 5.dp
            ) {
                Box{
                    Column(Modifier.padding(10.dp)){
                        Box(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .padding(1.dp)
                        ){
                            Column(Modifier.padding(10.dp)) {
                                Text(text = "텍스트 색상")
                                Row(Modifier.padding(top = 10.dp)){
                                    //# == 0xFF
                                    // 빨강
                                    TextColorButton(paintcolor = 0xFFFF0000,"#FF0000")
                                    // 주황
                                    TextColorButton(paintcolor = 0xFFfc9803,"#fc9803")
                                    // 노랑
                                    TextColorButton(paintcolor = 0xFFfcf403,"#fcf403")
                                    // 연두
                                    TextColorButton(paintcolor = 0xFFa1fc03,"#a1fc03")
                                    // 초록
                                    TextColorButton(paintcolor = 0xFF03fc1c,"#03fc1c")
                                    // 파랑
                                    TextColorButton(paintcolor = 0xFF0000FF,"#0000FF")
                                    // 보라
                                    TextColorButton(paintcolor = 0xFF6203fc,"#6203fc")
                                    // 검정
                                    TextColorButton(paintcolor = 0xFF000000,"#000000")
                                    // 추가 색상
                                    Button(onClick = { toggleCustomColorDialog.value = true },
                                        shape = RoundedCornerShape(5.dp),
                                        modifier = Modifier.size(20.dp))
                                    {}
                                }

                                DashedDivider(thickness = 1.dp, modifier = Modifier.padding(top = 10.dp))

                                Text(text = "텍스트 배경 색상")
                                Row(Modifier.padding(top = 10.dp)){
                                    //# == 0xFF
                                    // 빨강
                                    TextBGColorButton(paintcolor = 0xFFFF0000,"#FF0000")
                                    // 주황
                                    TextBGColorButton(paintcolor = 0xFFfc9803,"#fc9803")
                                    // 노랑
                                    TextBGColorButton(paintcolor = 0xFFfcf403,"#fcf403")
                                    // 연두
                                    TextBGColorButton(paintcolor = 0xFFa1fc03,"#a1fc03")
                                    // 초록
                                    TextBGColorButton(paintcolor = 0xFF03fc1c,"#03fc1c")
                                    // 파랑
                                    TextBGColorButton(paintcolor = 0xFF0000FF,"#0000FF")
                                    // 보라
                                    TextBGColorButton(paintcolor = 0xFF6203fc,"#6203fc")
                                    // 검정
                                    TextBGColorButton(paintcolor = 0xFF000000,"#000000")
                                    // 투명 색 임의로 넣었음
                                    TextBGColorButton(paintcolor = 0xFFFc001c , xmlcolor = "#00000000")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DashedDivider(
        thickness: Dp,
        color: Color = Color.LightGray,
        phase: Float = 10f,
        intervals: FloatArray = floatArrayOf(10f, 10f),
        modifier: Modifier
    ) {
        Canvas(
            modifier = modifier
        ) {
            val dividerHeight = thickness.toPx()
            drawRoundRect(
                color = color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = dividerHeight,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = intervals,
                        phase = phase
                    )
                )
            )
        }
    }

    @Composable
    fun DrawColorPopup(offsetX: Float,offsetY: Float,togglePopup: (Boolean) -> Unit){

        val toggleCustomColorDialog = remember{ mutableStateOf(false)}

        if(toggleCustomColorDialog.value) {
            ColorPickerDialog(setShowDialog = {toggleCustomColorDialog.value = it})
        }

        Popup(
            alignment = TopStart,
            offset = IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
            onDismissRequest = { togglePopup(false) },
            properties = PopupProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true,
                focusable = true)
        ) {
            Surface(
                modifier = Modifier
                    .width(intrinsicSize = IntrinsicSize.Max)
                    .height(IntrinsicSize.Max),
                shape = RoundedCornerShape(16.dp),
                elevation = 5.dp
            ) {
                Box{
                    Column(Modifier.padding(10.dp)){
                        Row(Modifier.padding(10.dp)) {
                           Text(text = "펜 설정")
                        }
                        Box(
                            modifier = Modifier
                                .width(IntrinsicSize.Max)
                                .padding(1.dp)
                        ){
                            Column{
                                Row{}
                                // 크기 조정 슬라이더
                                /*Row{
                                    var sliderPosition by remember {
                                        mutableStateOf(paintBrush.strokeWidth)
                                    }
                                    Slider(value = sliderPosition, onValueChange = { sliderPosition = it }, Modifier.width(180.dp), valueRange = 1f..30f, steps = 1)
                                }*/

                                Row{
                                    //# == 0xFF
                                    // 빨강
                                    ColorButton(paintcolor = 0xFFFF0000,"#FF0000")
                                    // 주황
                                    ColorButton(paintcolor = 0xFFfc9803,"#fc9803")
                                    // 노랑
                                    ColorButton(paintcolor = 0xFFfcf403,"#fcf403")
                                    // 연두
                                    ColorButton(paintcolor = 0xFFa1fc03,"#a1fc03")
                                    // 초록
                                    ColorButton(paintcolor = 0xFF03fc1c,"#03fc1c")
                                    // 파랑
                                    ColorButton(paintcolor = 0xFF0000FF,"#0000FF")
                                    // 보라
                                    ColorButton(paintcolor = 0xFF6203fc,"#6203fc")
                                    // 검정
                                    ColorButton(paintcolor = 0xFF000000,"#000000")
                                    // 추가 색상
                                    Button(onClick = { toggleCustomColorDialog.value = true },
                                        shape = RoundedCornerShape(5.dp),
                                        modifier = Modifier.size(20.dp))
                                    {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ColorPickerDialog(setShowDialog:(Boolean) -> Unit){
        val controller = rememberColorPickerController()

        Dialog(onDismissRequest = { setShowDialog(false) }) {
            Surface(
                modifier = Modifier,
                shape = RoundedCornerShape(16.dp),
                elevation = 5.dp){
                Box{
                    Column(Modifier.padding(20.dp)) {
                        // hex
                        Text(text = "사용자 지정 색")
                        HsvColorPicker(modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp),
                            controller = controller, onColorChanged = { colorEnvelope: ColorEnvelope ->
                                var hexcode:String = colorEnvelope.hexCode
                                val s = "#"
                                hexcode = hexcode.substring(2)
                                hexcode = "$s$hexcode"
                                //hexcode = hexcode.substring(2)
                                Log.v("d",hexcode)
                                paintBrush.color = android.graphics.Color.parseColor(hexcode)
                            })
                        BrightnessSlider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .height(35.dp),
                            controller = controller,
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ColorButton(paintcolor: Long , xmlcolor: String){
        val ColorValue = android.graphics.Color.parseColor(xmlcolor)
        Button(
            onClick = { paintBrush.color = ColorValue },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(paintcolor)),
            modifier = Modifier.size(20.dp)) { }
    }

    @Composable
    fun TextColorButton(paintcolor: Long , xmlcolor: String){
        val ColorValue = android.graphics.Color.parseColor(xmlcolor)
        val start = focusedEditText!!.selectionStart
        val end = focusedEditText!!.selectionEnd
        val builder = SpannableStringBuilder(focusedEditText!!.text)

        Button(
            onClick = {
                builder.setSpan( ForegroundColorSpan(ColorValue), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                focusedEditText!!.text = builder
                focusedEditText!!.setSelection(start, end) },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(paintcolor)),
            modifier = Modifier.size(20.dp)) { }
    }

    @Composable
    fun TextBGColorButton(paintcolor: Long , xmlcolor: String){
        val ColorValue = android.graphics.Color.parseColor(xmlcolor)
        val start = focusedEditText!!.selectionStart
        val end = focusedEditText!!.selectionEnd
        val builder = SpannableStringBuilder(focusedEditText!!.text)

        Button(
            onClick = {
                builder.setSpan( BackgroundColorSpan(ColorValue), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                focusedEditText!!.text = builder },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(paintcolor)),
            modifier = Modifier.size(20.dp)) { }
    }

    override fun onStart() {
        super.onStart()
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
        if(mode!=7){
            scrollView.isScrollable=false}
        wrapAreaBox.clearBox()
        focusedImg?.clearBox()
        focusedImg=null
    }

    private fun currentColor(color:Int){
        currentBrush=color      // set currentBrush to color
    }

    private fun openGallery(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            val readPermission=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES)
            if(readPermission==PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES),1)
            }
            else{
                val intent =Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
                imageResult.launch(intent)
            }
        }
        else{
            val readPermission=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
            if(readPermission==PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }
            else{
                val intent =Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
                imageResult.launch(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.draw_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->
                finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun getPathDateClass(){
        for(stroke in pathList){
            var x=ArrayList<Float>()
            var y=ArrayList<Float>()
            for(pos in  stroke.point){
                x.add(pos.first)
                y.add(pos.second)
            }
            NoteData.width.add(stroke.brush.strokeWidth)
            NoteData.pColor.add(stroke.brush.color)
            NoteData.pPos_X.add(x)
            NoteData.pPos_Y.add(y)
            NoteData.pId.add(stroke.id)
        }
        //Log.v("path","path : "+ PathData!![0].pos_X[0].toString())
    }

    fun getImgDateClass(){
        for(img in imgList){
            NoteData.uri.add(img.imgURI.toString())
            NoteData.iPos_X.add(img.pos.first)
            NoteData.iPos_Y.add(img.pos.second)
            NoteData.isXFlip.add(img.isXFlip)
            NoteData.isYFlip.add(img.isYFlip)
            NoteData.imageDegree.add(img.degree)
            NoteData.iXSize.add(img.imgWidth)
            NoteData.iYSize.add(img.imgHeight)
            NoteData.iId.add(img.id)
        }
    }
    fun getTextDateClass(){
        for(text in textList){
            NoteData.span.add(HtmlCompat.toHtml(text.text!!.toSpanned(), HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL))
            NoteData.gravity.add(text.gravity)
        }
    }

    fun clearCompanion(){
        paintBrush = Paint().apply {
            isAntiAlias=true // set paintBrush
            color= currentBrush
            style=Paint.Style.STROKE
            strokeJoin=Paint.Join.ROUND
            strokeWidth=10f
            strokeCap=Paint.Cap.ROUND
        }
        mode=1
        pages.clear()
        textList.clear()
        pathList.clear()
        imgList.clear()
        unStroke.clear()
        reStroke.clear()
        unPage.clear()
        rePage.clear()
        unImg.clear()
        reImg.clear()
        unText.clear()
        reText.clear()
        shapeMode=1
        backgroundMode=1
        currentBrush= android.graphics.Color.BLACK
        wrapAreaBox=SelectedBox(0f,0f,0f,0f)
        focusedImg=null
        isMagnetMode=false
        posX=-100f
        posY=-100f
    }

    fun addPathData(){
        for (i in 0 until note.pId.size) {
            val pos:ArrayList<Pair<Float,Float>> = ArrayList()
            for(j in 0 until note.pPos_X[i].size){
                pos.add(Pair(note.pPos_X[i][j], note.pPos_Y[i][j]))
            }
            pathList.add(
                Stroke(
                    pos,
                    note.pId[i],
                    note.width[i],
                    note.pColor[i]))
        }
    }

    fun addImgData(){
        for (i in 0 until note.iId.size) {
            imgList.add(
                Image(
                    Uri.parse(note.uri[i]),
                    this,
                    false,
                    contentResolver,
                    Pair(note.iPos_X[i],note.iPos_Y[i]),
                    note.iId[i],
                    note.imageDegree[i],
                    note.isXFlip[i],
                    note.isYFlip[i],
                    note.iXSize[i],
                    note.iYSize[i],
                    true
                )
            )
        }
    }

    fun addTextData(){

    }
}




