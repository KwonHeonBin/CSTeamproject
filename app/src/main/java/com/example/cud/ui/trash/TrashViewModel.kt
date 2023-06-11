package com.example.cud.ui.trash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TrashViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "구현 예정"
    }
    val text: LiveData<String> = _text
}