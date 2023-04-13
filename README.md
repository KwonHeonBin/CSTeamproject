# DCUCSTeamproject

#### drawingPart

#### 주요 기능
- 펜
  - 버튼 터치로 색깔 변경 가능
- 지우개
  - 영역 지우개
  - 획 지우개
- 백그라운드 설정
  - 격자
  - 밑줄
- 도형 그리기(채우기 가능)
  - 사각형
  - 삼각형
  - 직선
  - 원
- 모두 지우기
- 되돌리기
- 되돌리기 취소
- 자석
- 커서
  - 이동
  - 크기 조정
- 올가미
- 이미지 추가
  - 회전
- 페이지 추가


#### 설명
 - 하단 메뉴버튼을 클릭하여 모드 변경 가능
 - 펜 선을 그리기 위한 클래스 변수는 다음과 같다.
 ```Kotlin
    var brush:Paint=Paint()// 펜을 그리기 위한 브러쉬
    var point=ArrayList<Pair<Float,Float>>()// 펜을 이루는 선들의 좌표
    var maxDistPerPoint:Float=30f// 좌표들 사이 최대 거리
    var id:Int=0// 펜들의 페이지 id
 ```
 - 캔버스 터치 시 stroke 클래스를 생성하고 드래그 중 클래스에 터치 좌표를 추가한다.
 - 선 출력 중 선의 좌표가 바뀌게 될 경우 생기는 오류를 방지하기 위해 선을 그리기위한 함수는 읽기 전용으로 접근한다.
 ```Kotlin
 private fun drawStroke(){   // 선 그리기
        val list= pathList.iterator()   // 읽기 전용
        while (list.hasNext()){
            val box=list.next()
            val path= Path()
            if(box.point.isNotEmpty()){
                path.moveTo(box.point.first().first,box.point.first().second)
                for (j in 1 until box.point.size){
                    path.lineTo(box.point[j].first,box.point[j].second)
                }
                if(box.id==page){ canvas.drawPath(path,box.brush) } // path에 있는 점에 맞춰 선을 그림
                invalidate()                //refresh View      -> if any line exist, call onDraw infinitely
            }
        }
    }
 ```
 - 너무 빠르게 선을 그을 시 좌표들 사이에 간격이 멀어져서 지우개나 커서가 선을 인식하지 못함
   - 일정 거리 이상 선들이 떨어져 있을 시 점들 사이에 점을 추가하는 기능 추가
   ```Kotlin
   private fun interpolation(point:ArrayList<Pair<Float,Float>>, gap:Float):ArrayList<Pair<Float,Float>>{
        var i=0
        while(i<point.size-1){
            if(getDst(point[i], point[i + 1]) >gap){// getDst-> 점들 사이 거리를 구해줌
                val pos=Pair((point[i].first+point[i+1].first)/2,(point[i].second+point[i+1].second)/2)// 점들 사이 중간점을 추가
                point.add(i+1,pos)
                continue
            }
            i++
        }
        return point
    }
   
   ```
 - 깊은 복사를 위해 Clonealbe을 상속
 ```Kotlin
 class Stroke : Cloneable
 .
 .
  public override fun clone(): Stroke {// clone() 재정의(Stroke의 point또한 깊은 복사가 필요)
        val stroke = super.clone() as Stroke
        stroke.point = ArrayList<Pair<Float,Float>>().apply{ addAll(point) }
        return stroke
    }
 ```
 
 - 지우개 기능을 다음과 같이 구현했다.(Eraser.erase() 구현)
   - 획 지우개는 선의 어떤 점과 지우개의 거리가 일정 이하 시 점이 포함된 선을 전부 지움
   ```Kotlin
   fun getDst(p1: Pair<Float, Float>, p2: Pair<Float, Float>): Float {// companion object에 정의 
            return sqrt(
                abs(p1.first - p2.first).pow(2)// 피타고라스 정리
                + abs(p1.second - p2.second).pow(2))
        }
   
   ```
   - 영역 지우개는 지우개와 충돌한 점을 기점으로 선이 2개로 나뉜다. 여기서 나뉜 선의 점의 갯수가 1개 이하라면 선을 삭제한다.
 - 백그라운드의 격자 및 밑줄 간격은 설정 가능하다.(canvasView.drawBackGround())
 -  도형그리기는 도형의 꼭지점들을 먼저 계산한 뒤 interpolation을 통해 꼭지점들 사이에 점들을 보충한다.
 -  모두 지우기 기능은 모든 선들을 지운다.
 -  되돌리기 기능 및 취소 기능은 다음과 같이 구현됐다.
   - 캔버스에 변화가 있을 시 unStroke에 모든 선들을 저장한다.
   ```Kotlin
   var unStroke=ArrayList<ArrayList<Stroke>>()//되돌리기
   var reStroke=ArrayList<ArrayList<Stroke>>()// 되돌리기 취소
   ```
   - 되돌리기 버튼을 눌렀을 시 unStroke에 있는 마지막 원소를 선들을 저장하는 pathList에 추가한다.
   ```Kotlin
       reStroke.add(pathList.clone() as ArrayList<Stroke>)// 취소 리스트에 현재 선들 저장
       pathList.clear()// 선을 저장하는 리스트 초기화
       pathList=unStroke.removeLast().clone() as ArrayList<Stroke>// unStroke의 마지막 원소를 제거하고 pathList에 추가
   ```
   - 되돌리기 취소 버튼이 이와 유사하게 구현되었다.
     - 선을 그을 시 취소 할 내용이 없어지므로 reStroke를 초기화
 - 자석 기능은 격자무늬 배경일때 도형그리기를 해야 적용된다.
 ```Kotlin
     private fun magnetic(point:Float, isForced:Boolean=false, degree:Float=0.2f):Float{// isForced-> 모든 영역 자석효과 degree-> 자셕효과 범위
        val degree:Float=if(isForced){0.5f}else{degree}
        val magX:Float = if(abs(point% bgGap) <= bgGap *degree) {// point의 좌표가 자석범위 내에 있을 시 bgGap단위로 변환 
            ((point/ bgGap).toInt()* bgGap).toFloat()
        } else if(abs(point% bgGap)> bgGap *(1f-degree)){
            (((point/ bgGap).toInt()+1)* bgGap).toFloat()
        } else {
            point// point가 자석범위 밖에 있을 시 그래도 리턴
        }
        return magX
    }
 ```
 
 - 커서 기능은 다음과 같이 구현했다.
   - 클릭위치와 선을 이루는 점과의 거리가 일정 거리 이하일 시 그 점이 포함된 선을 포커스(CanvasManager.strokeClick())
     - 포커스된 선은 빨간색 외각선이 그려진다.
     - 포커스된 선이 있을 시 그 선을 포함하는 최소한의 박스가 그려진다.
     
       ```Kotlin
         override fun setBox(){ // 박스 좌표 설정
          // 선택된 선들의 상하좌우 최소,최대값을 기준으로 박스를 형성
           setPoint(Pair(checkedStroke.minOf { it.point.minOf { it.first }},checkedStroke.minOf { it.point.minOf { it.second }}),
                    Pair(checkedStroke.maxOf{it.point.maxOf { it.first }},checkedStroke.minOf { it.point.minOf { it.second }}),
                    Pair(checkedStroke.minOf { it.point.minOf { it.first }},checkedStroke.maxOf{it.point.maxOf { it.second }}),
                    Pair(checkedStroke.maxOf{it.point.maxOf { it.first }},checkedStroke.maxOf{it.point.maxOf { it.second }}))
           setStrokeScale()
           }
       ```
 - 박스 클래스는 4개의 꼭지점과 각 변들의 가운데 점, 도형의 가운데점, 회전버튼 ,삭제버튼의 좌표를 변수로 가진다.
    - 최초로 박스가 만들어질때 꼭지점의 좌표만 할당되고 나머지 점들의 좌표는 꼭지점을 기준으로 계산된다.
    - 박스를 이루는 점들을 드래그할 시 박스와 선들의 크기가 바뀌고 박스 내부 클릭 시 박스에 포함된 포커스된 선들을 움직일 수 있다. 
      - 박스에 포함된 선들을 각 좌표마다 선을 이루는 점들이 박스 내부를 기준으로 한 상대위치를 가진다.
      - 상대위치를 토대로 박스의 크기를 변경 시 그 크기에 맞춰 선을 이루는 점들의 위치를 스케일링 한다.
      - 스케일링된 선들은 그 크기에 따라 interpolation과 uninterpolation과정을 거친다.
        ```Kotlin
            fun setStrokeScale(){   // 박스내부 선들의 배율 설정
                scaleOfPoints.clear()
                for (i in checkedStroke){
                    val strokeBox=ArrayList<Pair<Float,Float>>()
                    for (j in i.point){
                        // 박스의 가로 길이를 기준으로 점이 몇 퍼센트 위치에 있나 저장(가장 좌측-> 0, 가장 우측->1)
                        val scaleX=(j.first-upperLPoint.first)/(upperRPoint.first-upperLPoint.first)
                        // 박스의 세로 길이를 기준으로 점이 몇 퍼센트 위치에 있나 저장(가장 상단-> 0, 가장 하단->1)
                        val scaleY=(j.second-upperRPoint.second)/(underRPoint.second-upperRPoint.second)
                        strokeBox.add(Pair(scaleX,scaleY))
                    }
                    scaleOfPoints.add(strokeBox)
                }

            }
            fun applyScale(){   // 배율 적용
                for (i in 0 until checkedStroke.size){
                    for (j in 0 until checkedStroke[i].point.size){
                        // 박스의 가로길이에 배율을 곱한 뒤 박스의 가장 좌측 x좌표에 더하는 
                        checkedStroke[i].point[j]=Pair(((upperRPoint.first-upperLPoint.first)*scaleOfPoints[i][j].first)+upperLPoint.first,
                                                       ((underRPoint.second-upperRPoint.second)*scaleOfPoints[i][j].second)+upperRPoint.second)
                    }
                }
            }
        ```
        
 - 올가미 기능은 커서 기능의 확장판이다.
   - 올가미로 그린 점선은 최적화를 위해 일정 거리 이하인 점들은 지우는 uninterpolation과정을 거친다.
     ```Kotlin
       private fun unInterpolation(points: ArrayList<Pair<Float,Float>>, gap:Float=40f):ArrayList<Pair<Float,Float>> {
        var i=0
        while(i < points.size-1){
            while(getDst(points[i], points[i + 1]) <gap){// 점들 사이 거리가 특정 거리 미만일 시 점을 삭제
                points.remove(points[i+1])
                if(i==points.size-1){break}
            }
            i++
          }
        return points
        }
     ```
   - 올가미 내부에 선이 포함되는지 확인하는 방법은 다음과 같다.(CanvasManager.isIn())
     - 선을 이루는 점들을 기준으로 아래 방향으로 선을 긋는다.
     - 선과 올가미 사이에 접점이 홀수개라면 내부, 짝수개라면 내부에 있다.
     - 접점을 구하는 원리는 다음과 같다.
       - 올가미를 이루는 두 점을 포함하는 직선 방정식에 선에서 나온 직선의 x좌표 또는 y좌표를 대입하여 <br> 그 값이 올가미를 이루는 두 점 사이에 있다면 접점이 존재한다.
     - 최적화를 위해 선의 홀수번째 점들만 선을 긋는다.
     - 정확도 향상을 위해 더블체크를 한다.(아래, 왼쪽)  

 - 이미지 추가는 갤러리에 접근한다.
   ```Kotlin
   
       private val imageResult=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){// 갤러리에서 이미지를 선택하여 결과값이 존재할 시 
            result->
            if(result.resultCode== RESULT_OK){

                val imgUrl=result?.data?.data
                val display = this.applicationContext?.resources?.displayMetrics    // get device size
                var img=Image(imgUrl,this,true,contentResolver,Pair(20f,20f))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    var ratio=if(img.bitmapImg.width>img.bitmapImg.height){min(img.bitmapImg.width.toFloat(),display?.widthPixels!!*0.5f)/img.bitmapImg.width}
                                else{min(img.bitmapImg.height.toFloat(),display?.heightPixels!!*0.5f)/img.bitmapImg.height}// 이미지 크기가 화면 크기를 넘어갈 시 스케일링
                    img.setImageSize((img.bitmapImg.width*ratio).toInt(),(img.bitmapImg.height*ratio).toInt())
                    img.setBox()// 이미지를 포커스 할 박스 생성
                    img.id= scrollView.focusedPageId// 이미지를 표시할 페이지의 id 부여
                }
                imgList.add(img)// 이미지 리스트에 추가
            }
       }
   
       private fun openGallery(){// 갤러리 접근 허가요청 및 갤러리 접근
        val writePermission=ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission=ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
        if(writePermission==PackageManager.PERMISSION_DENIED||readPermission==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }
        else{
            val intent =Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*")
            imageResult.launch(intent)
        }
    }
   
   ```
 - 이미지 크기 조정 시 화질 열화를 방지하기 위해 이미지 클래스에 오리지널 이미지 uri를 저장
 - 이미지를 회전하기 위해 다음과 같은 과정을 거친다.
   - 회전점을 드래그 하여 이미지를 회전한다. 회전 각도는 다음과 같이 구한다.
   ```Kotlin
       private fun getDegree(p1:Pair<Float,Float>,p2:Pair<Float,Float>,isRad:Boolean):Float{
        return if(isRad)atan2(p1.second-p2.second,p1.first-p2.first)+(Math.PI/2).toFloat()// 라디안값
                else 90+Math.toDegrees(atan2(p1.second-p2.second,p1.first-p2.first).toDouble()).toFloat()// degree값
    }
   ```
   - 이때 박스를 이루는 점들도 회전을 해야하기 때문에 다음과 같은 과정을 거친다.
   ```Kotlin
       fun rotatePoint(degree:Float,point:Pair<Float,Float>,pivot:Pair<Float,Float>):Pair<Float,Float>{// 이미지의 가운데 점(pivot)을 기준으로 회전
            val dTheta=Math.toRadians(degree.toDouble()).toFloat()// 회전하는 각도
            return Pair(pivot.first+((point.first-pivot.first)*cos(dTheta)-(point.second-pivot.second)*sin(dTheta)),// 오일러회전 사용
                        pivot.second+((point.first-pivot.first)*sin(dTheta)+(point.second-pivot.second)*cos(dTheta)))
    }
   
   ```
   
 - 페이지 추가기능은 다음과 같이 구현했다.(CustomScrollView)
   - 스크롤이 끝에 닿았을 시 일정 시간동안 터치를 유지할 시 페이지 추가
   - 터치 시 스크롤 뷰 터치와 캔버스 터치가 동시에 되면 안되기 때문에 제어 변수 추가
   ```Kotlin
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isScrollable && super.onInterceptTouchEvent(ev)// isScrollable가 true일 시 스크롤 뷰 터치 가능
    }
   ```
   
 - 현제 화면에 출력되는 페이지id는 스크롤의 위치를 기준으로 정한다.
 ```Kotlin
 private fun focusedIdCheck(){
        focusedPageId=(scrollY.toFloat()/resources.displayMetrics.heightPixels.toFloat()).roundToInt()+1// 스크롤의 전체 높이에 화면 크기를 나눠 현재 페이지id 가늠
    }
 ```
