package br.edu.ifsp.qualeaboa.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.data.QualEABoaDatabase
import br.edu.ifsp.qualeaboa.databinding.ActivityDetalheEventoBinding
import br.edu.ifsp.qualeaboa.model.Evento
import br.edu.ifsp.qualeaboa.util.DataUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalheEventoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EVENTO = "evento"
        const val DUAS_HORAS = 2 * 60 * 60 * 1000L
    }

    private lateinit var binding: ActivityDetalheEventoBinding
    private lateinit var evento: Evento
    private lateinit var db: QualEABoaDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalheEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QualEABoaDatabase.getInstance(this)

        loadData()
        setupViews()
        setupListeners()
    }

    private fun loadData() {
        evento = intent.getSerializableExtra(EXTRA_EVENTO) as Evento
    }

    private fun setupViews() {
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
            binding.tvData.text = getString(R.string.evento_data_horario, evento.data, evento.horario)
        }

        if (evento.descricao.isEmpty()) {
            binding.tvDescricao.setText(R.string.no_description)
        } else {
            binding.tvDescricao.text = evento.descricao
        }
    }

    private fun setupListeners() {
        binding.btnMapa.setOnClickListener { abrirMapa() }
        binding.btnAgenda.setOnClickListener { adicionarNaAgenda() }
        binding.btnEditar.setOnClickListener { editarEvento() }
        binding.btnExcluir.setOnClickListener { confirmarExclusao() }
        binding.btnVoltar.setOnClickListener { finish() }
    }

    // Intent implícita: abre o local do evento no Google Maps
    private fun abrirMapa() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = "geo:0,0?q=${Uri.encode(evento.local)}".toUri()

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_no_map_app, Toast.LENGTH_SHORT).show()
        }
    }

    // Intent implícita: abre a agenda do celular com o evento já preenchido
    private fun adicionarNaAgenda() {
        val inicio = DataUtils.converterParaMillis(evento.data, evento.horario)
        if (inicio == null) {
            Toast.makeText(this, R.string.error_invalid_date, Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_INSERT)
        intent.data = CalendarContract.Events.CONTENT_URI
        intent.putExtra(CalendarContract.Events.TITLE, evento.nome)
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, evento.local)
        intent.putExtra(CalendarContract.Events.DESCRIPTION, evento.descricao)
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicio)

        if (evento.horario.isEmpty()) {
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
        } else {
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, inicio + DUAS_HORAS)
        }

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.error_no_calendar_app, Toast.LENGTH_SHORT).show()
        }
    }

    // Abre o cadastro para editar e fecha o detalhe.
    // Depois de salvar, o usuário volta para a lista, que recarrega do banco.
    private fun editarEvento() {
        val intent = Intent(this, CadastroEventoActivity::class.java)
        intent.putExtra(CadastroEventoActivity.EXTRA_EVENTO, evento)
        startActivity(intent)
        finish()
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, evento.nome))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.confirm_delete) { _, _ -> excluirEvento() }
            .show()
    }

    private fun excluirEvento() {
        lifecycleScope.launch(Dispatchers.IO) {
            db.eventoDao().deletar(evento)

            withContext(Dispatchers.Main) {
                Toast.makeText(this@DetalheEventoActivity, R.string.message_event_deleted, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
