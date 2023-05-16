package com.example.timetabletest

import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val plusButton = findViewById<Button>(R.id.plus_btn)
        plusButton.setOnClickListener{

            val inflater = LayoutInflater.from(this)
            val popupLayout = inflater.inflate(R.layout.popup_layout, null) // popup_layout 연결
            val lecture = popupLayout.findViewById<EditText>(R.id.lecture_name) //강의명
            val classroom = popupLayout.findViewById<EditText>(R.id.classroom_name) //강의실
            val professor = popupLayout.findViewById<EditText>(R.id.professor_name) //교수명
            val popupOkBtn = popupLayout.findViewById<Button>(R.id.popupOk_btn) //팝업 확인 버튼
            val backgroundSelect = popupLayout.findViewById<Button>(R.id.background_select) // 배경색상 선택 버튼
            val daySelect = popupLayout. findViewById<Button>(R.id.day_select) // 요일 선택 버튼
            val startTimeSelect = popupLayout.findViewById<Button>(R.id.start_time_select) // 시작 시간 선택 버튼
            val endTimeSelect = popupLayout.findViewById<Button>(R.id.end_time_select) // 종료 시간 선택 버튼
            var startTime = 0 //강의 시작 시간
            var endTime  = 0 // 강의 종료 시간

            val popupLayoutDialog = AlertDialog.Builder(this) // popup_layout 다이얼로그 생성
                .setView(popupLayout)
                .create()

            daySelect.setOnClickListener {
                val popupMenu = PopupMenu(this, daySelect)
                popupMenu.menuInflater.inflate(R.menu.day_menu, popupMenu.menu)
                popupMenu.show()
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.daySelect_mon -> daySelect.text = "월요일 ▼"
                        R.id.daySelect_tue -> daySelect.text = "화요일 ▼"
                        R.id.daySelect_wed -> daySelect.text = "수요일 ▼"
                        R.id.daySelect_thu -> daySelect.text = "목요일 ▼"
                        R.id.daySelect_fri -> daySelect.text = "금요일 ▼"
                    }
                    true
                }
            }

            startTimeSelect.setOnClickListener{
                    val hour = 9
                    val minute = 0
                    val startTimePickerDialog = TimePickerDialog(
                        this,
                        {_, hourOfDay1, minute1 ->
                            // 시간 선택 시 동작
                            //val time = String.format("%02d:%02d", hourOfDay1, minute1)
                            //Toast.makeText(this@MainActivity, time, Toast.LENGTH_SHORT).show()
                            startTimeSelect.text = String.format("%02d:%02d ▼", hourOfDay1, minute1)
                            startTime = (hourOfDay1 * 60 + minute1) / 60 // 강의 시작 시간
                        },
                        hour,
                        minute,
                        true
                    )
                    startTimePickerDialog.show()
                }

                endTimeSelect.setOnClickListener{
                    val hour = if(startTime + 1 <= 23) startTime + 1 else 23
                    val minute = 0
                    val endTimePickerDialog = TimePickerDialog(
                        this,
                        {_, hourOfDay2, minute2 ->
                            // 시간 선택 시 동작
                            //val time = String.format("%02d:%02d", hourOfDay2, minute2)
                            //Toast.makeText(this@MainActivity, time, Toast.LENGTH_SHORT).show()
                            endTimeSelect.text = String.format("%02d:%02d ▼", hourOfDay2, minute2)
                            endTime = (hourOfDay2 * 60 + minute2) / 60 // 강의 종료 시간
                        },
                        hour,
                        minute,
                        true
                    )
                    endTimePickerDialog.show()
                }

            backgroundSelect.setOnClickListener{
                val popupMenu = PopupMenu(this, backgroundSelect)
                popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)
                popupMenu.setOnMenuItemClickListener {
                    when (it.itemId){
                        R.id.background_red -> backgroundSelect.text = "빨간색 ▼"
                        R.id.background_green -> backgroundSelect.text = "연두색 ▼"
                        R.id.background_yellow -> backgroundSelect.text = "노란색 ▼"
                        R.id.background_gray-> backgroundSelect.text = "회색 ▼"
                        R.id.background_blue -> backgroundSelect.text = "파란색 ▼"
                        R.id.background_default -> backgroundSelect.text = "기본 색상 ▼"
                    }
                    true
                }
                popupMenu.show()
            }

            popupOkBtn.setOnClickListener{
                if(lecture.text.isNullOrEmpty()){
                    Toast.makeText(this@MainActivity, "강의명을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                else if(startTime>=endTime || startTime < 9 || startTime > 18 || endTime > 19){
                    Toast.makeText(this@MainActivity, "시간 범위를 확인하세요.", Toast.LENGTH_SHORT).show()
                }
                else{
                    selectOK(lecture.text.toString(), classroom.text.toString(), professor.text.toString(),
                        daySelect.text.toString(), startTime, endTime, backgroundSelect.text.toString())
                    popupLayoutDialog.dismiss()
                }
            }
            popupLayoutDialog.show()
        }

        //수정 & 삭제
        val dayList = mutableListOf(
            monday1, monday2, monday3, monday4, monday5, monday6, monday7, monday8, monday9, monday10,
            tuesday1, tuesday2, tuesday3, tuesday4, tuesday5, tuesday6, tuesday7, tuesday8, tuesday9, tuesday10,
            wednesday1, wednesday2, wednesday3, wednesday4, wednesday5, wednesday6, wednesday7, wednesday8, wednesday9, wednesday10,
            thursday1, thursday2, thursday3, thursday4, thursday5, thursday6, thursday7, thursday8, thursday9, thursday10,
            friday1, friday2, friday3, friday4, friday5, friday6, friday7, friday8, friday9, friday10
        )

        val clickHandler = View.OnClickListener { view -> // 클릭 이벤트
            val clickedTextView = view as TextView
            val clickedTextViewBackgroundColor = clickedTextView.background
            val startIndex = dayList.indexOf(clickedTextView)

            if(clickedTextView.text.isNotEmpty()){

                val inflater = LayoutInflater.from(this)
                val clickLayout = inflater.inflate(R.layout.click_layout, null) // click_layout 연결
                val popupDelete = clickLayout.findViewById<Button>(R.id.popupDelete_btn) // 삭제 버튼
                val popupChange = clickLayout.findViewById<Button>(R.id.popupChange_btn) // 수정 버튼

                val clickLecture = clickLayout.findViewById<TextView>(R.id.click_lecture)
                val editLecture = clickLayout.findViewById<EditText>(R.id.click_editLecture)

                val clickClassroom = clickLayout.findViewById<TextView>(R.id.click_classroom)
                val editClassroom = clickLayout.findViewById<EditText>(R.id.click_editClassroom)

                val clickProfessor = clickLayout.findViewById<TextView>(R.id.click_professor)
                val editProfessor = clickLayout.findViewById<EditText>(R.id.click_editProfessor)

                //val clickTime = clickLayout.findViewById<TextView>(R.id.click_time)

                val changeCancel = clickLayout.findViewById<Button>(R.id.change_cancel) // 수정 취소
                val changeOk = clickLayout.findViewById<Button>(R.id.change_ok) // 수정 확인

                clickLecture.text = clickedTextView.text.split("\n")[0]
                clickClassroom.text = clickedTextView.text.split("\n")[1]
                clickProfessor.text = clickedTextView.text.split("\n")[2]
                //clickTime.text = clickedTextView.text.split("\n")[3]

                val clickLayoutDialog = AlertDialog.Builder(this) // click_layout 다이얼로그 생성
                    .setView(clickLayout)
                    .create()

                popupChange.setOnClickListener{
                    editLecture.text = Editable.Factory.getInstance().newEditable(clickLecture.text)
                    clickLecture.visibility = GONE
                    editLecture.visibility = View.VISIBLE
                    editLecture.requestFocus()

                    editClassroom.text = Editable.Factory.getInstance().newEditable(clickClassroom.text)
                    clickClassroom.visibility = GONE
                    editClassroom.visibility = View.VISIBLE
                    editClassroom.requestFocus()

                    editProfessor.text = Editable.Factory.getInstance().newEditable(clickProfessor.text)
                    clickProfessor.visibility = GONE
                    editProfessor.visibility = View.VISIBLE
                    editProfessor.requestFocus()

                    popupDelete.visibility = GONE
                    popupChange.visibility = GONE
                    changeCancel.visibility = View.VISIBLE
                    changeOk.visibility = View.VISIBLE

                    changeOk.setOnClickListener { // 수정 확인
                        val lectureText = editLecture.text.toString()
                        val classroomText = editClassroom.text.toString()
                        val professorText = editProfessor.text.toString()
//                        val timeText = clickTime.text.toString()
                        val clickedTextViewBackgroundColorInt = (clickedTextViewBackgroundColor as ColorDrawable).color

                        clickedTextView.apply {
                            text = SpannableStringBuilder().apply {
                                append(lectureText)
                                append("\n")
                                append(classroomText)
                                append("\n")
                                append(professorText)
                                setSpan(ForegroundColorSpan(clickedTextViewBackgroundColorInt),
                                    length - professorText.length, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                                append("\n")
//                                append(timeText)
//                                setSpan(ForegroundColorSpan(clickedTextViewBackgroundColorInt),
//                                    length - timeText.length, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            }
                        }
                        clickLayoutDialog.dismiss()
                    }

                    changeCancel.setOnClickListener{ // 수정 취소
                        clickLayoutDialog.dismiss()
                    }
                }

                popupDelete.setOnClickListener {
                    val popupDeleteLayout = inflater.inflate(R.layout.popup_delete_layout, null)
                    val deleteOk = popupDeleteLayout.findViewById<Button>(R.id.delete_ok)
                    val deleteCancel = popupDeleteLayout.findViewById<Button>(R.id.delete_cancel)

                    val popupDeleteDialog = AlertDialog.Builder(this) // 정말 삭제하시겠습니까? 창 생성
                        .setView(popupDeleteLayout)
                        .create()

                    //수정해야함
                    deleteOk.setOnClickListener{
                        val clickedTextViewBackgroundColorInt = (clickedTextViewBackgroundColor as ColorDrawable).color // 배경색상을 Int 형으로 변환
                        for (i in startIndex until dayList.size) {
                            val textView = dayList[i]
                            if (textView.background is ColorDrawable && (textView.background as ColorDrawable).color == clickedTextViewBackgroundColorInt) {
                                dayList[i].setBackgroundResource(R.drawable.cell_shape)
                                dayList[startIndex].text = ""
                                Toast.makeText(this, "삭제 되었습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        clickLayoutDialog.dismiss()
                        popupDeleteDialog.dismiss()
                    }
                    deleteCancel.setOnClickListener{
                        popupDeleteDialog.dismiss()
                        clickLayoutDialog.dismiss()
                    }
                    popupDeleteDialog.show()
                }
                clickLayoutDialog.show()
            }
        }
        // dayList 에 있는 모든 TextView 에 대해 클릭 이벤트 핸들러 설정
        dayList.forEach{ textView -> textView.setOnClickListener(clickHandler)}
    }

    //강의 추가 함수
    private fun selectOK(lecture:String, classroom:String, professor:String,
                         daySelect:String, startTime:Int, endTime:Int, backgroundSelect:String){

        var backgroundColor = ""

        if (backgroundSelect == "빨간색 ▼") {
            backgroundColor = "#ed5353"
        }
        if (backgroundSelect == "파란색 ▼") {
            backgroundColor = "#4b94e3"
        }
        if (backgroundSelect == "연두색 ▼") {
            backgroundColor = "#82ed98"
        }
        if (backgroundSelect == "노란색 ▼") {
            backgroundColor = "#ebe35b"
        }
        if (backgroundSelect == "회색 ▼") {
            backgroundColor = "#d6cece"
        }
        if (backgroundSelect == "배경 색상 선택(기본) ▼" || backgroundSelect == "기본 색상 ▼") {
            backgroundColor = "#FFEBEE"
        }

        if (daySelect == "월요일 ▼") {
            val mondayList = listOf(
                monday1,
                monday2,
                monday3,
                monday4,
                monday5,
                monday6,
                monday7,
                monday8,
                monday9,
                monday10
            )
            for ((index, view) in mondayList.withIndex()) {
                if (index == startTime - 9) {
                    val text = lecture + "\n" + classroom + "\n" + professor + "\n" + startTime + endTime
                    val spannable = SpannableStringBuilder(text)
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(professor), text.indexOf(professor) + professor.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(startTime.toString()), text.indexOf(startTime.toString()) + startTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(endTime.toString()), text.indexOf(endTime.toString()) + endTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    view.text = spannable
                }
                if (startTime - 9 <= index && endTime - 10 >= index) {
                    view.setBackgroundColor(Color.parseColor(backgroundColor))
                }
            }
        }

        if (daySelect == "화요일 ▼") {
            val tuesdayList = listOf(
                tuesday1,
                tuesday2,
                tuesday3,
                tuesday4,
                tuesday5,
                tuesday6,
                tuesday7,
                tuesday8,
                tuesday9,
                tuesday10
            )
            for ((index, view) in tuesdayList.withIndex()) {
                if (index == startTime - 9) {
                    val text = lecture + "\n" + classroom + "\n" + professor + "\n" + startTime + endTime
                    val spannable = SpannableStringBuilder(text)
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(professor), text.indexOf(professor) + professor.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(startTime.toString()), text.indexOf(startTime.toString()) + startTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(endTime.toString()), text.indexOf(endTime.toString()) + endTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    view.text = spannable
                }
                if (startTime - 9 <= index && endTime - 10 >= index) {
                    view.setBackgroundColor(Color.parseColor(backgroundColor))
                }
            }
        }

        if (daySelect == "수요일 ▼") {
            val wednesdayList = listOf(
                wednesday1,
                wednesday2,
                wednesday3,
                wednesday4,
                wednesday5,
                wednesday6,
                wednesday7,
                wednesday8,
                wednesday9,
                wednesday10
            )
            for ((index, view) in wednesdayList.withIndex()) {
                if (index == startTime - 9) {
                    val text = lecture + "\n" + classroom + "\n" + professor + "\n" + startTime + endTime
                    val spannable = SpannableStringBuilder(text)
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(professor), text.indexOf(professor) + professor.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(startTime.toString()), text.indexOf(startTime.toString()) + startTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(endTime.toString()), text.indexOf(endTime.toString()) + endTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    view.text = spannable
                }
                if (startTime - 9 <= index && endTime - 10 >= index) {
                    view.setBackgroundColor(Color.parseColor(backgroundColor))
                }
            }
        }

        if (daySelect == "목요일 ▼") {
            val thursdayList = listOf(
                thursday1,
                thursday2,
                thursday3,
                thursday4,
                thursday5,
                thursday6,
                thursday7,
                thursday8,
                thursday9,
                thursday10
            )
            for ((index, view) in thursdayList.withIndex()) {
                if (index == startTime - 9) {
                    val text = lecture + "\n" + classroom + "\n" + professor + "\n" + startTime + endTime
                    val spannable = SpannableStringBuilder(text)
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(professor), text.indexOf(professor) + professor.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(startTime.toString()), text.indexOf(startTime.toString()) + startTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(endTime.toString()), text.indexOf(endTime.toString()) + endTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    view.text = spannable
                }
                if (startTime - 9 <= index && endTime - 10 >= index) {
                    view.setBackgroundColor(Color.parseColor(backgroundColor))
                }
            }
        }

        if (daySelect == "금요일 ▼") {
            val fridayList = listOf(
                friday1,
                friday2,
                friday3,
                friday4,
                friday5,
                friday6,
                friday7,
                friday8,
                friday9,
                friday10
            )
            for ((index, view) in fridayList.withIndex()) {
                if (index == startTime - 9) {
                    val text = lecture + "\n" + classroom + "\n" + professor + "\n" + startTime + endTime
                    val spannable = SpannableStringBuilder(text)
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(professor), text.indexOf(professor) + professor.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(startTime.toString()), text.indexOf(startTime.toString()) + startTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    spannable.setSpan(
                        ForegroundColorSpan(Color.parseColor(backgroundColor)),
                        text.indexOf(endTime.toString()), text.indexOf(endTime.toString()) + endTime.toString().length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    view.text = spannable
                }
                if (startTime - 9 <= index && endTime - 10 >= index) {
                    view.setBackgroundColor(Color.parseColor(backgroundColor))
                }
            }
        }
    }
}