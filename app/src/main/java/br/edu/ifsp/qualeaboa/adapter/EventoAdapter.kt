package br.edu.ifsp.qualeaboa.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.databinding.ItemEventoBinding
import br.edu.ifsp.qualeaboa.model.Evento

class EventoAdapter(
    private var eventos: List<Evento>,
    private val onClick: (Evento) -> Unit
) : RecyclerView.Adapter<EventoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEventoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(eventos[position])
    }

    override fun getItemCount(): Int = eventos.size

    fun updateLista(novosEventos: List<Evento>) {
        this.eventos = novosEventos
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemEventoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(evento: Evento) {
            if (evento.foto.isNotEmpty()) {
                binding.imgFoto.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.imgFoto.setImageURI(evento.foto.toUri())
            } else {
                // Sem foto: mostra o ícone padrão sem esticar
                binding.imgFoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
                binding.imgFoto.setImageResource(R.drawable.ic_event_placeholder)
            }

            binding.tvNome.text = evento.nome
            binding.tvLocal.text = evento.local

            if (evento.horario.isEmpty()) {
                binding.tvData.text = evento.data
            } else {
                binding.tvData.text = binding.root.context.getString(
                    R.string.evento_data_horario, evento.data, evento.horario
                )
            }

            binding.root.setOnClickListener { onClick(evento) }
        }
    }
}
