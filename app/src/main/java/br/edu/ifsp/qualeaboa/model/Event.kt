package br.edu.ifsp.qualeaboa.model

/**
 * Evento ("uma boa").
 * - date: texto no formato dd/MM/yyyy
 * - time: texto no formato HH:mm (opcional, pode ficar vazio)
 */
data class Event(
    val id: Long = 0,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String
)
