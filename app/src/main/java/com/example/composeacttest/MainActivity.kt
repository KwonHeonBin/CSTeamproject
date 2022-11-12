@file:OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class,
    ExperimentalFoundationApi::class
)

package com.example.composeacttest.ui.components

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.Modifier
import com.example.composeacttest.ui.theme.ComposeActTestTheme
import android.widget.*
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.composeacttest.data.GridModel
import com.example.composeacttest.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

enum class MainScreen(@StringRes val title: Int)
{
    Start(title = R.string.app_name),
    Note(title = R.string.note_page)
}


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeActTestTheme {
                // 임의로 메인 액티비티에 다 떄려박았음, 다음 주 중에 분리해 정리 할 예정
                // 엄청 더러우니 물어보면 그때가 제정신이라면 제대로 답할 수 있음 아마도
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    //scaffoldState는 scaffoldState를 저장하는 변수다.
    val scaffoldState = rememberScaffoldState()
    //coroutinScope는,코루틴이 실행되는 범위로, 코루틴은 쓰레드와 기능적으로 비슷하지만,
    //하나의 쓰레드 내에 여러 개의 코루틴이 실행 되는 개념이다. 즉, 코루틴은 쓰레드의 간소화 버전
    //차후 자세히 공부를 해 이후 메인 액티비티를 정리하면서 제대로 정리할 예정.
    //이거 이해 못해서 노트 레이아웃에서 버튼을 눌렀을시 추가로 Box가 뜨는 걸 구현 못했음.
    val coroutineScope = rememberCoroutineScope()
    // Toast를 불러오기 위함.
    val contextForToast = LocalContext.current.applicationContext
    // 현재 백스택 앤트리? 를 가져옴
    val backStackEntry by navController.currentBackStackEntryAsState()
    // 현재 화면의 이름 값을 가져옴
    val currentScreen = MainScreen.valueOf(
        backStackEntry?.destination?.route?: MainScreen.Start.name
    )
    Scaffold(
        // 기본 구성 가능 함수
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        scaffoldState = scaffoldState,
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
        // 2022-11-10. 탑바에 추가로 canNaviBack이 된다면 다른 아이콘이 출력 되도록 해놨음.
        topBar = {
                TestTopAppBar(
                    currentScreen = currentScreen,
                    canNavigateBack = navController.previousBackStackEntry != null,
                    navigateUp = { navController.navigateUp()}
                )
                {
                    coroutineScope.launch {
                        scaffoldState.drawerState.open()}
            }
        }
    ){
        NavHost(
            navController = navController,
            startDestination = MainScreen.Start.name,
            modifier = modifier){
            composable(route = MainScreen.Start.name){
                MainView(
                    onFloatingButtonClicked = {
                        navController.navigate(MainScreen.Note.name)
                    }
                )
            }
            composable(route = MainScreen.Note.name){
                MNoteScreen(
                    onCancelButtonClicked = {
                        navController.navigate(MainScreen.Start.name)
                    },
                    item = DrawWriteItem(1)
                )
            }
        }
    }
}


// TestTopAppBar에서 Back navigation이 가능하면, Back_Button이 발생하도록 했음. 아니면 navigationDrawer를
// 출력하는 메뉴 버튼이 출력됨.
@Composable
fun TestTopAppBar(
    currentScreen: MainScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    onNavIconClick:() -> Unit)
{
    TopAppBar(backgroundColor = MaterialTheme.colors.background,
        modifier = modifier,
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
            if(canNavigateBack)
            {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button)
                    )
                }
            }
            // 되돌릴 수 있으면 되돌리는 버튼이 나옴
            else
            {
                IconButton(onClick = { onNavIconClick() }) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "Menu"
                    )
                }
            }
        },
        // 되돌아 갈 수 없다면 메인 메뉴라고 판단하고, 메인 메뉴의 창이 뜨도록 했음. 차후 설정이나 자세한 것을
        // 추가하게 되면 조건을 고쳐야 함, 설정에는 TopBar가 필요없기 때문.
        // ex) enum class를 이용해 임의의 화면 타이틀 값을 저장해 화면 타이틀 값이 얼정한 값이 된다면
        // if()으로 조건에 맞는 화면의 TopBarIcon이 나오도록 함
        actions = {
            if (canNavigateBack) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = "임의로 지정",
                        tint = Color.Black
                    )
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "차후 바꿀 예정",
                        tint = Color.Black
                    )
                }
            }else {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "페이지 검색을 위한 아이콘 버튼",
                        tint = Color.Black
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "페이지의 추가 메뉴를 출력하게 할 아이콘 버튼",
                        tint = Color.Black
                    )

                }
            }
        }
    )
}

// 임시로 만든 드롭다운 메뉴
@Composable
fun MainDropdownMenu(

){
    var isDropDownMenuExpanded by remember {
        mutableStateOf(false)
    }
    DropdownMenu(modifier = Modifier.wrapContentSize(),
        expanded = isDropDownMenuExpanded,
        onDismissRequest = { isDropDownMenuExpanded = false }) 
    {
        DropdownMenuItem(onClick = { println("Text") }) {
            Text(text = "Print Text")
        }
        DropdownMenuItem(onClick = { println("ABC") }) {
            Text(text = "ABC Print")
        }
    }
}

