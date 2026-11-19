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
import br.edu.ifsp.qualeaboa.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val EXTRA_USUARIO = "usuario"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var eventos: MutableList<Evento>
    private lateinit var adapter: EventoAdapter
    private lateinit var db: QualEABoaDatabase
    private lateinit var usuario: Usuario

    // Ids dos eventos em que o usuário logado marcou "Tô dentro!"
    private var idsEventosQueEuVou = listOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eventos = mutableListOf()
        db = QualEABoaDatabase.getInstance(this)
        // O usuário logado vem da LoginActivity
        usuario = intent.getSerializableExtra(EXTRA_USUARIO) as Usuario

        setupViews()
        setupRecyclerView()
        setupListeners()
    }

    // Toda vez que a tela aparece, carrega os dados do banco.
    // Assim a lista já volta atualizada depois de criar, editar, apagar ou marcar "Tô dentro!".
    override fun onResume() {
        super.onResume()
        loadData()
    }

    fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val listaDb = db.eventoDao().listarTodos()
            val meusInteresses = db.interesseDao().listarPorUsuario(usuario.id)

            withContext(Dispatchers.Main) {
                eventos = listaDb.toMutableList()
                idsEventosQueEuVou = meusInteresses.map { it.eventoId }
                atualizarLista()
            }
        }
    }

    private fun setupViews() {
        binding.tvTitulo.text = getString(R.string.greeting, usuario.nome)
    }

    private fun setupRecyclerView() {
        adapter = EventoAdapter(eventos) { evento ->
            val intent = Intent(this, DetalheEventoActivity::class.java)
            intent.putExtra(DetalheEventoActivity.EXTRA_EVENTO, evento)
            intent.putExtra(DetalheEventoActivity.EXTRA_USUARIO, usuario)
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
            intent.putExtra(CadastroEventoActivity.EXTRA_USUARIO_ID, usuario.id)
            startActivity(intent)
        }

        binding.btnSair.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.cbMeusEventos.setOnCheckedChangeListener { _, _ ->
            atualizarLista()
        }

        binding.edtFiltro.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                atualizarLista()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    // Mostra na lista só os eventos que combinam com a busca
    // (e, se a caixinha estiver marcada, só os que o usuário vai)
    private fun atualizarLista() {
        val filtro = binding.edtFiltro.text.toString().lowercase()
        val somenteOsQueEuVou = binding.cbMeusEventos.isChecked

        val filtrados = eventos.filter {
            val combinaComBusca = it.nome.lowercase().contains(filtro) ||
                it.local.lowercase().contains(filtro)

            if (somenteOsQueEuVou) {
                combinaComBusca && idsEventosQueEuVou.contains(it.id)
            } else {
                combinaComBusca
            }
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
