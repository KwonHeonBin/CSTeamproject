package com.example.cud.dataclass

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Parcelable
import android.text.Editable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoteDataClass(
    var width: ArrayList<Float> = ArrayList(), // width 값만
    var pColor: ArrayList<Int> = ArrayList(),
    var pPos_X: ArrayList<ArrayList<Float>> = ArrayList(ArrayList()), // x
    var pPos_Y: ArrayList<ArrayList<Float>> = ArrayList(ArrayList()), // y
    var pId:ArrayList<Int> = ArrayList(), // page
    var uri:ArrayList<String> = ArrayList(),
    var iPos_X:ArrayList<Float> = ArrayList(),
    var iPos_Y:ArrayList<Float> = ArrayList(),
    var isXFlip:ArrayList<Boolean> = ArrayList(),
    var isYFlip:ArrayList<Boolean> = ArrayList(),
    var imageDegree:ArrayList<Float> = ArrayList(),
    var iXSize:ArrayList<Int> = ArrayList(),
    var iYSize:ArrayList<Int> = ArrayList(),
    var iId:ArrayList<Int> = ArrayList(),
    var span: ArrayList<String> = ArrayList(),
    var gravity: ArrayList<Int> = ArrayList()
) : Parcelable

