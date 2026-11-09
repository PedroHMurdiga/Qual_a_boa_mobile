package br.edu.ifsp.qualeaboa.data

import android.content.Context
import br.edu.ifsp.qualeaboa.model.Event

/**
 * Banco de dados PROVISÓRIO, guardado só na memória.
 * Os dados somem quando o app é fechado. Ele será trocado pelo Room.
 */
class AppDatabase private constructor() {

    internal val events = mutableListOf<Event>()

    private var lastId = 0L

    /** Gera um id novo, como o autoGenerate do banco faria. */
    internal fun nextId(): Long {
        lastId++
        return lastId
    }

    private val eventDao = EventDao(this)

    fun eventDao(): EventDao = eventDao

    companion object {
        private var instance: AppDatabase? = null

        @Suppress("UNUSED_PARAMETER") // o Room vai precisar do context
        fun getInstance(context: Context): AppDatabase {
            return instance ?: AppDatabase().also { instance = it }
        }
    }
}
