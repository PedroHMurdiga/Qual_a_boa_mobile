package br.edu.ifsp.qualeaboa.util

import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Funções para formatar e validar a data (dd/MM/yyyy) e o horário (HH:mm) dos eventos. */
object EventDateUtils {

    private const val DATE_PATTERN = "dd/MM/yyyy"
    private const val TIME_PATTERN = "HH:mm"

    /** [month] começa em 0 (janeiro), como no DatePickerDialog. */
    fun formatDate(year: Int, month: Int, day: Int): String =
        String.format(Locale.ROOT, "%02d/%02d/%04d", day, month + 1, year)

    fun formatTime(hour: Int, minute: Int): String =
        String.format(Locale.ROOT, "%02d:%02d", hour, minute)

    fun isValidDate(date: String): Boolean = parse(date, DATE_PATTERN) != null

    /** O horário é opcional: vazio também é válido. */
    fun isValidTime(time: String): Boolean = time.isBlank() || parse(time, TIME_PATTERN) != null

    /** Texto mostrado na tela, ex.: "25/10/2026 • 19:30". */
    fun displayDateTime(date: String, time: String): String =
        if (time.isBlank()) date else "$date • $time"

    /** Converte data + horário em milissegundos. Sem horário, usa meia-noite. Retorna null se for inválido. */
    fun toMillis(date: String, time: String): Long? {
        val parsed = if (time.isBlank()) {
            parse(date, DATE_PATTERN)
        } else {
            parse("$date $time", "$DATE_PATTERN $TIME_PATTERN")
        }
        return parsed?.time
    }

    private fun parse(text: String, pattern: String): Date? {
        val format = SimpleDateFormat(pattern, Locale.ROOT).apply { isLenient = false }
        val position = ParsePosition(0)
        val date = format.parse(text.trim(), position)
        // Só aceita se o texto inteiro foi lido (evita aceitar "10/10/2026abc").
        return if (date != null && position.index == text.trim().length) date else null
    }
}