// 사실상, DrawerItem의 레이아웃임. itemList에 미리 만들어 놓은 메뉴 값을 넣어 출력하도록 함.
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
// DrawItem을 위한 NavigationListItem, 이것이 NavigationDrawerItem의 메뉴를 출력한다.
@Composable
private fun NavigationListItem(
    item: NavigationDrawerItem,
    itemClick:() -> Unit,
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
            //아이콘 지정
            Icon(modifier = Modifier
                .padding(horizontal = 4.dp, vertical = 1.dp)
                .size(size = 26.dp),
                painter = item.image,
                contentDescription = null,
                tint = Color.Black
            )
            //텍스트 지정
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

//DrawerItem에서 나오는 메뉴의 값을 저장하는 함수.
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

// DrawerItem의 데이터 클래스
data class NavigationDrawerItem(
    val image: Painter,
    val label: String
)

data class DrawWriteItem(
    val num: Int
)

// 노트 화면을 만드는 함수. 여기서만 BottomAppBar가 출력하게 만듬. 기존 화면에는 BottomBar가 필요없음.
// 아니면 그냥 위에 합쳐서 화면이 Note면 출력하도록 만들까도 생각 중.
@Composable
fun MNoteScreen(
    onCancelButtonClicked: () -> Unit ={},
    item: DrawWriteItem,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()

    ){
        var offset by remember {
            mutableStateOf(0f)
        }
        Spacer(Modifier.weight(1f,true))
        BottomAppBar(
            Modifier
                .background(Color.White)
                .fillMaxWidth()
                // 드래그 하기 위해서 추가 해봤는데 안됨 자세히 알아봐야할듯
                .draggable(
                    orientation = Orientation.Horizontal, state = rememberDraggableState()
                    { delta ->
                        offset += delta
                        delta
                    }
                )
        ) {
            // 깊은 복사를 사용해서 해야할텐데 갔다와서 하는걸로 mutable 쓰면 될지도
            if(item.num == 1) {
                IconButton(onClick = { DrawWriteItem(0) }) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.Person, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.Person, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.Person, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.Person, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.Person, contentDescription = "Localized description")
                }
            }
            if(item.num == 0) {
                IconButton(onClick = { DrawWriteItem(1) }) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Localized description")
                }
                IconButton(onClick = { /* doSomething() */ }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Localized description")
                }
            }






        }
    }
}
// 각 그리기 도구의 버튼을 눌렀을 시, 나올 레이아웃. 우선 PenSetting만 임의로 구성해놨음.
@Composable
fun PenSetting(){
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.Black)
            .padding(2.dp))
    {
        Icon(
            painter = painterResource(id = R.drawable.menu),
            contentDescription = null,
            Modifier
                .padding(2.dp)
                .clickable(onClick = {/* doSomething() */ }))
    }

}

@Composable
fun EraserSetting(){

}

@Composable
fun ColorSetting(){

}

@Composable
fun ShapeSetting(){

}
// 메인 뷰
@Composable
fun MainView(
    onFloatingButtonClicked: () -> Unit = {},
    modifier: Modifier = Modifier
){
    //scaffoldState는 scaffoldState를 저장하는 변수다.
    val scaffoldState = rememberScaffoldState()

    Scaffold(
        // 기본 구성 가능 함수
        modifier = Modifier.fillMaxWidth(),
        scaffoldState = scaffoldState,
        content = { GridView(LocalContext.current) },
        floatingActionButton = {
            val ctx = LocalContext.current
            FloatingActionButton(
                // 클릭이 되었을 경우, onFloatingButtonClicked가 Note로 Navigate한다.
                onClick = onFloatingButtonClicked,
                backgroundColor = MaterialTheme.colors.background,
                contentColor = Color.Gray,
                shape = CircleShape) {
                Icon(Icons.Filled.Add, "")
            }
        }
    )

}


// 그리드 뷰를 불러오기 위한 그리드 뷰 함수를 만듬
@Composable
fun GridView(context: Context) {
    // Page 구현 및 페이지를 생성하는 함수 만들기
    // 임시로 이미지를 구현해 그리드 뷰가 제대로 작동하는지 확인한다.
    lateinit var courseList: List<GridModel>
    courseList = ArrayList<GridModel>()
    // 그리드 칸을 차지할 임의의 리스트를 선언한다.
    // 차후 리스트를 생성할
    courseList = courseList + GridModel("Android1", R.drawable.document)
    courseList = courseList + GridModel("Android2", R.drawable.document)
    courseList = courseList + GridModel("Android3", R.drawable.document)
    courseList = courseList + GridModel("Android4", R.drawable.document)
    courseList = courseList + GridModel("Android5", R.drawable.document)
    courseList = courseList + GridModel("Android6", R.drawable.document)
    courseList = courseList + GridModel("Android7", R.drawable.document)


                // 그리드 뷰의 세로열을 선언한다.
                LazyVerticalGrid(
                    cells = GridCells.Fixed(3),
                    modifier = Modifier
                        .padding(10.dp)
                        .background(Color.White))
                {
                    items(courseList.size)
                    {
                        Card(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    courseList[it].languageName + " 선택됨 ",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(shape = RoundedCornerShape(10.dp)),
                            elevation = 15.dp
                        )
                        {
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = courseList[it].languageImg),
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
                                    color = Color.Black
                                )
                            }
                        }

                    }
                }
            }