package br.edu.ifsp.qualeaboa.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.data.QualEABoaDatabase
import br.edu.ifsp.qualeaboa.databinding.ActivityLoginBinding
import br.edu.ifsp.qualeaboa.util.SenhaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Primeira tela do app
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: QualEABoaDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = QualEABoaDatabase.getInstance(this)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnEntrar.setOnClickListener { entrar() }
        binding.btnCriarConta.setOnClickListener {
            val intent = Intent(this, CadastroUsuarioActivity::class.java)
            startActivity(intent)
        }
    }

    private fun entrar() {
        val email = binding.edtEmail.text.toString().trim().lowercase()
        val senha = binding.edtSenha.text.toString()

        if (email.isBlank() || senha.isBlank()) {
            Toast.makeText(this, R.string.error_fill_login, Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val usuario = db.usuarioDao().buscarPorEmail(email)

            withContext(Dispatchers.Main) {
                // Compara o hash da senha digitada com o hash salvo no banco
                if (usuario != null && usuario.senha == SenhaUtils.gerarHash(senha)) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.putExtra(MainActivity.EXTRA_USUARIO, usuario)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, R.string.error_wrong_credentials, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
