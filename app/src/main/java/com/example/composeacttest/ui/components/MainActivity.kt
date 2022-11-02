@file:OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)

package com.example.composeacttest.ui.components

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.composeacttest.ui.theme.ComposeActTestTheme
import android.widget.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composeacttest.GridModel
import com.example.composeacttest.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        setContent {
            ComposeActTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    // 제일 아래 바탕색
                    color = MaterialTheme.colors.background
                ) {

                    val scaffoldState = rememberScaffoldState()
                    val coroutineScope = rememberCoroutineScope()
                    val contextForToast = LocalContext.current.applicationContext

                    Scaffold(
                        // 기본 구성 가능 함수
                        modifier = Modifier.fillMaxWidth(),
                        scaffoldState = scaffoldState,
                        floatingActionButton = {
                            val ctx = LocalContext.current
                                               FloatingActionButton(
                                                   onClick = {
                                                       Toast.makeText(ctx, "플로팅 버튼 인식", Toast.LENGTH_SHORT).show() },
                                                   backgroundColor = MaterialTheme.colors.background,
                                                   contentColor = Color.LightGray,
                                                   shape = CircleShape,) {
                                                   Icon(Icons.Filled.Add, "")
                                               }
                        },
                        // 드로워 컨텐츠로, 메뉴 버튼이 눌리면 오른쪽에서 다른 레이아웃이 나옴
                        drawerContent = {
                                        DrawerContent { itemLabel ->
                                            Toast
                                                .makeText(contextForToast,itemLabel,Toast.LENGTH_SHORT)
                                                .show()
                                            coroutineScope.launch {
                                                // delay for the ripple effect
                                                delay(timeMillis = 250)
                                                scaffoldState.drawerState.close()
                                            }
                                        }
                        },
                        // 탑바로, 어플리케이션 상단에 메뉴와 검색, 추가 메뉴가 나옴
                        topBar = {
                            TestTopAppBar {
                                coroutineScope.launch {
                                    scaffoldState.drawerState.open()
                                }
                            }
                        }
                    ){
                        // 만들어 놓은 그리드 뷰 함수 호출
                        GridView(LocalContext.current)
                    }
                    }
                }
            }
        }
    }

@Composable
private fun TestTopAppBar(onNavIconClick:() -> Unit)
{
    TopAppBar(backgroundColor = MaterialTheme.colors.background,
        title = {
            Text(
                text = "",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        },
        // 네비게이션 메뉴, 즉 버튼을 누르면 drawerContent를 출력시킴
        navigationIcon = {
            IconButton(onClick = { onNavIconClick() }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = Color.Black)
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "MoreVert",
                    tint = Color.Black
                )
            }
        }
    )
}


@Composable
private fun DrawerContent(
    itemClick: (String) -> Unit)
{
    val itemsList = prepareNavigationDrawerItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .clip(shape = RoundedCornerShape(10.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 36.dp)
    ){
        items(itemsList) { item ->
            NavigationListItem(item = item) {
                itemClick(item.label)
            }
        }

    }
}
@Composable
private fun NavigationListItem(
    item: NavigationDrawerItem,
    itemClick:() -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                itemClick()
            }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        // 아이콘과 텍스트
        Box{

            Icon(modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 1.dp)
                .size(size = 26.dp),
                painter = item.image,
                contentDescription = null,
                tint = Color.Black
            )

            //label
            Text(
                modifier = Modifier.padding(start = 45.dp),
                text = item.label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }

}


@Composable
fun prepareNavigationDrawerItems(): List<NavigationDrawerItem> {
    val itemsList = arrayListOf<NavigationDrawerItem>()

    itemsList.add(
        NavigationDrawerItem(
            image = painterResource(id = R.drawable.home),
            label = "모든 노트"
        )
    )
    itemsList.add(
        NavigationDrawerItem(
            image = painterResource(id = R.drawable.settings),
            label = "휴지통"
        )
    )

    return itemsList
}

data class NavigationDrawerItem(
    val image: Painter,
    val label: String
)

// 그리드 뷰를 불러오기 위한 그리드 뷰 함수를 만듬

@Composable
fun GridView(context: Context) {
    // Page 구현 및 페이지를 생성하는 함수 만들기
    // 임시로 이미지를 구현해 그리드 뷰가 제대로 작동하는지 확인한다.
    lateinit var courseList: List<GridModel>
    courseList = ArrayList<GridModel>()
    // 그리드 칸을 차지할 임의의 리스트를 선언한다.
    // 차후 리스트를 생성할
    courseList = courseList + GridModel("Android1", R.drawable.image04)
    courseList = courseList + GridModel("Android2", R.drawable.image04)
    courseList = courseList + GridModel("Android3", R.drawable.image04)
    courseList = courseList + GridModel("Android4", R.drawable.image04)
    courseList = courseList + GridModel("Android5", R.drawable.image04)
    courseList = courseList + GridModel("Android6", R.drawable.image04)
    courseList = courseList + GridModel("Android7", R.drawable.image04)

    // 그리드 뷰의 세로열을 선언한다.
    LazyVerticalGrid(cells = GridCells.Fixed(3), modifier = Modifier.padding(10.dp))
    {
        items(courseList.size)
        {
            Card(onClick = {
                Toast.makeText(context,
                courseList[it].languageName + " 선택됨 ",
                Toast.LENGTH_SHORT).show()
                           },
                modifier = Modifier
                    .padding(8.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .border(
                        width = 2.dp,
                        color = Color.Gray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                elevation = 4.dp)
            {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    
                    verticalArrangement = Arrangement.Center
                ){
                    Image(painter = painterResource(id = courseList[it].languageImg),
                        contentDescription = "contentDescription",
                    modifier = Modifier
                        .height(70.dp)
                        .width(50.dp)
                        .padding(5.dp)
                    )
                    //spacer를 추가
                    Spacer(modifier = Modifier.height(9.dp))

                    Text(
                        text = courseList[it].languageName,
                        modifier = Modifier.padding(4.dp),
                        color = Color.Black)
                }
            }
           
        }
    }

}
