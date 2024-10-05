package com.example.notdefterispecial

import android.app.AlertDialog
import android.database.Cursor
import android.icu.text.DateFormat
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: NoteDatabaseHelper
    private lateinit var notesList: MutableList<String>
    private lateinit var listView: ListView
    private lateinit var addButton: Button  // Not eklemek için buton

    companion object {
        const val COLUMN_ID = "id" // Veritabanınızdaki gerçek isim
        const val COLUMN_NOTE = "note" // Veritabanınızdaki gerçek isim
        const val COLUMN_DATE = "date" // Veritabanınızdaki gerçek isim
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = NoteDatabaseHelper(this)
        notesList = mutableListOf()
        listView = findViewById(R.id.listView)
        addButton = findViewById(R.id.addButton) // Butonun referansını al

        loadNotes()

        // Not ekleme butonuna tıklama olayı
        addButton.setOnClickListener {
            showAddNoteDialog()
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val noteToEdit = notesList[position]
            val noteParts = noteToEdit.split(" - ")
            val noteId = noteParts.last().toInt()

            AlertDialog.Builder(this)
                .setTitle("Not Seçenekleri")
                .setMessage("Düzenlemek veya silmek için seçenek seçin.")
                .setPositiveButton("Düzenle") { dialog, which ->
                    showEditNoteDialog(noteId, noteParts[0])
                }
                .setNegativeButton("Sİl") { dialog, which ->
                    dbHelper.deleteNote(noteId)
                    loadNotes()
                }
                .setNeutralButton("İptal", null)
                .show()
        }
    }

    private fun loadNotes() {
        notesList.clear()
        val cursor: Cursor = dbHelper.getAllNotes()
        if (cursor.moveToFirst()) {
            do {
                val noteId = cursor.getInt(cursor.run { getColumnIndex(COLUMN_ID) })
                val note = cursor.getString(cursor.run { getColumnIndex(COLUMN_NOTE) })
                val dateMilis = cursor.getLong(cursor.run { getColumnIndex(COLUMN_DATE) })
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("Europe/Istanbul")
                val date = dateFormat.format(Date(dateMilis))
                //val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

                notesList.add("$note - $date - $noteId")

            } while (cursor.moveToNext())
        }
        cursor.close()

        // Notları ters çevir
        notesList.reverse()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, notesList)
        listView.adapter = adapter
    }

    private fun showAddNoteDialog() {
        val editText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Not Ekle")
            .setMessage("Notunuzu girin.")
            .setView(editText)
            .setPositiveButton("Ekle") { dialog, which ->
                val newNote = editText.text.toString()
                val date = System.currentTimeMillis().toString()
                // Geçici tarih, güncelleyebilirsin


                dbHelper.addNote(newNote, date) // Veritabanına yeni not ekle
                loadNotes() // Notları yenile
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun showEditNoteDialog(noteId: Int, currentNote: String) {
        val editText = EditText(this)
        editText.setText(currentNote)

        AlertDialog.Builder(this)
            .setTitle("Notu Düzenle")
            .setMessage("Notunuzu düzenleyin.")
            .setView(editText)
            .setPositiveButton("Kaydet") { dialog, which ->
                val newNote = editText.text.toString()
                dbHelper.updateNote(noteId, newNote)
                loadNotes()
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}
