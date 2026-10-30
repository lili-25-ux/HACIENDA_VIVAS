package com.example.hacienda_vivas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Bovino(
    val id: Int = 0,
    val numero: String,
    val letra: String,
    val color: String,
    val raza: String,
    val observaciones: String,
    val lugar: String = "",
    val lote: String = "",
    val fechaNacimiento: String = "",
    val coordenadas: String = "",
)

data class Cultivo(
    val id: Int = 0,
    val codigo: String,
    val nombre: String,
    val hectareas: Double,
    val estado: String,
    val observaciones: String,
    val fechaNacimiento: String = "",
    val coordenadas: String = "",
)

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "RanchoTrebol.db"
        private const val DATABASE_VERSION = 4

        // Tabla Bovinos
        private const val TABLE_BOVINOS = "bovinos"
        private const val KEY_BOV_ID = "id"
        private const val KEY_BOV_NUMERO = "numero"
        private const val KEY_BOV_LETRA = "letra"
        private const val KEY_BOV_COLOR = "color"
        private const val KEY_BOV_RAZA = "raza"
        private const val KEY_BOV_OBSERVACIONES = "observaciones"
        private const val KEY_BOV_LUGAR = "lugar"
        private const val KEY_BOV_LOTE = "lote"
        private const val KEY_BOV_FECHA_NAC = "fecha_nacimiento"
        private const val KEY_BOV_COORDENADAS = "coordenadas"

        // Tabla Cultivos
        private const val TABLE_CULTIVOS = "cultivos"
        private const val KEY_CUL_ID = "id"
        private const val KEY_CUL_CODIGO = "codigo"
        private const val KEY_CUL_NOMBRE = "nombre"
        private const val KEY_CUL_HECTARES = "hectareas"
        private const val KEY_CUL_ESTADO = "estado"
        private const val KEY_CUL_OBSERVACIONES = "observaciones"
        private const val KEY_CUL_FECHA_NAC = "fecha_nacimiento"
        private const val KEY_CUL_COORDENADAS = "coordenadas"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createBovinosTable = ("CREATE TABLE " + TABLE_BOVINOS + " ("
                + KEY_BOV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_BOV_NUMERO + " TEXT, "
                + KEY_BOV_LETRA + " TEXT, "
                + KEY_BOV_COLOR + " TEXT, "
                + KEY_BOV_RAZA + " TEXT, "
                + KEY_BOV_OBSERVACIONES + " TEXT, "
                + KEY_BOV_LUGAR + " TEXT, "
                + KEY_BOV_LOTE + " TEXT, "
                + KEY_BOV_FECHA_NAC + " TEXT, "
                + KEY_BOV_COORDENADAS + " TEXT)")

        val createCultivosTable = ("CREATE TABLE " + TABLE_CULTIVOS + " ("
                + KEY_CUL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_CUL_CODIGO + " TEXT, "
                + KEY_CUL_NOMBRE + " TEXT, "
                + KEY_CUL_HECTARES + " REAL, "
                + KEY_CUL_ESTADO + " TEXT, "
                + KEY_CUL_OBSERVACIONES + " TEXT, "
                + KEY_CUL_FECHA_NAC + " TEXT, "
                + KEY_CUL_COORDENADAS + " TEXT)")

        db.execSQL(createBovinosTable)
        db.execSQL(createCultivosTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_BOVINOS ADD COLUMN $KEY_BOV_LUGAR TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_BOVINOS ADD COLUMN $KEY_BOV_LOTE TEXT DEFAULT ''")
            } catch (_: Exception) {}
        }
        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE $TABLE_BOVINOS ADD COLUMN fecha TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_CULTIVOS ADD COLUMN fecha TEXT DEFAULT ''")
            } catch (_: Exception) {}
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE $TABLE_BOVINOS ADD COLUMN $KEY_BOV_FECHA_NAC TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_BOVINOS ADD COLUMN $KEY_BOV_COORDENADAS TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_CULTIVOS ADD COLUMN $KEY_CUL_FECHA_NAC TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_CULTIVOS ADD COLUMN $KEY_CUL_COORDENADAS TEXT DEFAULT ''")
            } catch (_: Exception) {
                db.execSQL("DROP TABLE IF EXISTS $TABLE_BOVINOS")
                db.execSQL("DROP TABLE IF EXISTS $TABLE_CULTIVOS")
                onCreate(db)
            }
        }
    }

    // --- OPERACIONES BOVINOS ---

    fun guardarBovino(bovino: Bovino): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_BOV_NUMERO, bovino.numero)
            put(KEY_BOV_LETRA, bovino.letra)
            put(KEY_BOV_COLOR, bovino.color)
            put(KEY_BOV_RAZA, bovino.raza)
            put(KEY_BOV_OBSERVACIONES, bovino.observaciones)
            put(KEY_BOV_LUGAR, bovino.lugar)
            put(KEY_BOV_LOTE, bovino.lote)
            put(KEY_BOV_FECHA_NAC, bovino.fechaNacimiento)
            put(KEY_BOV_COORDENADAS, bovino.coordenadas)
        }
        val id = db.insert(TABLE_BOVINOS, null, values)
        db.close()
        return id
    }

    fun buscarBovino(letra: String, numero: String): Bovino? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BOVINOS,
            null,
            "$KEY_BOV_LETRA = ? AND $KEY_BOV_NUMERO = ?",
            arrayOf(letra, numero),
            null, null, null
        )

        var bovino: Bovino? = null
        if (cursor.moveToFirst()) {
            val lugarIdx = cursor.getColumnIndex(KEY_BOV_LUGAR)
            val loteIdx = cursor.getColumnIndex(KEY_BOV_LOTE)
            val fechaNacIdx = cursor.getColumnIndex(KEY_BOV_FECHA_NAC)
            val fechaOldIdx = cursor.getColumnIndex("fecha")
            val coordsIdx = cursor.getColumnIndex(KEY_BOV_COORDENADAS)

            val fechaVal = when {
                fechaNacIdx != -1 && !cursor.getString(fechaNacIdx).isNullOrEmpty() -> cursor.getString(fechaNacIdx)
                fechaOldIdx != -1 -> cursor.getString(fechaOldIdx) ?: ""
                else -> ""
            }

            bovino = Bovino(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOV_ID)),
                numero = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_NUMERO)),
                letra = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_LETRA)),
                color = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_COLOR)),
                raza = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_RAZA)),
                observaciones = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_OBSERVACIONES)),
                lugar = if (lugarIdx != -1) cursor.getString(lugarIdx) ?: "" else "",
                lote = if (loteIdx != -1) cursor.getString(loteIdx) ?: "" else "",
                fechaNacimiento = fechaVal,
                coordenadas = if (coordsIdx != -1) cursor.getString(coordsIdx) ?: "" else ""
            )
        }
        cursor.close()
        db.close()
        return bovino
    }

    fun modificarBovino(bovino: Bovino): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_BOV_NUMERO, bovino.numero)
            put(KEY_BOV_LETRA, bovino.letra)
            put(KEY_BOV_COLOR, bovino.color)
            put(KEY_BOV_RAZA, bovino.raza)
            put(KEY_BOV_OBSERVACIONES, bovino.observaciones)
            put(KEY_BOV_LUGAR, bovino.lugar)
            put(KEY_BOV_LOTE, bovino.lote)
            put(KEY_BOV_FECHA_NAC, bovino.fechaNacimiento)
            put(KEY_BOV_COORDENADAS, bovino.coordenadas)
        }
        val rows = db.update(TABLE_BOVINOS, values, "$KEY_BOV_ID = ?", arrayOf(bovino.id.toString()))
        db.close()
        return rows
    }

    fun eliminarBovino(id: Int): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_BOVINOS, "$KEY_BOV_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    fun obtenerTodosBovinos(): List<Bovino> {
        val lista = mutableListOf<Bovino>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_BOVINOS ORDER BY $KEY_BOV_ID DESC", null)
        if (cursor.moveToFirst()) {
            val lugarIdx = cursor.getColumnIndex(KEY_BOV_LUGAR)
            val loteIdx = cursor.getColumnIndex(KEY_BOV_LOTE)
            val fechaNacIdx = cursor.getColumnIndex(KEY_BOV_FECHA_NAC)
            val fechaOldIdx = cursor.getColumnIndex("fecha")
            val coordsIdx = cursor.getColumnIndex(KEY_BOV_COORDENADAS)
            do {
                val fechaVal = when {
                    fechaNacIdx != -1 && !cursor.getString(fechaNacIdx).isNullOrEmpty() -> cursor.getString(fechaNacIdx)
                    fechaOldIdx != -1 -> cursor.getString(fechaOldIdx) ?: ""
                    else -> ""
                }
                val bovino = Bovino(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BOV_ID)),
                    numero = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_NUMERO)),
                    letra = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_LETRA)),
                    color = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_COLOR)),
                    raza = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_RAZA)),
                    observaciones = cursor.getString(cursor.getColumnIndexOrThrow(KEY_BOV_OBSERVACIONES)),
                    lugar = if (lugarIdx != -1) cursor.getString(lugarIdx) ?: "" else "",
                    lote = if (loteIdx != -1) cursor.getString(loteIdx) ?: "" else "",
                    fechaNacimiento = fechaVal,
                    coordenadas = if (coordsIdx != -1) cursor.getString(coordsIdx) ?: "" else ""
                )
                lista.add(bovino)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }

    // --- OPERACIONES CULTIVOS ---

    fun guardarCultivo(cultivo: Cultivo): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_CUL_CODIGO, cultivo.codigo)
            put(KEY_CUL_NOMBRE, cultivo.nombre)
            put(KEY_CUL_HECTARES, cultivo.hectareas)
            put(KEY_CUL_ESTADO, cultivo.estado)
            put(KEY_CUL_OBSERVACIONES, cultivo.observaciones)
            put(KEY_CUL_FECHA_NAC, cultivo.fechaNacimiento)
            put(KEY_CUL_COORDENADAS, cultivo.coordenadas)
        }
        val id = db.insert(TABLE_CULTIVOS, null, values)
        db.close()
        return id
    }

    fun buscarCultivo(codigo: String): Cultivo? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CULTIVOS,
            null,
            "$KEY_CUL_CODIGO = ?",
            arrayOf(codigo),
            null, null, null
        )

        var cultivo: Cultivo? = null
        if (cursor.moveToFirst()) {
            val fechaNacIdx = cursor.getColumnIndex(KEY_CUL_FECHA_NAC)
            val fechaOldIdx = cursor.getColumnIndex("fecha")
            val coordsIdx = cursor.getColumnIndex(KEY_CUL_COORDENADAS)

            val fechaVal = when {
                fechaNacIdx != -1 && !cursor.getString(fechaNacIdx).isNullOrEmpty() -> cursor.getString(fechaNacIdx)
                fechaOldIdx != -1 -> cursor.getString(fechaOldIdx) ?: ""
                else -> ""
            }

            cultivo = Cultivo(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CUL_ID)),
                codigo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CUL_CODIGO)),
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CUL_NOMBRE)),
                hectareas = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_CUL_HECTARES)),
                estado = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CUL_ESTADO)),
                observaciones = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CUL_OBSERVACIONES)),
                fechaNacimiento = fechaVal,
                coordenadas = if (coordsIdx != -1) cursor.getString(coordsIdx) ?: "" else ""
            )
        }
        cursor.close()
        db.close()
        return cultivo
    }

    fun modificarCultivo(cultivo: Cultivo): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_CUL_CODIGO, cultivo.codigo)
            put(KEY_CUL_NOMBRE, cultivo.nombre)
            put(KEY_CUL_HECTARES, cultivo.hectareas)
            put(KEY_CUL_ESTADO, cultivo.estado)
            put(KEY_CUL_OBSERVACIONES, cultivo.observaciones)
            put(KEY_CUL_FECHA_NAC, cultivo.fechaNacimiento)
            put(KEY_CUL_COORDENADAS, cultivo.coordenadas)
        }
        val rows = db.update(TABLE_CULTIVOS, values, "$KEY_CUL_ID = ?", arrayOf(cultivo.id.toString()))
        db.close()
        return rows
    }

    fun eliminarCultivo(id: Int): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_CULTIVOS, "$KEY_CUL_ID = ?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    fun obtenerTodosCultivos(): List<Cultivo> {
        val lista = mutableListOf<Cultivo>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CULTIVOS ORDER BY $KEY_CUL_ID DESC", null)
        if (cursor.moveToFirst()) {
            val fechaNacIdx = cursor.getColumnIndex(KEY_CUL_FECHA_NAC)
            val fechaOldIdx = cursor.getColumnIndex("fecha")
            val coordsIdx = cursor.getColumnIndex(KEY_CUL_COORDENADAS)
            do {
                val fechaVal = when {
                    fechaNacIdx != -1 && !cursor.getString(fechaNacIdx).isNullOrEmpty() -> cursor.getString(fechaNacIdx)
                    fechaOldIdx != -1 -> cursor.getString(fechaOldIdx) ?: ""
                    else -> ""
                }
                val cultivo = Cultivo(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CUL_ID)),
                    codigo = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CUL_CODIGO)),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CUL_NOMBRE)),
                    hectareas = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_CUL_HECTARES)),
                    estado = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CUL_ESTADO)),
                    observaciones = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CUL_OBSERVACIONES)),
                    fechaNacimiento = fechaVal,
                    coordenadas = if (coordsIdx != -1) cursor.getString(coordsIdx) ?: "" else ""
                )
                lista.add(cultivo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return lista
    }
}
