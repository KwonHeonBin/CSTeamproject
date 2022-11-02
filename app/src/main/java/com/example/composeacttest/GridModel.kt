package com.example.composeacttest

// 데이터 클래스를 위한 모델 클래스 선언
// 모델 클래스란, 데이터를 저장하고 가져오는 역활을 하는 클래스이다.
//
data class GridModel (
    // 언어 이름을 위한 languageName
    val languageName: String,
    // 이미지 위치를 받기 위한 정수 값을 받는 languageImg
    val languageImg: Int
        )