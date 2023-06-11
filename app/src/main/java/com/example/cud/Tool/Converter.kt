package com.example.cud.Tool

import android.text.Editable
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

val gson : Gson = GsonBuilder()
    .setPrettyPrinting()
    .excludeFieldsWithoutExposeAnnotation()
    .create()


inline fun <reified T> Gson.fromJson(json: String) =
    fromJson<T>(json, object : TypeToken<T>() {}.type)

@ProvidedTypeConverter
class Converter {

    @TypeConverter
    fun arrListToJson(value: ArrayList<ArrayList<Float>>?): String{
        return gson.toJson(value)
    }

    @TypeConverter
    fun arrtJsonToList(value: String):ArrayList<ArrayList<Float>>{
        val listType = object : TypeToken<ArrayList<ArrayList<Float>>>(){}.type
        return gson.fromJson(value,listType)
    }

    @TypeConverter
    fun StringListToJson(value: ArrayList<String>?): String{
        return gson.toJson(value)
    }

    @TypeConverter
    fun StringJsonToList(value: String):ArrayList<String>{
        val listType = object : TypeToken<ArrayList<String>>(){}.type
        return gson.fromJson(value,listType)
    }

    @TypeConverter
    fun FloatListToJson(value: ArrayList<Float>?): String{
        return gson.toJson(value)
    }

    @TypeConverter
    fun FloatJsonToList(value: String):ArrayList<Float>{
        val listType = object : TypeToken<ArrayList<Float>>(){}.type
        return gson.fromJson(value,listType)
    }

    @TypeConverter
    fun IntListToJson(value: ArrayList<Int>?): String{
        return gson.toJson(value)
    }

    @TypeConverter
    fun IntJsonToList(value: String): ArrayList<Int>{
        val listType = object : TypeToken<ArrayList<Int>>(){}.type
        return gson.fromJson(value,listType)
    }

    @TypeConverter
    fun BooleanListToJson(value: ArrayList<Boolean>?): String{
        return gson.toJson(value)
    }

    @TypeConverter
    fun BooleanJsonToList(value: String):ArrayList<Boolean>{
        val listType = object : TypeToken<ArrayList<Boolean>>(){}.type
        return gson.fromJson(value,listType)
    }




    //@TypeConverter
    //fun pageListToJson(value: ArrayList<canvasView>?) = Gson().toJson(value)
    //@TypeConverter
    //fun pageJsonToList(value: String):ArrayList<canvasView>{
    //    return try{ //       Gson().fromJson<ArrayList<canvasView>>(value)
    //    } catch (e:java.lang.Exception){
    //        arrayListOf()
    //    }
    //}

}