package br.edu.ifsp.qualeaboa.data

import br.edu.ifsp.qualeaboa.model.Event

/** Operações com eventos (versão em memória). */
class EventDao(private val database: AppDatabase) {

    /** Todos os eventos, mais novos primeiro. */
    suspend fun getAll(): List<Event> =
        database.events.sortedByDescending { it.id }

    suspend fun getById(id: Long): Event? =
        database.events.firstOrNull { it.id == id }

    suspend fun insert(event: Event): Long {
        val id = database.nextId()
        database.events.add(event.copy(id = id))
        return id
    }

    suspend fun update(event: Event) {
        val index = database.events.indexOfFirst { it.id == event.id }
        if (index != -1) database.events[index] = event
    }

    suspend fun delete(event: Event) {
        database.events.removeAll { it.id == event.id }
    }
}
