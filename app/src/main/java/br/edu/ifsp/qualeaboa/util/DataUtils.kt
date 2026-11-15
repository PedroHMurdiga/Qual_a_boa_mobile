package br.edu.ifsp.qualeaboa.util

import java.text.SimpleDateFormat
import java.util.Locale

// Funções para trabalhar com a data e o horário dos eventos
object DataUtils {

    // O DatePickerDialog devolve o mês começando em 0 (janeiro = 0)
    fun formatarData(ano: Int, mes: Int, dia: Int): String {
        return String.format(Locale.ROOT, "%02d/%02d/%04d", dia, mes + 1, ano)
    }

    fun formatarHora(hora: Int, minuto: Int): String {
        return String.format(Locale.ROOT, "%02d:%02d", hora, minuto)
    }

    // Converte "25/12/2026" e "19:30" em milissegundos (usado na agenda do celular).
    // Retorna null se a data for inválida.
    fun converterParaMillis(data: String, horario: String): Long? {
        val texto: String
        val formato: SimpleDateFormat

        if (horario.isEmpty()) {
            texto = data
            formato = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
        } else {
            texto = "$data $horario"
            formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT)
        }
        formato.isLenient = false

        return try {
            formato.parse(texto)?.time
        } catch (e: Exception) {
            null
        }
    }
}
