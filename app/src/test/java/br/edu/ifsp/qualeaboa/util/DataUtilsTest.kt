package br.edu.ifsp.qualeaboa.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DataUtilsTest {

    @Test
    fun formatarData_colocaZerosEAjustaOMes() {
        // No DatePickerDialog o mês começa em 0 (janeiro)
        assertEquals("05/01/2026", DataUtils.formatarData(2026, 0, 5))
    }

    @Test
    fun formatarHora_colocaZeros() {
        assertEquals("09:05", DataUtils.formatarHora(9, 5))
    }

    @Test
    fun converterParaMillis_somaOHorarioNaData() {
        val semHorario = DataUtils.converterParaMillis("25/12/2026", "")
        val comHorario = DataUtils.converterParaMillis("25/12/2026", "19:30")

        assertNotNull(semHorario)
        assertNotNull(comHorario)
        // 19h30 = (19 * 60 + 30) minutos depois da meia-noite
        assertEquals((19 * 60 + 30) * 60 * 1000L, comHorario!! - semHorario!!)
    }

    @Test
    fun converterParaMillis_retornaNullParaDataInvalida() {
        assertNull(DataUtils.converterParaMillis("31/04/2026", ""))
        assertNull(DataUtils.converterParaMillis("amanhã", ""))
    }
}
