package br.edu.ifsp.qualeaboa.ui

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.data.QualEABoaDatabase
import br.edu.ifsp.qualeaboa.databinding.ActivityCadastroUsuarioBinding
import br.edu.ifsp.qualeaboa.model.Usuario
import br.edu.ifsp.qualeaboa.util.SenhaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CadastroUsuarioActivity : AppCompatActivity() {

    companion object {
        const val TAMANHO_MINIMO_SENHA = 4
    }

    private lateinit var binding: ActivityCadastroUsuarioBinding
    private lateinit var db: QualEABoaDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QualEABoaDatabase.getInstance(this)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCadastrar.setOnClickListener { cadastrarUsuario() }
        binding.btnVoltar.setOnClickListener { finish() }
    }

    private fun cadastrarUsuario() {
        val nome = binding.edtNome.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim().lowercase()
        val senha = binding.edtSenha.text.toString()

        if (!validarCampos(nome, email, senha)) {
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val usuarioExistente = db.usuarioDao().buscarPorEmail(email)

            if (usuarioExistente == null) {
                val novoUsuario = Usuario(
                    nome = nome,
                    email = email,
                    senha = SenhaUtils.gerarHash(senha)
                )
                db.usuarioDao().inserir(novoUsuario)
            }

            withContext(Dispatchers.Main) {
                if (usuarioExistente != null) {
                    Toast.makeText(this@CadastroUsuarioActivity, R.string.error_email_in_use, Toast.LENGTH_SHORT).show()
                } else {
                    // Volta para o login, onde a pessoa entra com a conta nova
                    Toast.makeText(this@CadastroUsuarioActivity, R.string.message_account_created, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    // Mostra um Toast com o problema e retorna false se algum campo estiver errado
    private fun validarCampos(nome: String, email: String, senha: String): Boolean {
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            Toast.makeText(this, R.string.error_fill_register, Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show()
            return false
        }

        if (senha.length < TAMANHO_MINIMO_SENHA) {
            val mensagem = getString(R.string.error_short_password, TAMANHO_MINIMO_SENHA)
            Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
