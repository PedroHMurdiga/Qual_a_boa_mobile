package br.edu.ifsp.qualeaboa.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.adapter.EventoAdapter
import br.edu.ifsp.qualeaboa.databinding.ActivityMainBinding
import br.edu.ifsp.qualeaboa.model.Evento

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val CHAVE_EVENTOS = "eventos"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var eventos: MutableList<Evento>
    private lateinit var adapter: EventoAdapter
    private lateinit var launcherCadastro: ActivityResultLauncher<Intent>
    private lateinit var launcherDetalhe: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Por enquanto os eventos ficam só na memória (o banco de dados vem depois)
        eventos = mutableListOf()

        setupRecyclerView()
        setupLaunchers()
        setupListeners()
        atualizarLista()
    }

    // Guarda a lista antes da tela ser recriada (ex.: quando gira o celular)
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(CHAVE_EVENTOS, ArrayList(eventos))
    }

    @Suppress("UNCHECKED_CAST")
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val eventosSalvos = savedInstanceState.getSerializable(CHAVE_EVENTOS) as ArrayList<Evento>
        eventos.clear()
        eventos.addAll(eventosSalvos)
        atualizarLista()
    }

    private fun setupRecyclerView() {
        adapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, DetalheEventoActivity::class.java)
            intent.putExtra(DetalheEventoActivity.EXTRA_EVENTO, evento)
            launcherDetalhe.launch(intent)
        }

        binding.rvEventos.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupLaunchers() {
        // Volta da tela de cadastro com um evento novo
        launcherCadastro = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val novoEvento = result.data?.getSerializableExtra(
                    CadastroEventoActivity.EXTRA_EVENTO
                ) as? Evento

                if (novoEvento != null) {
                    eventos.add(0, novoEvento)
                    atualizarLista()
                    Toast.makeText(this, R.string.message_event_saved, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Volta da tela de detalhes: o evento pode ter sido editado ou excluído
        launcherDetalhe = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val evento = result.data?.getSerializableExtra(
                    DetalheEventoActivity.EXTRA_EVENTO
                ) as? Evento
                val acao = result.data?.getStringExtra(DetalheEventoActivity.EXTRA_ACAO)

                if (evento != null) {
                    if (acao == DetalheEventoActivity.ACAO_EXCLUIR) {
                        excluirEvento(evento)
                    } else if (acao == DetalheEventoActivity.ACAO_EDITAR) {
                        atualizarEvento(evento)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnAdicionar.setOnClickListener {
            val intent = Intent(this, CadastroEventoActivity::class.java)
            launcherCadastro.launch(intent)
        }

        binding.edtFiltro.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                atualizarLista()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Mostra na lista só os eventos que combinam com o texto da busca
    private fun atualizarLista() {
        val filtro = binding.edtFiltro.text.toString().lowercase()

        val filtrados = eventos.filter {
            it.nome.lowercase().contains(filtro) || it.local.lowercase().contains(filtro)
        }
        adapter.updateLista(filtrados)

        if (filtrados.isEmpty()) {
            binding.tvVazio.visibility = View.VISIBLE
            if (eventos.isEmpty()) {
                binding.tvVazio.setText(R.string.empty_no_events)
            } else {
                binding.tvVazio.setText(R.string.empty_no_results)
            }
        } else {
            binding.tvVazio.visibility = View.GONE
        }
    }

    private fun atualizarEvento(eventoEditado: Evento) {
        val posicao = eventos.indexOfFirst { it.id == eventoEditado.id }
        if (posicao != -1) {
            eventos[posicao] = eventoEditado
            atualizarLista()
        }
    }

    private fun excluirEvento(evento: Evento) {
        eventos.removeAll { it.id == evento.id }
        atualizarLista()
        Toast.makeText(this, R.string.message_event_deleted, Toast.LENGTH_SHORT).show()
    }
}
