package br.edu.ifsp.qualeaboa.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.databinding.ActivityDetalheEventoBinding
import br.edu.ifsp.qualeaboa.model.Evento
import br.edu.ifsp.qualeaboa.util.DataUtils

class DetalheEventoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EVENTO = "evento"
        const val EXTRA_ACAO = "acao"
        const val ACAO_EDITAR = "editar"
        const val ACAO_EXCLUIR = "excluir"
        const val CHAVE_FOI_EDITADO = "foiEditado"
        const val DUAS_HORAS = 2 * 60 * 60 * 1000L
    }

    private lateinit var binding: ActivityDetalheEventoBinding
    private lateinit var evento: Evento
    private lateinit var launcherEdicao: ActivityResultLauncher<Intent>
    private var foiEditado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalheEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadData()
        setupViews()
        setupLauncher()
        setupListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(EXTRA_EVENTO, evento)
        outState.putBoolean(CHAVE_FOI_EDITADO, foiEditado)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        evento = savedInstanceState.getSerializable(EXTRA_EVENTO) as Evento
        foiEditado = savedInstanceState.getBoolean(CHAVE_FOI_EDITADO)
        setupViews()
        if (foiEditado) {
            devolverEventoEditado()
        }
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

    // Volta da tela de cadastro com o evento editado
    private fun setupLauncher() {
        launcherEdicao = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val eventoEditado = result.data?.getSerializableExtra(
                    CadastroEventoActivity.EXTRA_EVENTO
                ) as? Evento

                if (eventoEditado != null) {
                    evento = eventoEditado
                    foiEditado = true
                    setupViews()
                    devolverEventoEditado()
                    Toast.makeText(this, R.string.message_event_saved, Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun editarEvento() {
        val intent = Intent(this, CadastroEventoActivity::class.java)
        intent.putExtra(CadastroEventoActivity.EXTRA_EVENTO, evento)
        launcherEdicao.launch(intent)
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, evento.nome))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.confirm_delete) { _, _ -> excluirEvento() }
            .show()
    }

    // Avisa a MainActivity que o evento deve ser excluído e fecha a tela
    private fun excluirEvento() {
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_EVENTO, evento)
        resultIntent.putExtra(EXTRA_ACAO, ACAO_EXCLUIR)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    // Deixa o resultado pronto: quando o usuário voltar, a MainActivity recebe o evento editado
    private fun devolverEventoEditado() {
        val resultIntent = Intent()
        resultIntent.putExtra(EXTRA_EVENTO, evento)
        resultIntent.putExtra(EXTRA_ACAO, ACAO_EDITAR)
        setResult(Activity.RESULT_OK, resultIntent)
    }
}
