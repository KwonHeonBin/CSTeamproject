package com.KDB.exam



class SelectedBox:Box {


    var checkedStroke=ArrayList<Stroke>()
    private var scaleOfPoints=ArrayList<ArrayList<Pair<Float,Float>>>()
    constructor(upperLX:Float,upperLY:Float,underRX:Float,underRY:Float){
        setPoint(Pair(upperLX,upperLY),Pair(underRX,upperLY),Pair(upperLX,underRY),Pair(underRX,underRY))
    }

    fun setStrokeScale(){   // 박스내부 선들의 배율 설정
        scaleOfPoints.clear()
        for (i in checkedStroke){
            val strokeBox=ArrayList<Pair<Float,Float>>()
            for (j in i.point){
                val scaleX=(j.first-upperLPoint.first)/(upperRPoint.first-upperLPoint.first)
                val scaleY=(j.second-upperRPoint.second)/(underRPoint.second-upperRPoint.second)
                strokeBox.add(Pair(scaleX,scaleY))
            }
            scaleOfPoints.add(strokeBox)
        }

    }
    fun applyScale(){   // 배율 적용
        for (i in 0 until checkedStroke.size){
            for (j in 0 until checkedStroke[i].point.size){
                checkedStroke[i].point[j]=Pair(((upperRPoint.first-upperLPoint.first)*scaleOfPoints[i][j].first)+upperLPoint.first,
                    ((underRPoint.second-upperRPoint.second)*scaleOfPoints[i][j].second)+upperRPoint.second)
            }
        }
    }

    override fun setBox(){ // 박스 좌표 설정
        setPoint(Pair(checkedStroke.minOf { it.point.minOf { it.first }},checkedStroke.minOf { it.point.minOf { it.second }}),
                 Pair(checkedStroke.maxOf{it.point.maxOf { it.first }},checkedStroke.minOf { it.point.minOf { it.second }}),
                Pair(checkedStroke.minOf { it.point.minOf { it.first }},checkedStroke.maxOf{it.point.maxOf { it.second }}),
                 Pair(checkedStroke.maxOf{it.point.maxOf { it.first }},checkedStroke.maxOf{it.point.maxOf { it.second }}))
        setStrokeScale()
    }

    override fun clearBox(){ // 박스 초기화
        super.clearBox()
        checkedStroke.clear()
        scaleOfPoints.clear()

    }
}