package com.example.cud.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cud.R
import com.example.cud.databinding.ListItemBinding
import com.example.cud.Tool.DateChange
import com.example.cud.entity.Note

class NoteAdapter: RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    private val dataNotes = ArrayList<Note>()
    private lateinit var listener: NoteListener

    fun setOnClicked(listener: NoteListener){
        this.listener = listener
    }

    fun setData(items: ArrayList<Note>){
        dataNotes.clear()
        dataNotes.addAll(items)
        notifyDataSetChanged()
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val binding = ListItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val dateChange = DateChange()
        val item = dataNotes[position]
        //Log.v("Load file 2",item.notePathList!![0].pos_X[0].toString())

        with(holder){
            binding.textViewTitle.text = item.title
            binding.tvDate.text = dateChange.changeFormatDate(item.date)
            itemView.setOnClickListener{
                listener.onItemClicked(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataNotes.size
    }

    interface NoteListener{
        fun onItemClicked(note:Note)
    }
}