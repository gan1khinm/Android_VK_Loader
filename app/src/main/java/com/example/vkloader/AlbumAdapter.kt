package com.example.vkloader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AlbumAdapter(private val callback: AlbumCallback) :
    ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(AlbumComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        return AlbumViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { callback.onClick(item) }
        holder.bind(item)
    }

    class AlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val albumCount: TextView = itemView.findViewById(R.id.album_count)
        private val albumCover: ImageView = itemView.findViewById(R.id.album_img)


        fun bind(item: Album) {
            albumCount.text = item.count.toString()
            Glide.with(itemView).load(item.coverUrl).centerCrop().into(albumCover)
        }

        companion object {
            fun create(parent: ViewGroup): AlbumViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_album, parent, false)
                return AlbumViewHolder(view)
            }
        }
    }

    class AlbumComparator : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id && oldItem.title == newItem.title &&
                    oldItem.count == newItem.count && oldItem.coverUrl == newItem.coverUrl
        }
    }
}