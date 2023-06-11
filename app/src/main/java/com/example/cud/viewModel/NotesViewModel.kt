package com.example.cud.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cud.dao.NoteDao
import com.example.cud.database.NoteRoomDatabase
import com.example.cud.entity.Note

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val listNotes = MutableLiveData<ArrayList<Note>>()
    private var dao: NoteDao

    init {
        val database = NoteRoomDatabase.getDatabase(context)
        dao = database.getNoteDao()
    }

    fun setNotes(){
        val listItems = arrayListOf<Note>()

        listItems.addAll(dao.getAll())
        listNotes.postValue(listItems)
    }

    fun setNotesByTitle(title:String){
        val listItems = arrayListOf<Note>()

        listItems.addAll(dao.getByTitle(title))
        listNotes.postValue(listItems)
    }

    fun insertnote(note: Note){
        dao.insert(note)
    }

    fun updateNote(note: Note){
        dao.update(note)
    }

    fun deleteNote(note: Note){
        dao.delete(note)
    }

    fun getNotes(): LiveData<ArrayList<Note>> {
        return listNotes
    }
}