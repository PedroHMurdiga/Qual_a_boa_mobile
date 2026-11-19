package br.edu.ifsp.qualeaboa.util

import java.security.MessageDigest

// Transforma a senha em um "hash" (SHA-256) para não salvar a senha de verdade no banco.
// Serve para um trabalho acadêmico; um app real precisaria de mais segurança (servidor, salt, etc.).
object SenhaUtils {

    fun gerarHash(senha: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(senha.toByteArray())
        var hash = ""
        for (byte in bytes) {
            hash += String.format("%02x", byte)
        }
        return hash
    }
}
