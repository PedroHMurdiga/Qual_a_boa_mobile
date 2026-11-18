package br.edu.ifsp.qualeaboa.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.edu.ifsp.qualeaboa.model.Evento

@Dao
interface EventoDao {

    @Insert
    suspend fun inserir(evento: Evento)

    // Os mais novos primeiro
    @Query("SELECT * FROM evento ORDER BY id DESC")
    suspend fun listarTodos(): List<Evento>

    @Delete
    suspend fun deletar(evento: Evento)

    @Update
    suspend fun atualizar(evento: Evento)
}
