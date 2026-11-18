package br.edu.ifsp.qualeaboa.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.adapter.EventoAdapter
import br.edu.ifsp.qualeaboa.data.QualEABoaDatabase
import br.edu.ifsp.qualeaboa.databinding.ActivityMainBinding
import br.edu.ifsp.qualeaboa.model.Evento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var eventos: MutableList<Evento>
    private lateinit var adapter: EventoAdapter
    private lateinit var db: QualEABoaDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventos = mutableListOf()
        db = QualEABoaDatabase.getInstance(this)

        setupRecyclerView()
        setupListeners()
    }

    // Toda vez que a tela aparece, carrega os eventos do banco.
    // Assim a lista já volta atualizada depois de criar, editar ou apagar.
    override fun onResume() {
        super.onResume()
        loadData()
    }

    fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listaDb = db.eventoDao().listarTodos()

            withContext(Dispatchers.Main) {
                eventos = listaDb.toMutableList()
                atualizarLista()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, DetalheEventoActivity::class.java)
            intent.putExtra(DetalheEventoActivity.EXTRA_EVENTO, evento)
            startActivity(intent)
        }

        binding.rvEventos.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.btnAdicionar.setOnClickListener {
            val intent = Intent(this, CadastroEventoActivity::class.java)
            startActivity(intent)
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
}
