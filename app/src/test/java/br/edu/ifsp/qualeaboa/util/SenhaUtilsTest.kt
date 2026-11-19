package br.edu.ifsp.qualeaboa.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SenhaUtilsTest {

    @Test
    fun gerarHash_naoGuardaASenhaDigitada() {
        val hash = SenhaUtils.gerarHash("minhaSenha")

        assertNotEquals("minhaSenha", hash)
        assertEquals(64, hash.length) // SHA-256 tem 64 caracteres em hexadecimal
    }

    @Test
    fun gerarHash_mesmaSenhaGeraMesmoHash() {
        assertEquals(SenhaUtils.gerarHash("1234"), SenhaUtils.gerarHash("1234"))
    }

    @Test
    fun gerarHash_senhasDiferentesGeramHashesDiferentes() {
        assertNotEquals(SenhaUtils.gerarHash("1234"), SenhaUtils.gerarHash("4321"))
    }
}
