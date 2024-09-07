package com.example.addsticker.bottomsheet

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.addsticker.R
import com.example.addsticker.adapter.StickerBottomSheetAdapter
import com.example.addsticker.databinding.StickerBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class StickerBottomSheet(private val context: Context) : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "StickerBottomSheet"
    }

    private lateinit var binding : StickerBottomSheetBinding
    private lateinit var stickerBottomSheetAdapter : StickerBottomSheetAdapter
    private lateinit var listOfSticker : ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet = (view.parent as View)
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)

        listOfSticker = arrayListOf()

        listOfSticker.add(R.drawable.a)
        listOfSticker.add(R.drawable.b)
        listOfSticker.add(R.drawable.c)
        listOfSticker.add(R.drawable.d)
        listOfSticker.add(R.drawable.e)
        listOfSticker.add(R.drawable.f)
        listOfSticker.add(R.drawable.g)
        listOfSticker.add(R.drawable.h)
        listOfSticker.add(R.drawable.i)
        listOfSticker.add(R.drawable.j)
        listOfSticker.add(R.drawable.k)
        listOfSticker.add(R.drawable.a)
        listOfSticker.add(R.drawable.b)
        listOfSticker.add(R.drawable.c)
        listOfSticker.add(R.drawable.d)
        listOfSticker.add(R.drawable.e)
        listOfSticker.add(R.drawable.f)
        listOfSticker.add(R.drawable.g)
        listOfSticker.add(R.drawable.h)
        listOfSticker.add(R.drawable.i)
        listOfSticker.add(R.drawable.j)
        listOfSticker.add(R.drawable.k)


        stickerBottomSheetAdapter = StickerBottomSheetAdapter()
        stickerBottomSheetAdapter.list = listOfSticker

        val gridLayoutManager = GridLayoutManager(requireContext(), 3)

        binding.rvSticker.layoutManager = gridLayoutManager
        binding.rvSticker.adapter = stickerBottomSheetAdapter
    }

}