package br.edu.ifsp.qualeaboa.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.qualeaboa.databinding.ItemEventBinding
import br.edu.ifsp.qualeaboa.model.Event
import br.edu.ifsp.qualeaboa.util.EventDateUtils

class EventAdapter(
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private var events: List<Event> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class ViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.txtTitle.text = event.title
            binding.txtDate.text = EventDateUtils.displayDateTime(event.date, event.time)
            binding.txtLocation.text = event.location
            binding.root.setOnClickListener { onEventClick(event) }
        }
    }
}
