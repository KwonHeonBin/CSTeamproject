package com.example.cud.ui.draw

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.math.MathUtils.clamp
import com.example.cud.ui.draw.CanvasManager.Companion.getDst
import com.example.cud.ui.draw.CanvasManager.Companion.posX
import com.example.cud.ui.draw.CanvasManager.Companion.posY
import com.example.cud.ui.draw.DrawCanvas.Companion.height
import com.example.cud.ui.draw.DrawCanvas.Companion.width
import kotlin.math.*

class Image: Box {
    var bitmapImg:Bitmap
    lateinit var oriBitmapImg:Bitmap
    var context:Context
    var imgURI:Uri
    var pos=Pair(0f,0f)
    var isFocused=false
    var contentResolver:ContentResolver
    var matrix=Matrix()
    var matrixDegree:Float=0f
    var imgWidth:Int=0
    var imgHeight:Int=0
    var isXFlip:Boolean =false
    var isYFlip:Boolean =false

    constructor(uri:Uri?, context: Context, focused:Boolean,contentResolver:ContentResolver,pos:Pair<Float,Float>,id:Int,
                degree:Float=0f,isXFlip:Boolean=false,isYFlip:Boolean=false,imgWidth:Int=0,imgHeight: Int=0,isLoad:Boolean=false){
        this.context=context
        isFocused=focused
        imgURI=uri!!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // 이미지 원본 저장
            oriBitmapImg= ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imgURI))
        }
        bitmapImg=oriBitmapImg
        this.pos= pos
        this.id=id
        isImgBox=true
        this.degree=degree
        this.contentResolver=contentResolver
        boxBrush.strokeWidth=6f
        circleBrush.strokeWidth=6f
        circleFillBrush.strokeWidth=6f
        this.isXFlip=isXFlip
        this.isYFlip=isYFlip
        if(isLoad){
            this.imgWidth=imgWidth
            this.imgHeight=imgHeight
            bitmapImg=Bitmap.createScaledBitmap(oriBitmapImg, imgWidth, imgHeight, true)
            setPoint(Pair(pos.first,pos.second),
                Pair(pos.first+imgWidth,pos.second),
                Pair(pos.first,pos.second+imgHeight),
                Pair(pos.first+imgWidth,pos.second+imgHeight))
            if(degree!=0f){
                rotateBox(degree)
                matrixDegree=degree
            }
            setMatrix()
        }
        else{matrix.setTranslate(this.pos.first,this.pos.second)}
    }

    fun setImageSize(x:Int,y:Int){// 이미지 사이즈 설정
        bitmapImg=Bitmap.createScaledBitmap(oriBitmapImg, x, y, true)
        setBox()
        Log.v("width",imgWidth.toString())
    }

    fun setImgWH(){// 이미지 가로 세로 길이 업데이트
        imgWidth=getDst(upperLPoint,upperRPoint).toInt()
        imgHeight=getDst(upperLPoint,underLPoint).toInt()
    }

    fun applyImageSize(){ // 이미지 사이즈 적용
        swapPoints()
        Log.v("apply",imgWidth.toString())
        if(degree==0f){
            pos=Pair(upperLPoint.first,upperLPoint.second)
            setMatrix()
        }
        bitmapImg=Bitmap.createBitmap(oriBitmapImg,0,0,     // apply matrix
            oriBitmapImg.width, oriBitmapImg.height,null,false)
        setImgWH()
        bitmapImg=Bitmap.createScaledBitmap(bitmapImg, imgWidth, imgHeight, true)// apply scale
//        bitmapImg=Bitmap.createBitmap(bitmapImg,0,0,     // drawbitmap에서 이미 매트릭스를 적용했기때문에 불필요
//            bitmapImg.width, bitmapImg.height,matrix, true)
    }

    fun setImageRotate(degree:Float){
        if(degree!=matrixDegree&&abs(degree-matrixDegree)>0.1f){
            matrix.postRotate((degree-matrixDegree),midPoint.first,midPoint.second)
            rotateBox(degree-matrixDegree)
            matrixDegree=degree
        }
        applyImageSize()
    }

    override fun setMidPoint() {
        super.setMidPoint()
        if(clickedPoint!=10){
            rotationPoint=Pair(upperMPoint.first+50*sin(toRad(degree)),
                                upperMPoint.second-50*cos(toRad(degree)))
            deletePoint=Pair(upperRPoint.first-50*cos(toRad(degree-45)),
                            upperRPoint.second-50*sin(toRad(degree-45)))
        }
    }

    override fun setBox(){// 박스를 설정(이미지를 움직일 때 실행)
        if(degree==0f){// 회전하지 않았다면
            pos=Pair(clamp(pos.first,0f,width-bitmapImg.width),clamp(pos.second,0f, height-bitmapImg.height ))// 수정 필요
            setPoint(Pair(pos.first,pos.second),
                Pair(pos.first+bitmapImg.width,pos.second),
                Pair(pos.first,pos.second+bitmapImg.height),
                Pair(pos.first+bitmapImg.width,pos.second+bitmapImg.height))
        }
        else{

            setPos()
            pos=rotatePoint(-degree,upperLPoint,midPoint)
        }// 회전 했다면
        setMatrix()
    }

    private fun setPos(){// 이미지가 화면 밖을 나갈 시 위치를 화면 안으로 조정
        val pointIndex= arrayOf(upperLPoint,upperRPoint,underLPoint,underRPoint)// 자주 사용하지 않는다면 전역변수보단 지역번수가 더 나을듯?
        val minX=pointIndex.minOf { it.first }// 꼭지점들 중 가장 작은 값과 큰 값을 기준으로 조정 범위를 정함
        val maxX=pointIndex.maxOf { it.first }
        val minY=pointIndex.minOf { it.second }
        val maxY=pointIndex.maxOf { it.second }
        val xGap:Float = if(minX<0){ -minX } // 조정 값
                        else if(maxX>width){ width - maxX }
                        else { 0f }
        val yGap:Float=if(minY<0){-minY}
                        else if(maxY> height){ height - maxY }
                        else{0f}
        upperLPoint=Pair(upperLPoint.first+xGap,upperLPoint.second+yGap)// 실제 조정
        upperRPoint=Pair(upperRPoint.first+xGap,upperRPoint.second+yGap)
        underLPoint=Pair(underLPoint.first+xGap,underLPoint.second+yGap)
        underRPoint=Pair(underRPoint.first+xGap,underRPoint.second+yGap)
        setMidPoint()// 중간 점 지정
    }

    fun moveImg(dst:Pair<Float,Float>){// 이미지 이동
        pos= Pair(pos.first+dst.first,pos.second+dst.second)
        setMatrix()
    }

    override fun clearBox() {
        isFocused=false
    }

    fun moveBox(dst: Pair<Float, Float>, pos: Pair<Float, Float>) {
        val wx: Float// 가로의 x성분
        val wy: Float// 가로의 y성분
        val hx: Float// 세로의 x성분
        val hy: Float// 세로의 y성분
        val dx: Float// x 변화량
        val dy: Float// y 변화량
        var gapDegree: Float// 드래그 각도와 이미지 회전 각도의 차이
        when(clickedPoint){
            1->{    // set size of XY upperL
                upperLPoint=Pair(upperLPoint.first+dst.first,upperLPoint.second+dst.second)
                gapDegree=toRad(degree-getDegree(underRPoint,upperLPoint,false,-90f))// 사각형의 대각선과 가로의 각도
                wx= getDst(upperLPoint,underRPoint)*cos(gapDegree)*sin(toRad(degree))// 대각선의 길이에 위에 구한 각도를 내적 후 사각형의 기울기 만큼 내적
                wy= getDst(upperLPoint,underRPoint)*cos(gapDegree)*cos(toRad(degree))
                hx= getDst(upperLPoint,underRPoint)*sin(gapDegree)*cos(toRad(degree))
                hy= getDst(upperLPoint,underRPoint)*sin(gapDegree)*sin(toRad(degree))
                upperRPoint=Pair(underRPoint.first+wx,underRPoint.second-wy)
                underLPoint=Pair(underRPoint.first-hx,underRPoint.second-hy)
            }
            2->{    // set size of XY upperR
                upperRPoint=Pair(upperRPoint.first+dst.first,upperRPoint.second+dst.second)
                gapDegree=-toRad(degree-getDegree(upperRPoint,underLPoint,false,90f))
                if(toDeg(gapDegree)<0){gapDegree+=toRad(360f)}
                wx= getDst(upperRPoint,underLPoint)*cos(gapDegree)*sin(toRad(degree))
                wy= getDst(upperRPoint,underLPoint)*cos(gapDegree)*cos(toRad(degree))
                hx= getDst(upperRPoint,underLPoint)*sin(gapDegree)*cos(toRad(-degree))
                hy= getDst(upperRPoint,underLPoint)*sin(gapDegree)*sin(toRad(-degree))
                upperLPoint=Pair(underLPoint.first+wx,underLPoint.second-wy)
                underRPoint=Pair(underLPoint.first+hx,underLPoint.second-hy)
            }
            3->{    // set size of XY underL
                underLPoint=Pair(underLPoint.first+dst.first,underLPoint.second+dst.second)
                gapDegree=-toRad(degree-getDegree(upperRPoint,underLPoint,false,90f))
                wx= getDst(underLPoint,upperRPoint)*cos(gapDegree)*sin(toRad(degree))
                wy= getDst(underLPoint,upperRPoint)*cos(gapDegree)*cos(toRad(degree))
                hx= getDst(underLPoint,upperRPoint)*sin(gapDegree)*cos(toRad(degree))
                hy= getDst(underLPoint,upperRPoint)*sin(gapDegree)*sin(toRad(degree))
                underRPoint=Pair(upperRPoint.first-wx,upperRPoint.second+wy)
                upperLPoint=Pair(upperRPoint.first-hx,upperRPoint.second-hy)
            }
            4->{    // set size of XY underR
                underRPoint=Pair(underRPoint.first+dst.first,underRPoint.second+dst.second)
                gapDegree=toRad(degree-getDegree(upperLPoint,underRPoint,false,-90f))
                wx= getDst(underRPoint,upperLPoint)*cos(gapDegree)*sin(toRad(degree))
                wy= getDst(underRPoint,upperLPoint)*cos(gapDegree)*cos(toRad(degree))
                hx= getDst(underRPoint,upperLPoint)*sin(gapDegree)*cos(toRad(degree))
                hy= getDst(underRPoint,upperLPoint)*sin(gapDegree)*sin(toRad(degree))
                underLPoint=Pair(upperLPoint.first+wx,upperLPoint.second-wy)
                upperRPoint=Pair(upperLPoint.first-hx,upperLPoint.second-hy)
            }
            5->{    // set size of X midL
                gapDegree=toRad(getDegree(pos,midLPoint,false)-degree)// 각도와 dst각도의 차이
                dx=getDst(midLPoint,pos)*cos(gapDegree)*cos(toRad(degree))
                dy=getDst(midLPoint,pos)*cos(gapDegree)*sin(toRad(degree))
                upperLPoint=Pair(upperLPoint.first+dx,upperLPoint.second+dy)
                underLPoint=Pair(underLPoint.first+dx,underLPoint.second+dy)
            }
            6->{    // set size of X midR
                gapDegree=toRad(getDegree(pos,midRPoint,false)-degree)// 각도와 dst각도의 차이
                dx=getDst(midRPoint,pos)*cos(gapDegree)*cos(toRad(degree))
                dy=getDst(midRPoint,pos)*cos(gapDegree)*sin(toRad(degree))
                underRPoint=Pair(underRPoint.first+dx,underRPoint.second+dy)
                upperRPoint=Pair(upperRPoint.first+dx,upperRPoint.second+dy)
            }
            7->{    //set size of Y underM
                gapDegree=toRad(getDegree(pos,underMPoint,false)-degree)// 각도와 dst각도의 차이
                dx=getDst(underMPoint,pos)*sin(gapDegree)*sin(toRad(degree))
                dy=getDst(underMPoint,pos) *sin(gapDegree)*cos(toRad(degree))
                underRPoint=Pair(underRPoint.first-dx,underRPoint.second+dy)
                underLPoint=Pair(underLPoint.first-dx,underLPoint.second+dy)
            }
            8->{    // set size of Y upperM
                gapDegree=toRad(getDegree(pos,upperMPoint,false)-degree)// 각도와 dst각도의 차이
                dx=getDst(upperMPoint,pos)*sin(gapDegree)*sin(toRad(degree))
                dy=getDst(upperMPoint,pos) *sin(gapDegree)*cos(toRad(degree))
                upperRPoint=Pair(upperRPoint.first-dx,upperRPoint.second+dy)
                upperLPoint=Pair(upperLPoint.first-dx,upperLPoint.second+dy)
            }
            9->{    // set pos
                upperLPoint=Pair(upperLPoint.first+dst.first,upperLPoint.second+dst.second)
                upperRPoint=Pair(upperRPoint.first+dst.first,upperRPoint.second+dst.second)
                underLPoint=Pair(underLPoint.first+dst.first,underLPoint.second+dst.second)
                underRPoint=Pair(underRPoint.first+dst.first,underRPoint.second+dst.second)
            }
            10->{   // set degree
                degree=magDegree(
                    if(getDegree(pos,midPoint,false,90f)>0)getDegree(pos,midPoint,false,90f)
                    else getDegree(pos,midPoint,false,90f)+360
                    ,10)
                if(degree==360f){degree=0f}
            }
        }
        setMidPoint()
    }

    private fun setMatrix(){ // 이미지 매트릭스 설정
        matrix.setTranslate(pos.first,pos.second)// 매트릭스 위치 설정
        if(isXFlip){// 매트릭스 플립 설정
            matrix.postScale(-1f,1f,midPoint.first,midPoint.second)

        }
        else{matrix.postScale(1f,1f,midPoint.first,midPoint.second)}
        if(isYFlip){
            matrix.postScale(1f,-1f,midPoint.first,midPoint.second)

        }
        else{matrix.postScale(1f,1f,midPoint.first,midPoint.second)}
        matrix.postRotate(degree,midPoint.first,midPoint.second)// 매트릭스 회전 설정
    }

    private fun getDegree(p1:Pair<Float,Float>,p2:Pair<Float,Float>,isRad:Boolean,offSet:Float=0f):Float{// 두 점이 이루는 각도 리턴
        return if(isRad)atan2(p1.second-p2.second,p1.first-p2.first)+toRad(offSet)
                else offSet+Math.toDegrees(atan2(p1.second-p2.second,p1.first-p2.first).toDouble()).toFloat()
    }

    private fun magDegree(degree: Float, magArea: Int = 5, section: Int = 8): Float {// 각도 자석효과
        val gap: Int = 360 / section
        return if ((degree % gap) <= magArea) (((degree / gap).toInt()) * gap).toFloat()
        else if ((degree % gap) >= gap - magArea) (((degree / gap).toInt() + 1) * gap).toFloat()
        else degree
    }

    private fun toRad(deg:Float):Float{
        return Math.toRadians(deg.toDouble()).toFloat()
    }

    private fun toDeg(rad:Float):Float{
        return Math.toDegrees(rad.toDouble()).toFloat()
    }

    private fun swapPoints(){// 박스가 뒤집어 졌을 시 점들의 위치를 스왑
        if(rotatePoint(-degree,upperRPoint,midPoint).first<rotatePoint(-degree,upperLPoint,midPoint).first){// 좌우 x좌표 스왑
            upperLPoint=upperRPoint.also { upperRPoint=upperLPoint }
            underLPoint=underRPoint.also { underRPoint=underLPoint }
            setMidPoint()
            clickedPoint=clickPosCheck(posX, posY)
            isXFlip=!isXFlip
        }
        if(rotatePoint(-degree,underLPoint,midPoint).second<rotatePoint(-degree,upperLPoint,midPoint).second){// y좌표 스왑
            upperLPoint=underLPoint.also { underLPoint=upperLPoint }
            upperRPoint=underRPoint.also { underRPoint=upperRPoint }
            setMidPoint()
            clickedPoint=clickPosCheck(posX, posY)
            isYFlip=!isYFlip
        }

    }
}