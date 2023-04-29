package com.example.myapplication

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    var colors = arrayOf("Black", "Red", "Yellow", "Green", "Cyan", "Blue", "Magenta")                  // 색상 선택 리스트
    var bg = arrayOf("None", "Black", "Red", "Yellow", "Green", "Cyan", "Blue", "Magenta")              // 색상 선택 리스트
    var fonts = arrayOf("조선신명조", "조선가는고딕", "조선굴림체", "조선궁서체", "조선일보명조")                 // 폰트 선택 리스트
    var sizes = arrayOf("20pt", "24pt", "28pt", "32pt", "36pt", "40pt")                                 // 글자 크기 선택 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "텍스트 디자인 테스트"

        // 버튼, 스피너, 에디트 텍스트 생성
        val alignLeft = findViewById<Button>(R.id.alignLeft)
        val alignCenter = findViewById<Button>(R.id.alignCenter)
        val alignRight = findViewById<Button>(R.id.alignRight)

        val boldBtn = findViewById<Button>(R.id.boldBtn)
        val italicBtn = findViewById<Button>(R.id.italicBtn)
        val underlineBtn = findViewById<Button>(R.id.underlineBtn)
        val strikelineBtn = findViewById<Button>(R.id.lineBtn)

        val colorSpin = findViewById<Spinner>(R.id.colorSpin)
        val fontSpin = findViewById<Spinner>(R.id.fontSpin)
        val sizeSpin = findViewById<Spinner>(R.id.sizeSpin)
        val bgSpin = findViewById<Spinner>(R.id.bgSpin)

        val memo = findViewById<EditText>(R.id.memo)



        boldBtn.setOnClickListener(View.OnClickListener {
            val start = memo.selectionStart
            val end = memo.selectionEnd

            if (start == end)                                                                           // 드래그된 텍스트가 없으면 함수 종료
                return@OnClickListener

            val spannable: Spannable = memo.text
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
        italicBtn.setOnClickListener(View.OnClickListener {
                val start = memo.selectionStart
                val end = memo.selectionEnd

                if (start == end)                                                                       // 드래그된 텍스트가 없으면 함수 종료
                    return@OnClickListener

                val spannable: Spannable = memo.text
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
        underlineBtn.setOnClickListener(View.OnClickListener {
            val start = memo.selectionStart
            val end = memo.selectionEnd

            if (start == end)                                                                           // 드래그된 텍스트가 없으면 함수 종료
                return@OnClickListener

            val spannable: Spannable = memo.text
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
        strikelineBtn.setOnClickListener(View.OnClickListener {
                val start = memo.selectionStart
                val end = memo.selectionEnd

                if (start == end)                                                                       // 드래그된 텍스트가 없으면 함수 종료
                    return@OnClickListener

                val spannable: Spannable = memo.text
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



        alignLeft.setOnClickListener { memo.gravity = Gravity.LEFT }
        alignCenter.setOnClickListener { memo.gravity = Gravity.CENTER_HORIZONTAL }
        alignRight.setOnClickListener { memo.gravity = Gravity.RIGHT }



        val colorAdapt = ArrayAdapter(                                                                  // 컬러 선택 스피너와 리스트를 연결 할 어댑터 생성
            this, android.R.layout.simple_spinner_dropdown_item, colors )
        colorAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)               // 어댑터에 심플 스피너 아이템 레이아웃 적용
        colorSpin.adapter = colorAdapt                                                                  // 스피너에 어댑터 연결
        colorSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {                // 스피너에서 리스트 선택 시 동작 설정
            override fun onItemSelected( parent: AdapterView<*>?, view: View, position: Int, id: Long ) {
                val start = memo.selectionStart
                val end = memo.selectionEnd
                val builder = SpannableStringBuilder(memo.text)

                when (colors[position]) {
                    "Black" -> builder.setSpan( ForegroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Red" -> builder.setSpan( ForegroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Yellow" -> builder.setSpan( ForegroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Green" -> builder.setSpan( ForegroundColorSpan(Color.GREEN), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Cyan" -> builder.setSpan( ForegroundColorSpan(Color.CYAN), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Blue" -> builder.setSpan( ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                    "Magenta" -> builder.setSpan( ForegroundColorSpan(Color.MAGENTA), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                }
                memo.text = builder
                memo.setSelection(start, end)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {                                   // 아무것도 선택되지 않았을 때
                                                                                                        // 따로 동작하는 건 없지만 onNothingSelected가 있어야 오류가 발생하지 않음
            }
        }



        val fontAdapt = ArrayAdapter(                                                                   // 폰트 선택 스피너와 리스트를 연결 할 어댑터 생성
            this, android.R.layout.simple_spinner_dropdown_item, fonts )
        fontAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)                // 어댑터에 심플 스피너 아이템 레이아웃 적용
        fontSpin.adapter = fontAdapt                                                                    // 스피너에 어댑터 연결
        fontSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {                 // 스피너에서 리스트 선택 시 동작 설정
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onItemSelected( parent: AdapterView<*>?, view: View, position: Int, id: Long ) {
                val start = memo.selectionStart
                val end = memo.selectionEnd
                val builder = SpannableStringBuilder(memo.text)
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
                memo.text = builder
                memo.setSelection(start, end)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {                                   // 아무것도 선택되지 않았을 때
                                                                                                        // 따로 동작하는 건 없지만 onNothingSelected가 있어야 오류가 발생하지 않음
            }
        }



        val sizeAdapt = ArrayAdapter(                                                                   // 글자 크기 선택 스피너와 리스트를 연결 할 어댑터 생성
            this, android.R.layout.simple_spinner_dropdown_item, sizes )
        sizeAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)                // 어댑터에 심플 스피너 아이템 레이아웃 적용
        sizeSpin.adapter = sizeAdapt                                                                    // 스피너에 어댑터 연결
        sizeSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {                 // 스피너에서 리스트 선택 시 동작 설정
            override fun onItemSelected( parent: AdapterView<*>?, view: View, position: Int, id: Long ) {
                val start = memo.selectionStart
                val end = memo.selectionEnd
                val builder = SpannableStringBuilder(memo.text)
                when (sizes[position]) {
                    "20pt" -> builder.setSpan( AbsoluteSizeSpan(48), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "24pt" -> builder.setSpan( AbsoluteSizeSpan(58), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "28pt" -> builder.setSpan( AbsoluteSizeSpan(67), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "32pt" -> builder.setSpan( AbsoluteSizeSpan(77), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "36pt" -> builder.setSpan( AbsoluteSizeSpan(86), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                    "40pt" -> builder.setSpan( AbsoluteSizeSpan(96), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE )
                }
                memo.text = builder
                memo.setSelection(start, end)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }                                 // 아무것도 선택되지 않았을 때
        }



        val bgAdapt = ArrayAdapter(                                                                     // 컬러 선택 스피너와 리스트를 연결 할 어댑터 생성
            this, android.R.layout.simple_spinner_dropdown_item, bg )
        bgAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)                  // 어댑터에 심플 스피너 아이템 레이아웃 적용
        bgSpin.adapter = bgAdapt                                                                        // 스피너에 어댑터 연결
        bgSpin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {                   // 스피너에서 리스트 선택 시 동작 설정
            override fun onItemSelected( parent: AdapterView<*>?, view: View, position: Int, id: Long ) {
                val start = memo.selectionStart
                val end = memo.selectionEnd
                val builder = SpannableStringBuilder(memo.text)
                when (bg[position]) {
                    "None" -> {
                        builder.setSpan( BackgroundColorSpan(Color.parseColor("#00000000")), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        memo.text = builder }
                    "Black" -> {
                        builder.setSpan( BackgroundColorSpan(Color.BLACK), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        memo.text = builder }
                    "Red" -> {
                        builder.setSpan( BackgroundColorSpan(Color.RED), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        memo.text = builder }
                    "Yellow" -> {
                        builder.setSpan( BackgroundColorSpan(Color.YELLOW), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        memo.text = builder }
                    "Green" -> {
                        builder.setSpan( BackgroundColorSpan(Color.GREEN), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        memo.text = builder }
                    "Cyan" -> {
                        builder.setSpan( BackgroundColorSpan(Color.CYAN), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        memo.text = builder }
                    "Blue" -> {
                        builder.setSpan( BackgroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        memo.text = builder }
                    "Magenta" -> {
                        builder.setSpan( BackgroundColorSpan(Color.MAGENTA), start, end, Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
                        memo.text = builder }
                }
                memo.setSelection(start, end)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }                                 // 아무것도 선택되지 않았을 때
        }

    }
}