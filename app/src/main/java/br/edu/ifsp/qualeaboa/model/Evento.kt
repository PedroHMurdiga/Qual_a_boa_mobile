package br.edu.ifsp.qualeaboa.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// @Entity: o Room transforma esta classe na tabela "evento"
@Entity(tableName = "evento")
data class Evento(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val descricao: String,
    val data: String,     // formato dd/MM/yyyy
    val horario: String,  // formato HH:mm (pode ficar vazio)
    val local: String,
    val foto: String,     // Uri da imagem da galeria guardada como String
    val criadorId: Int    // id do usuário que criou o evento
) : Serializable
