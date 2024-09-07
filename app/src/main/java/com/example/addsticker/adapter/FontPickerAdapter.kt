package com.example.addsticker.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.addsticker.R

@SuppressLint("RecyclerView")
class FontPickerAdapter(private val context: Context, private val fontPickerList: List<Int>) :
    RecyclerView.Adapter<FontPickerAdapter.ViewHolder>() {
    private var onFontPickerClickListener: OnFontPickerClickListener? = null
    private var selectedPosition = 0

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentFontResId = fontPickerList[position]
        val typeface = context.resources.getFont(currentFontResId)
        holder.fontPickerView.typeface = typeface

        // Set background color based on selection
        holder.itemView.setBackgroundColor(
            if (position == selectedPosition) {
                context.resources.getColor(android.R.color.white)
            } else {
                context.resources.getColor(android.R.color.transparent)
            }
        )

        holder.fontPickerView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged() // Update the UI for the selected item
            onFontPickerClickListener?.onFontPickerClickListener(fontPickerList[position])
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.font_item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fontPickerList!!.size
    }



    fun setOnFontPickerClickListener(onFontPickerClickListener: OnFontPickerClickListener?) {
        this.onFontPickerClickListener = onFontPickerClickListener
    }


     class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fontPickerView: TextView = itemView.findViewById(R.id.font_picker_view)
    }


    interface OnFontPickerClickListener {
        fun onFontPickerClickListener(fontCode: Int)
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }


}