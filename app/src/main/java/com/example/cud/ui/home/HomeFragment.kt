package com.example.cud.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.AnimatedContentScope.SlideDirection.Companion.Start
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceAround
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceEvenly
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.cud.MainActivity
import com.example.cud.R
import com.example.cud.adapter.NoteAdapter
import com.example.cud.databinding.FragmentHomeBinding
import com.example.cud.entity.Note
import com.example.cud.ui.draw.DrawCanvas
import com.example.cud.viewModel.NotesViewModel
import com.google.accompanist.appcompattheme.AppCompatTheme
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class HomeFragment : Fragment() {

    lateinit var mainAct: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mainAct = context as MainActivity
    }

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var listNoteAdapter: NoteAdapter
    private lateinit var notesViewModel: NotesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _binding!!.fab.setOnClickListener{
            val nextIntent = Intent(mainAct,DrawCanvas::class.java)
            startActivity(nextIntent)
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initViewModel()
        initListener()
    }

    private fun initView(){
        binding.allnote.setHasFixedSize(true)
        listNoteAdapter = NoteAdapter()
        listNoteAdapter.notifyDataSetChanged()

        binding.allnote.layoutManager = StaggeredGridLayoutManager(1,LinearLayout.VERTICAL)
        binding.allnote.adapter = listNoteAdapter
        binding.allnote.run {

        }

        listNoteAdapter.setOnClicked(object: NoteAdapter.NoteListener{
            override fun onItemClicked(note: Note){
                Log.v("adapter",note.id.toString())
                val intent = Intent(context,DrawCanvas::class.java)
                intent.putExtra(DrawCanvas().editNoteExtra,note)
                Log.v("title", note.title)

                startActivity(intent)
            }
        })

    }

    private fun initViewModel(){
        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        notesViewModel.getNotes().observe(viewLifecycleOwner, Observer { notes->
            if(notes.isNotEmpty()){
                binding.noteEmpty.visibility = View.GONE
            } else {
                binding.noteEmpty.visibility = View.VISIBLE
            }
            listNoteAdapter.setData(notes)
        })
    }

    private fun initListener(){
        notesViewModel.setNotes()
    }

    override fun onResume() {
        super.onResume()
        //ListUpdate
        initListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@Composable
fun CardNote(modifier: Modifier, date : String, text : String){
    Column(modifier = modifier) {
        Card(modifier = modifier, elevation = 5.dp) {
            Column {
                Image( painterResource(id = R.drawable.note), contentDescription ="" )
            }
        }
        Row {
            Text(text = date)
            Text(text = "노트1", style = MaterialTheme.typography.h5, color = Color.Black)
        }
    }
}