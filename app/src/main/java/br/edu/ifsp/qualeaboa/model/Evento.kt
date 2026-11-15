package br.edu.ifsp.qualeaboa.model

import java.io.Serializable

// Serializable para poder enviar o evento de uma tela para outra com putExtra
data class Evento(
    val id: Long,
    val nome: String,
    val descricao: String,
    val data: String,     // formato dd/MM/yyyy
    val horario: String,  // formato HH:mm (pode ficar vazio)
    val local: String,
    val foto: String      // Uri da imagem da galeria guardada como String
) : Serializable
