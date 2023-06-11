package com.example.cud.ui.schedule

import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.example.cud.MainActivity
import com.example.cud.R
import com.example.cud.databinding.FragmentScheduleBinding


class ScheduleFragment : Fragment() {

    lateinit var mainAct: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainAct = context as MainActivity
    }

    private var _binding: FragmentScheduleBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dayList: MutableList<TextView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // View 모델에 값 저장

        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initializeViews()
        setClickListeners()

        return root
    }

    private fun initializeViews() {
        dayList = mutableListOf(
            binding.monday1, binding.monday2, binding.monday3, binding.monday4, binding.monday5, binding.monday6, binding.monday7, binding.monday8, binding.monday9, binding.monday10,
            binding.tuesday1, binding.tuesday2, binding.tuesday3, binding.tuesday4, binding.tuesday5, binding.tuesday6, binding.tuesday7, binding.tuesday8, binding.tuesday9, binding.tuesday10,
            binding.wednesday1, binding.wednesday2, binding.wednesday3, binding.wednesday4, binding.wednesday5, binding.wednesday6, binding.wednesday7, binding.wednesday8,binding.wednesday9, binding.wednesday10,
            binding.thursday1, binding.thursday2, binding.thursday3, binding.thursday4, binding.thursday5, binding.thursday6, binding.thursday7, binding.thursday8, binding.thursday9, binding.thursday10,
            binding.friday1, binding.friday2, binding.friday3, binding.friday4, binding.friday5, binding.friday6, binding.friday7, binding.friday8,binding.friday9, binding.friday10
        )
    }

    private fun setClickListeners() {
        val plusButton = binding.plusBtn
        plusButton.setOnClickListener {
            showPopupDialog()
        }

        dayList.forEach { textView ->
            textView.setOnClickListener { view ->
                lectureClickEvent(view)
            }
        }
    }

    private fun showPopupDialog() { // + 버튼 클릭 시 나오는 dialog
        val inflater = LayoutInflater.from(mainAct)
        val popupLayout = inflater.inflate(R.layout.popup_schedule, null) // popup_layout 연결

        val lecture = popupLayout.findViewById<EditText>(R.id.lecture_name) //강의명
        val classroom = popupLayout.findViewById<EditText>(R.id.classroom_name) //강의실
        val professor = popupLayout.findViewById<EditText>(R.id.professor_name) //교수명
        val popupOkBtn = popupLayout.findViewById<Button>(R.id.popupOk_btn) //팝업 확인 버튼
        val backgroundSelect = popupLayout.findViewById<Button>(R.id.background_select) // 배경색상 선택 버튼
        val daySelect = popupLayout.findViewById<Button>(R.id.day_select) // 요일 선택 버튼
        val startTimeSelect = popupLayout.findViewById<Button>(R.id.start_time_select) // 시작 시간 선택 버튼
        val endTimeSelect = popupLayout.findViewById<Button>(R.id.end_time_select) // 종료 시간 선택 버튼

        var startTime = 0 //강의 시작 시간
        var endTime = 0 // 강의 종료 시간

        val popupLayoutDialog = androidx.appcompat.app.AlertDialog.Builder(mainAct) // popup_layout 다이얼로그 생성
            .setView(popupLayout)
            .create()

        daySelect.setOnClickListener {
            showPopupMenu(daySelect, R.menu.day_menu) { menuItem ->
                daySelect.text = when (menuItem.itemId) {
                    R.id.daySelect_mon -> "월요일 ▼"
                    R.id.daySelect_tue -> "화요일 ▼"
                    R.id.daySelect_wed -> "수요일 ▼"
                    R.id.daySelect_thu -> "목요일 ▼"
                    R.id.daySelect_fri -> "금요일 ▼"
                    else -> ""
                }
            }
        }

        startTimeSelect.setOnClickListener {
            showTimePickerDialog(startTimeSelect) { hourOfDay ->
                startTime = hourOfDay
            }
        }

        endTimeSelect.setOnClickListener {
            showTimePickerDialog(endTimeSelect) { hourOfDay->
                endTime = hourOfDay
            }
        }

        backgroundSelect.setOnClickListener {
            showPopupMenu(backgroundSelect, R.menu.backgroundcolor_menu) {
                backgroundSelect.text = when (it.itemId) {
                    R.id.background_red -> "빨간색 ▼"
                    R.id.background_green -> "연두색 ▼"
                    R.id.background_yellow -> "노란색 ▼"
                    R.id.background_gray -> "회색 ▼"
                    R.id.background_blue -> "파란색 ▼"
                    R.id.background_default -> "기본 색상 ▼"
                    else -> ""
                }
            }
        }

        popupOkBtn.setOnClickListener {
            when {
                (lecture.text.isNullOrEmpty()) -> Toast.makeText(mainAct, "강의명을 입력해주세요.", Toast.LENGTH_SHORT).show()

                (startTime >= endTime || startTime < 9 || startTime > 18 || endTime > 19) ->
                    Toast.makeText(mainAct, "시간 범위를 확인하세요.", Toast.LENGTH_SHORT).show()

                (duplicateCheck(daySelect.text.toString(), startTime, endTime)) ->
                    Toast.makeText(mainAct, "중복된 시간입니다.", Toast.LENGTH_SHORT).show()

                else -> {
                    selectOK(
                        lecture.text.toString(), classroom.text.toString(), professor.text.toString(),
                        daySelect.text.toString(), startTime, endTime, backgroundSelect.text.toString()
                    )
                    popupLayoutDialog.dismiss()
                }
            }
        }
        popupLayoutDialog.show()
    }

    // 중복 체크
    private fun duplicateCheck(daySelect: String, startTime: Int, endTime: Int): Boolean {
        val dayIndex = when (daySelect) {
            "월요일 ▼" -> 0
            "화요일 ▼" -> 1
            "수요일 ▼" -> 2
            "목요일 ▼" -> 3
            "금요일 ▼" -> 4
            else -> return false
        }
        for (i in startTime - 9 + dayIndex * 10 until endTime - 9 + dayIndex * 10) {
            if ((dayList[i].background as ColorDrawable).color != -1) {
                return true // 중복이 있는 경우 true 반환
            }
        }
        return false // 중복이 없는 경우 false 반환
    }

    // 팝업 메뉴 생성
    private fun showPopupMenu(view: View, menuResId: Int, itemClickAction: (MenuItem) -> Unit) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(menuResId, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            itemClickAction(it)
            true
        }
        popupMenu.show()
    }

    // 시간 선택 시 동작 부분
    private fun showTimePickerDialog(view: View, timeSetAction: (Int) -> Unit) {
        val defaultHour = 9
        val defaultMinute = 0
        val timePickerDialog = TimePickerDialog(
            view.context,
            { _, hour, minute ->
                if(minute > 0){
                    Toast.makeText(mainAct, "죄송합니다. 분은 0만 선택 할 수 있습니다.", Toast.LENGTH_SHORT).show()
                }
                timeSetAction(hour)
                val timeText = String.format("%02d:%02d ▼", hour, 0)
                (view as Button).text = timeText
            },
            defaultHour,
            defaultMinute,
            true
        )
        timePickerDialog.show()
    }

    // 시간표에 추가된 강의 클릭 이벤트
    private fun lectureClickEvent(view: View) {
        val clickedTextView = view as TextView
        val clickedTextViewBackgroundColor = clickedTextView.background
        val startIndex = dayList.indexOf(clickedTextView)

        if (clickedTextView.text.isNotEmpty()) {
            val inflater = LayoutInflater.from(mainAct)
            val clickLayout = inflater.inflate(R.layout.click_layout, null) // click_layout 연결
            val clickLayoutDialog = androidx.appcompat.app.AlertDialog.Builder(mainAct) // click_layout 다이얼로그 생성
                .setView(clickLayout)
                .create()

            val clickLecture = clickLayout.findViewById<TextView>(R.id.click_lecture) // 클릭한 강의의 강의명
            val editLecture = clickLayout.findViewById<EditText>(R.id.click_editLecture) // 수정할 강의명 입력 부분
            val clickClassroom = clickLayout.findViewById<TextView>(R.id.click_classroom) // 클릭한 강의의 강의실
            val editClassroom = clickLayout.findViewById<EditText>(R.id.click_editClassroom) // 수정할 강의실 입력 부분
            val clickProfessor = clickLayout.findViewById<TextView>(R.id.click_professor) // 클릭한 강의의 교수명
            val editProfessor = clickLayout.findViewById<EditText>(R.id.click_editProfessor) // 수정할 교수명 입력 부분
            val popupDelete = clickLayout.findViewById<Button>(R.id.popupDelete_btn) // 삭제 버튼
            val popupChange = clickLayout.findViewById<Button>(R.id.popupChange_btn) // 수정 버튼

            clickLecture.text = clickedTextView.text.split("\n")[0]
            clickClassroom.text = clickedTextView.text.split("\n")[1]
            clickProfessor.text = clickedTextView.text.split("\n")[2]

            popupChange.setOnClickListener { // 수정 버튼 클릭 이벤트
                editLecture.text = Editable.Factory.getInstance().newEditable(clickLecture.text)
                clickLecture.visibility = View.GONE
                editLecture.visibility = View.VISIBLE
                editLecture.requestFocus()

                editClassroom.text = Editable.Factory.getInstance().newEditable(clickClassroom.text)
                clickClassroom.visibility = View.GONE
                editClassroom.visibility = View.VISIBLE
                editClassroom.requestFocus()

                editProfessor.text = Editable.Factory.getInstance().newEditable(clickProfessor.text)
                clickProfessor.visibility = View.GONE
                editProfessor.visibility = View.VISIBLE
                editProfessor.requestFocus()

                popupDelete.visibility = View.GONE
                popupChange.visibility = View.GONE
                clickLayout.findViewById<View>(R.id.change_cancel).visibility = View.VISIBLE //수정 확인 버튼
                clickLayout.findViewById<View>(R.id.change_ok).visibility = View.VISIBLE // 수정 취소 버튼

                clickLayout.findViewById<Button>(R.id.change_ok).setOnClickListener { // 수정 확인 버튼 클릭 이벤트
                    val clickedTextViewBackgroundColorInt = (clickedTextViewBackgroundColor as ColorDrawable).color

                    clickedTextView.apply {
                        text = SpannableStringBuilder().apply {
                            append(editLecture.text.toString())
                            append("\n")
                            append(editClassroom.text.toString())
                            append("\n")
                            append(editProfessor.text.toString())
                            setSpan(
                                ForegroundColorSpan(clickedTextViewBackgroundColorInt),
                                length - editProfessor.text.toString().length, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                    clickLayoutDialog.dismiss()
                }

                clickLayout.findViewById<Button>(R.id.change_cancel).setOnClickListener { // 수정 취소 버튼 클릭 이벤트
                    clickLayoutDialog.dismiss()
                }
            }

            popupDelete.setOnClickListener { // 삭제 버튼 클릭 이벤트
                val popupDeleteLayout = inflater.inflate(R.layout.popup_delete_layout, null) // 정말 삭제하시겠습니까? 레이아웃 연결
                val popupDeleteDialog = androidx.appcompat.app.AlertDialog.Builder(mainAct) // 정말 삭제하시겠습니까? 창 생성
                    .setView(popupDeleteLayout)
                    .create()

                popupDeleteLayout.findViewById<Button>(R.id.delete_ok).setOnClickListener {
                    val clickedTextViewBackgroundColorInt = (clickedTextViewBackgroundColor as ColorDrawable).color // 배경색상을 Int 형으로 변환

                    for (i in startIndex until dayList.size) {
                        val textView = dayList[i]
                        if (textView.background is ColorDrawable && (textView.background as ColorDrawable).color == clickedTextViewBackgroundColorInt) {
                            textView.setBackgroundColor(Color.WHITE) //클릭한 텍스트뷰의 배경색상과 다음 인덱스의 텍스트뷰 배경색상이 같다면 삭제
                            dayList[startIndex].text = ""
                            Toast.makeText(mainAct, "삭제 되었습니다.", Toast.LENGTH_SHORT).show()
                        }

                        if (i + 1 < dayList.size && dayList[i + 1].background is ColorDrawable &&
                            (dayList[i + 1].background as ColorDrawable).color == clickedTextViewBackgroundColorInt &&
                            dayList[i + 1].text.isNotEmpty()) {
                            // 다음 인덱스가 같은 색상이고 값이 있는 경우 반복문을 즉시 종료
                            break
                        }
                    }
                    clickLayoutDialog.dismiss()
                    popupDeleteDialog.dismiss()
                }

                popupDeleteLayout.findViewById<Button>(R.id.delete_cancel).setOnClickListener {
                    popupDeleteDialog.dismiss()
                    clickLayoutDialog.dismiss()
                }
                popupDeleteDialog.show()
            }
            clickLayoutDialog.show()
        }
    }

    //강의 추가 함수
    private fun selectOK(
        lecture: String, classroom: String, professor: String,
        daySelect: String, startTime: Int, endTime: Int, backgroundSelect: String
    ) {
        val backgroundColor = when (backgroundSelect) {
            "빨간색 ▼" -> "#ed5353"
            "파란색 ▼" -> "#4b94e3"
            "연두색 ▼" -> "#82ed98"
            "노란색 ▼" -> "#ebe35b"
            "회색 ▼" -> "#d6cece"
            "배경 색상 선택(기본) ▼", "기본 색상 ▼" -> "#FFEBEE"
            else -> ""
        }

        val dayList = when (daySelect) {
            "월요일 ▼" -> listOf(binding.monday1, binding.monday2, binding.monday3, binding.monday4, binding.monday5, binding.monday6, binding.monday7, binding.monday8, binding.monday9, binding.monday10)
            "화요일 ▼" -> listOf(binding.tuesday1, binding.tuesday2, binding.tuesday3, binding.tuesday4, binding.tuesday5, binding.tuesday6, binding.tuesday7, binding.tuesday8, binding.tuesday9, binding.tuesday10)
            "수요일 ▼" -> listOf(binding.wednesday1, binding.wednesday2, binding.wednesday3, binding.wednesday4, binding.wednesday5, binding.wednesday6, binding.wednesday7, binding.wednesday8, binding.wednesday9, binding.wednesday10)
            "목요일 ▼" -> listOf(binding.thursday1, binding.thursday2, binding.thursday3, binding.thursday4, binding.thursday5, binding.thursday6, binding.thursday7, binding.thursday8, binding.thursday9, binding.thursday10)
            "금요일 ▼" -> listOf(binding.friday1, binding.friday2, binding.friday3, binding.friday4, binding.friday5, binding.friday6, binding.friday7, binding.friday8, binding.friday9, binding.friday10)
            else -> emptyList()
        }

        dayList.forEachIndexed { index, view ->
            if (index == startTime - 9) {
                val text = "$lecture\n$classroom\n$professor"
                val spannable = SpannableStringBuilder(text)
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor(backgroundColor)),
                    text.indexOf(professor), text.indexOf(professor) + professor.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                view.text = spannable
            }
            if (startTime - 9 <= index && endTime - 10 >= index) {
                view.setBackgroundColor(Color.parseColor(backgroundColor))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}