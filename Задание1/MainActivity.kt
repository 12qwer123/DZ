package com.example.myapplication2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication2.ui.theme.MyApplication2Theme

import android.graphics.Color
import java.util.UUID
import org.json.JSONObject
import org.json.JSONArray
import java.io.File



// класс Note
data class Note(
    val uid: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val importance: Importance = Importance.NORMAL,
    val color: Int = Color.WHITE

) {
    // важность заметки
    enum class Importance {
        NO_MATTER,
        NORMAL,
        MATTER
    }
    companion object {
        // для разбора JSON
        fun parse(json: JSONObject): Note? {
            return try {
                Note(
                    uid = json.optString("uid", UUID.randomUUID().toString()),
                    title = json.getString("title"),
                    content = json.getString("content"),
                    importance = json.optString("importance").let {
                        when (it) {
                            "NO_MATTER" -> Importance.NO_MATTER
                            "MATTER" -> Importance.MATTER
                            else -> Importance.NORMAL
                        }
                    },
                    color = json.optInt("color", Color.WHITE)

                )
            } catch (e: Exception) {
                null
            }
        }
    }

    val json: JSONObject
        get() = JSONObject().apply {
            put("uid", uid)
            put("title", title)
            put("content", content)

            // Сохраняем важность только если она не обычная
            if (importance != Importance.NORMAL) {
                put("importance", importance.name)
            }

            // Сохраняем цвет только если он не белый
            if (color != Color.WHITE) {
                put("color", color)
            }
        }
}

// Класс FileNotebook
class FileNotebook {
    private val _notes = mutableListOf<Note>()
    val notes: List<Note> get() = _notes.toList()

    // Добавление заметки
    fun addNote(note: Note) {
        _notes.add(note)
    }

    // Удаление заметки по uid
    fun deleteNote(uid: String): Boolean {
        return _notes.removeIf { it.uid == uid }
    }

    // Сохранение в файл
    fun saveNote(file: File): Boolean {
        return try {
            val jsonA = JSONArray()
            _notes.forEach { note ->
                jsonA.put(note.json)
            }
            file.writeText(jsonA.toString())
            true
        } catch (e: Exception) {
            false
        }
    }

    // Загрузка из файла
    fun loadNote(file: File): Boolean {
        return try {
            val readF = file.readText()
            val jsonA = JSONArray(readF)

            _notes.clear()

            for (i in 0 until jsonA.length()) {
                val obj = jsonA.getJSONObject(i)
                Note.parse(obj)?.let { note ->
                    _notes.add(note)
                }
            }

            true
        } catch (e: Exception) {
            false
        }
    }
}
