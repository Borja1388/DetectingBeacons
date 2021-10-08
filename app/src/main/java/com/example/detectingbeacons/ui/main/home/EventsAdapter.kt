package com.example.detectingbeacons.ui.main.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.detectingbeacons.databinding.EventListRowBinding
import com.example.detectingbeacons.extensions.roundCorners
import com.example.detectingbeacons.models.Event

class EventsAdapter(
    private val items: List<Event>,
    private val adapterAction: (Event) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            EventListRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        ) {
            adapterAction(items[it])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class ViewHolder(private val binding: EventListRowBinding, callback: (Int) -> Unit) :
    RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.setOnClickListener {
            callback(adapterPosition)
        }
    }

    fun bind(event: Event) {
        with(binding) {
            title.text = event.title
            imageView.roundCorners(event.reference)
        }
    }
}
