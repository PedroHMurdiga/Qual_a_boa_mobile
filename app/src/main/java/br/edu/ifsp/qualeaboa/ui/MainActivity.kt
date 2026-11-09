package br.edu.ifsp.qualeaboa.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.adapter.EventAdapter
import br.edu.ifsp.qualeaboa.data.AppDatabase
import br.edu.ifsp.qualeaboa.databinding.ActivityMainBinding
import br.edu.ifsp.qualeaboa.model.Event
import kotlinx.coroutines.launch

/** Tela principal: lista de eventos com busca. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: EventAdapter
    private val database by lazy { AppDatabase.getInstance(this) }

    private var allEvents: List<Event> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Recarrega sempre que a tela volta a aparecer, para mostrar
        // eventos criados, editados ou apagados nas outras telas.
        loadEvents()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter(onEventClick = { event -> openEditScreen(event.id) })

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupListeners() {
        binding.edtSearch.doAfterTextChanged { applySearch() }

        binding.fabCreateEvent.setOnClickListener {
            startActivity(Intent(this, CadastroEventoActivity::class.java))
        }
    }

    private fun loadEvents() {
        lifecycleScope.launch {
            allEvents = database.eventDao().getAll()
            applySearch()
        }
    }

    /** Filtra a lista pelo texto digitado na busca. */
    private fun applySearch() {
        val query = binding.edtSearch.text.toString().trim()
        val filtered = allEvents.filter { matchesSearch(it, query) }
        adapter.updateList(filtered)

        binding.txtEmpty.isVisible = filtered.isEmpty()
        binding.txtEmpty.setText(
            if (allEvents.isEmpty()) R.string.empty_no_events else R.string.empty_no_results
        )
    }

    private fun matchesSearch(event: Event, query: String): Boolean {
        if (query.isBlank()) return true
        return event.title.contains(query, ignoreCase = true) ||
            event.description.contains(query, ignoreCase = true) ||
            event.location.contains(query, ignoreCase = true)
    }

    /** Abre a tela de cadastro já com o evento escolhido, para editar ou apagar. */
    private fun openEditScreen(eventId: Long) {
        val intent = Intent(this, CadastroEventoActivity::class.java)
        intent.putExtra(CadastroEventoActivity.EXTRA_EVENT_ID, eventId)
        startActivity(intent)
    }
}
