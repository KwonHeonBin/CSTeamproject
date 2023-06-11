package com.example.cud.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cud.databinding.FragmentSettingsBinding
import com.google.accompanist.appcompattheme.AppCompatTheme


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun  onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View{

        _binding = FragmentSettingsBinding.inflate(inflater,container,false)
        val root:View = binding.root

        _binding!!.settingsView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent { 
                AppCompatTheme{
                    Text(text = "구현 예정...")
                }
            }
        }

        return root
    }

}