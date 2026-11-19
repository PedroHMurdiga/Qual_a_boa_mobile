package br.edu.ifsp.qualeaboa.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.data.QualEABoaDatabase
import br.edu.ifsp.qualeaboa.databinding.ActivityCadastroEventoBinding
import br.edu.ifsp.qualeaboa.model.Evento
import br.edu.ifsp.qualeaboa.util.DataUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

// Tela usada para criar um evento novo e também para editar um evento que já existe
class CadastroEventoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EVENTO = "evento"
        const val EXTRA_USUARIO_ID = "usuarioId"
        const val CHAVE_FOTO = "foto"
        const val HORA_PADRAO = 19
    }

    private lateinit var binding: ActivityCadastroEventoBinding
    private lateinit var launcherGaleria: ActivityResultLauncher<Array<String>>
    private var eventoEditado: Evento? = null
    private var usuarioId = 0
    private var uriSelecionada = ""
    private lateinit var db: QualEABoaDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QualEABoaDatabase.getInstance(this)

        loadData()
        setupViews()
        setupLauncher()
        setupListeners()

        // Só preenche os campos na primeira vez. Ao girar a tela, o Android
        // já devolve sozinho o que estava digitado.
        if (savedInstanceState == null) {
            preencherCampos()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CHAVE_FOTO, uriSelecionada)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        uriSelecionada = savedInstanceState.getString(CHAVE_FOTO, "")
        mostrarFoto()
    }

    // Se veio um evento pela Intent, é uma edição
    private fun loadData() {
        eventoEditado = intent.getSerializableExtra(EXTRA_EVENTO) as? Evento
        usuarioId = intent.getIntExtra(EXTRA_USUARIO_ID, 0)
    }

    private fun setupViews() {
        if (eventoEditado != null) {
            binding.tvTitulo.setText(R.string.title_edit_event)
        } else {
            binding.tvTitulo.setText(R.string.title_new_event)
        }
    }

    private fun setupLauncher() {
        launcherGaleria = registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                uriSelecionada = uri.toString()
                mostrarFoto()
            }
        }
    }

    private fun setupListeners() {
        binding.imgFoto.setOnClickListener {
            launcherGaleria.launch(arrayOf("image/*"))
        }
        binding.edtData.setOnClickListener { abrirCalendario() }
        binding.edtHorario.setOnClickListener { abrirRelogio() }
        binding.btnSalvar.setOnClickListener { salvarEvento() }
        binding.btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun preencherCampos() {
        val evento = eventoEditado
        if (evento != null) {
            binding.edtNome.setText(evento.nome)
            binding.edtDescricao.setText(evento.descricao)
            binding.edtData.setText(evento.data)
            binding.edtHorario.setText(evento.horario)
            binding.edtLocal.setText(evento.local)
            uriSelecionada = evento.foto
        }
        mostrarFoto()
    }

    private fun mostrarFoto() {
        if (uriSelecionada.isNotEmpty()) {
            binding.imgFoto.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.imgFoto.setImageURI(uriSelecionada.toUri())
        } else {
            // Sem foto: mostra o ícone padrão sem esticar
            binding.imgFoto.scaleType = ImageView.ScaleType.CENTER_INSIDE
            binding.imgFoto.setImageResource(R.drawable.ic_event_placeholder)
        }
    }

    private fun abrirCalendario() {
        val hoje = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                binding.edtData.setText(DataUtils.formatarData(ano, mes, dia))
            },
            hoje.get(Calendar.YEAR),
            hoje.get(Calendar.MONTH),
            hoje.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun abrirRelogio() {
        val dialog = TimePickerDialog(
            this,
            { _, hora, minuto ->
                binding.edtHorario.setText(DataUtils.formatarHora(hora, minuto))
            },
            HORA_PADRAO,
            0,
            true
        )
        // O horário é opcional, então tem um botão para deixar sem horário
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.action_no_time)) { _, _ ->
            binding.edtHorario.setText("")
        }
        dialog.show()
    }

    private fun salvarEvento() {
        val nome = binding.edtNome.text.toString().trim()
        val descricao = binding.edtDescricao.text.toString().trim()
        val data = binding.edtData.text.toString()
        val horario = binding.edtHorario.text.toString()
        val local = binding.edtLocal.text.toString().trim()

        if (nome.isNotBlank() && data.isNotBlank() && local.isNotBlank()) {
            // Na edição mantém o mesmo id e o mesmo criador.
            // Num evento novo o id fica 0 (o Room cria um sozinho) e o criador é o usuário logado.
            val evento = eventoEditado
            var id = 0
            var criadorId = usuarioId
            if (evento != null) {
                id = evento.id
                criadorId = evento.criadorId
            }

            val eventoSalvo = Evento(
                id = id,
                nome = nome,
                descricao = descricao,
                data = data,
                horario = horario,
                local = local,
                foto = uriSelecionada,
                criadorId = criadorId
            )
            salvarNoBanco(eventoSalvo)
        } else {
            Toast.makeText(this, R.string.error_fill_event, Toast.LENGTH_SHORT).show()
        }
    }

    private fun salvarNoBanco(evento: Evento) {
        lifecycleScope.launch(Dispatchers.IO) {
            if (eventoEditado == null) {
                db.eventoDao().inserir(evento)
            } else {
                db.eventoDao().atualizar(evento)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CadastroEventoActivity, R.string.message_event_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
