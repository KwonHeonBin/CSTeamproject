package com.example.cud.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cud.Tool.Converter
import com.example.cud.dao.NoteDao
import com.example.cud.entity.Note

@Database(entities = [Note::class], version = 3, exportSchema = false)
@TypeConverters(Converter::class)
abstract  class NoteRoomDatabase : RoomDatabase(){
    companion object{
        @Volatile
        private var INSTANCE: NoteRoomDatabase? = null

        fun getDatabase(context: Context): NoteRoomDatabase {
            return INSTANCE
                ?: synchronized(this) {
                    // Create database here
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        NoteRoomDatabase::class.java,
                        "note_db"
                    )
                        .allowMainThreadQueries() //allows Room to executing task in main thread
                        .fallbackToDestructiveMigration() //allows Room to recreate database if no migrations found
                        .addTypeConverter(Converter())
                        .build()


                    INSTANCE = instance
                    instance
                }
        }
    }

    abstract fun getNoteDao() : NoteDao
}