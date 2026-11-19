package br.edu.ifsp.qualeaboa.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Quando o usuário toca em "Tô dentro!", salvamos um Interesse ligando ele ao evento
@Entity(tableName = "interesse")
data class Interesse(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val eventoId: Int
)
