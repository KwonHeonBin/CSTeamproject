package com.KDB.exam

class Eraser {
    var radius:Float =5f
    var pos=Pair(-100f,-100f)
    var mode:Int=0      // 0-> stroke  1-> area
    constructor(rad:Float,pos:Pair<Float,Float>,mod:Int=0){
        radius=rad
        this.pos=pos
        mode=mod
    }
    fun setRad(rad: Float){
        radius=rad
    }

}