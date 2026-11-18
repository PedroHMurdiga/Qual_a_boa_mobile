package br.edu.ifsp.qualeaboa.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import br.edu.ifsp.qualeaboa.model.Evento

@Database(entities = [Evento::class], version = 1)
abstract class QualEABoaDatabase : RoomDatabase() {

    abstract fun eventoDao(): EventoDao

    companion object {
        @Volatile
        private var INSTANCE: QualEABoaDatabase? = null

        // Singleton: cria o banco uma vez só e depois devolve sempre o mesmo
        fun getInstance(context: Context): QualEABoaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QualEABoaDatabase::class.java,
                    "qualeaboa_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
