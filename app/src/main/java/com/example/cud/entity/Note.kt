package com.example.cud.entity

import android.os.Parcelable
import android.text.Editable

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "notes")
@Parcelize
data class Note (
    @PrimaryKey(autoGenerate = true)@ColumnInfo(name = "id") var id: Int = 0,
    @ColumnInfo(name = "title") var title: String = "",
    @ColumnInfo(name = "date") var date: String = "",
    @ColumnInfo(name = "time") var time: String = "",
    @ColumnInfo(name = "width") var width: ArrayList<Float> = ArrayList(),
    @ColumnInfo(name = "color") var pColor: ArrayList<Int>,
    @ColumnInfo(name = "PathPos_X") var pPos_X: ArrayList<ArrayList<Float>> = ArrayList(ArrayList()),
    @ColumnInfo(name = "PathPos_Y") var pPos_Y : ArrayList<ArrayList<Float>> = ArrayList(ArrayList()),
    @ColumnInfo(name = "Path_id") var pId : ArrayList<Int> = ArrayList(),
    @ColumnInfo(name = "Uri") var uri : ArrayList<String> = ArrayList(),
    @ColumnInfo(name = "ImgPos_X") var iPos_X: ArrayList<Float> = ArrayList(),
    @ColumnInfo(name = "ImgPos_Y") var iPos_Y: ArrayList<Float> = ArrayList(),
    @ColumnInfo(name = "isXFlip") var isXFlip: ArrayList<Boolean>,
    @ColumnInfo(name = "isYFlip") var isYFlip: ArrayList<Boolean>,
    @ColumnInfo(name = "imageDegree") var imageDegree:ArrayList<Float>,
    @ColumnInfo(name = "imageXSize") var iXSize:ArrayList<Int>,
    @ColumnInfo(name = "imageYSize") var iYSize:ArrayList<Int>,
    @ColumnInfo(name = "Img_id") var iId: ArrayList<Int>,
    @ColumnInfo(name = "span") var span: ArrayList<String>,
    @ColumnInfo(name = "gravity") var gravity: ArrayList<Int>,
    @ColumnInfo(name = "pages") var pageSize: Int ?= 0
):Parcelable