package com.example.addsticker.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.addsticker.R
import com.example.addsticker.rxbus.RxBus
import com.example.addsticker.rxbus.RxEvent


class StickerBottomSheetAdapter():RecyclerView.Adapter<StickerBottomSheetAdapter.ViewHolder>() {
    var list: List<Int>? = null

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val image : ImageView = itemView.findViewById(R.id.stickerItemImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var View = LayoutInflater.from(parent.context).inflate(R.layout.sticker_item,parent,false)
        return ViewHolder(View)
    }

    override fun getItemCount(): Int {
       return list?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val currItem = list?.get(position)

        if (currItem != null) {
            holder.image.setImageResource(currItem)
        }
        holder.image.setOnClickListener {
            RxBus.publish(RxEvent.ItemClick(itemId = currItem ?: -1))
        }
    }


}

