package br.edu.ifsp.qualeaboa.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import br.edu.ifsp.qualeaboa.R
import br.edu.ifsp.qualeaboa.data.AppDatabase
import br.edu.ifsp.qualeaboa.databinding.ActivityCadastroEventoBinding
import br.edu.ifsp.qualeaboa.model.Event
import br.edu.ifsp.qualeaboa.util.EventDateUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Cadastro de eventos (RF03).
 * - Sem EXTRA_EVENT_ID: cria um evento novo.
 * - Com EXTRA_EVENT_ID: edita o evento e permite excluí-lo.
 */
class CadastroEventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroEventoBinding
    private val eventDao by lazy { AppDatabase.getInstance(this).eventDao() }

    private var editingEvent: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.title_new_event)

        setupListeners()

        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        if (eventId != -1L) {
            loadEventForEdit(eventId, fillForm = savedInstanceState == null)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupListeners() {
        binding.edtDate.setOnClickListener { showDatePicker() }
        binding.edtTime.setOnClickListener { showTimePicker() }
        binding.btnSave.setOnClickListener { saveEvent() }
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    private fun loadEventForEdit(eventId: Long, fillForm: Boolean) {
        lifecycleScope.launch {
            val event = eventDao.getById(eventId)
            if (event == null) {
                finish()
                return@launch
            }
            editingEvent = event
            supportActionBar?.setTitle(R.string.title_edit_event)
            binding.txtSubtitle.isVisible = false
            binding.btnDelete.isVisible = true

            if (fillForm) {
                binding.edtTitle.setText(event.title)
                binding.edtDescription.setText(event.description)
                binding.edtDate.setText(event.date)
                binding.edtTime.setText(event.time)
                binding.edtLocation.setText(event.location)
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        // Se já houver uma data, o seletor abre nela.
        EventDateUtils.toMillis(binding.edtDate.text.toString(), "")?.let {
            calendar.timeInMillis = it
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                binding.edtDate.setText(EventDateUtils.formatDate(year, month, day))
                binding.tilDate.error = null
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val currentTime = binding.edtTime.text.toString()
        val hour = currentTime.substringBefore(":").toIntOrNull() ?: DEFAULT_HOUR
        val minute = currentTime.substringAfter(":", "").toIntOrNull() ?: 0

        val dialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                binding.edtTime.setText(EventDateUtils.formatTime(selectedHour, selectedMinute))
            },
            hour,
            minute,
            DateFormat.is24HourFormat(this)
        )
        // O horário é opcional: este botão limpa o campo.
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.action_no_time)) { _, _ ->
            binding.edtTime.setText("")
        }
        dialog.show()
    }

    private fun saveEvent() {
        val title = binding.edtTitle.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        val date = binding.edtDate.text.toString().trim()
        val time = binding.edtTime.text.toString().trim()
        val location = binding.edtLocation.text.toString().trim()

        if (!isFormValid(title, date, time, location)) return

        binding.btnSave.isEnabled = false // evita salvar duas vezes com toque duplo
        lifecycleScope.launch {
            val current = editingEvent
            if (current == null) {
                eventDao.insert(
                    Event(
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        location = location
                    )
                )
            } else {
                eventDao.update(
                    current.copy(
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        location = location
                    )
                )
            }
            Toast.makeText(this@CadastroEventoActivity, R.string.message_event_saved, Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun isFormValid(title: String, date: String, time: String, location: String): Boolean {
        val required = getString(R.string.error_required)

        binding.tilTitle.error = if (title.isBlank()) required else null
        binding.tilLocation.error = if (location.isBlank()) required else null
        binding.tilDate.error = when {
            date.isBlank() -> required
            !EventDateUtils.isValidDate(date) -> getString(R.string.error_invalid_date)
            else -> null
        }
        binding.tilTime.error =
            if (EventDateUtils.isValidTime(time)) null else getString(R.string.error_invalid_time)

        return binding.tilTitle.error == null &&
            binding.tilLocation.error == null &&
            binding.tilDate.error == null &&
            binding.tilTime.error == null
    }

    private fun confirmDelete() {
        val event = editingEvent ?: return

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setMessage(getString(R.string.dialog_delete_message, event.title))
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.confirm_delete) { _, _ -> deleteEvent(event) }
            .show()
    }

    private fun deleteEvent(event: Event) {
        lifecycleScope.launch {
            eventDao.delete(event)
            Toast.makeText(this@CadastroEventoActivity, R.string.message_event_deleted, Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        private const val DEFAULT_HOUR = 19
    }
}
