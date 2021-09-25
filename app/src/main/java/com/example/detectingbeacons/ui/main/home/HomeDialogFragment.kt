package com.example.detectingbeacons.ui.main.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.example.detectingbeacons.databinding.FragmentHomeDialogBinding
import com.example.detectingbeacons.extensions.roundCorners

class HomeDialogFragment : DialogFragment() {
    private var _binding: FragmentHomeDialogBinding? = null
    private val binding: FragmentHomeDialogBinding
        get() = _binding!!
    private val navArgs: HomeDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.beaconImage.roundCorners(navArgs.beaconImage)
        binding.imageClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}