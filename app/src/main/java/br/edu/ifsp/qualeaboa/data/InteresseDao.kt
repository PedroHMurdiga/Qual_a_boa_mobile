package br.edu.ifsp.qualeaboa.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import br.edu.ifsp.qualeaboa.model.Interesse

@Dao
interface InteresseDao {

    @Insert
    suspend fun inserir(interesse: Interesse)

    // Todos os "Tô dentro!" de um usuário
    @Query("SELECT * FROM interesse WHERE usuarioId = :usuarioId")
    suspend fun listarPorUsuario(usuarioId: Int): List<Interesse>

    // Todos os "Tô dentro!" de um evento
    @Query("SELECT * FROM interesse WHERE eventoId = :eventoId")
    suspend fun listarPorEvento(eventoId: Int): List<Interesse>

    // "Não vou mais": apaga o interesse do usuário naquele evento
    @Query("DELETE FROM interesse WHERE usuarioId = :usuarioId AND eventoId = :eventoId")
    suspend fun deletar(usuarioId: Int, eventoId: Int)

    // Usado quando um evento é apagado
    @Query("DELETE FROM interesse WHERE eventoId = :eventoId")
    suspend fun deletarDoEvento(eventoId: Int)
}
