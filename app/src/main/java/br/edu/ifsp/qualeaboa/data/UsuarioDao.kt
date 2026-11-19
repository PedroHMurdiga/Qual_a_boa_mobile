package br.edu.ifsp.qualeaboa.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import br.edu.ifsp.qualeaboa.model.Usuario

@Dao
interface UsuarioDao {

    @Insert
    suspend fun inserir(usuario: Usuario)

    // Retorna null se não existir ninguém com esse e-mail
    @Query("SELECT * FROM usuario WHERE email = :email")
    suspend fun buscarPorEmail(email: String): Usuario?

    // Busca os usuários cujos ids estão na lista (usado para mostrar quem vai ao evento)
    @Query("SELECT * FROM usuario WHERE id IN (:ids) ORDER BY nome ASC")
    suspend fun buscarPorIds(ids: List<Int>): List<Usuario>
}
