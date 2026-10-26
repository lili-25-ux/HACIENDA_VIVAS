package com.example.hacienda_vivas

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var firebaseHelper: FirebaseHelper

    // UI Controls Mode
    private lateinit var rgCategoria: RadioGroup
    private lateinit var cardFormBovinos: MaterialCardView
    private lateinit var cardFormCultivos: MaterialCardView

    // Bovino Fields
    private lateinit var etLetra: TextInputEditText
    private lateinit var etNumero: TextInputEditText
    private lateinit var etColor: TextInputEditText
    private lateinit var etRaza: TextInputEditText
    private lateinit var etObsBovino: TextInputEditText

    // Cultivo Fields
    private lateinit var etCodigo: TextInputEditText
    private lateinit var etNombreCultivo: TextInputEditText
    private lateinit var etHectareas: TextInputEditText
    private lateinit var etEstado: TextInputEditText
    private lateinit var etObsCultivo: TextInputEditText

    // Buttons
    private lateinit var btnGuardar: Button
    private lateinit var btnBuscar: Button
    private lateinit var btnModificar: Button
    private lateinit var btnEliminar: Button
    private lateinit var btnLimpiar: Button

    // List Output
    private lateinit var tvTituloLista: TextView
    private lateinit var tvListaRegistros: TextView

    // ID tracking for current item selected/searched
    private var currentBovinoId: Int? = null
    private var currentCultivoId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbHelper = DatabaseHelper(this)
        firebaseHelper = FirebaseHelper()

        initViews()
        setupListeners()
        actualizarLista()
    }

    private fun initViews() {
        rgCategoria = findViewById(R.id.rgCategoria)
        cardFormBovinos = findViewById(R.id.cardFormBovinos)
        cardFormCultivos = findViewById(R.id.cardFormCultivos)

        // Bovinos
        etLetra = findViewById(R.id.etLetra)
        etNumero = findViewById(R.id.etNumero)
        etColor = findViewById(R.id.etColor)
        etRaza = findViewById(R.id.etRaza)
        etObsBovino = findViewById(R.id.etObsBovino)

        // Cultivos
        etCodigo = findViewById(R.id.etCodigo)
        etNombreCultivo = findViewById(R.id.etNombreCultivo)
        etHectareas = findViewById(R.id.etHectareas)
        etEstado = findViewById(R.id.etEstado)
        etObsCultivo = findViewById(R.id.etObsCultivo)

        // Buttons
        btnGuardar = findViewById(R.id.btnGuardar)
        btnBuscar = findViewById(R.id.btnBuscar)
        btnModificar = findViewById(R.id.btnModificar)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnLimpiar = findViewById(R.id.btnLimpiar)

        // List
        tvTituloLista = findViewById(R.id.tvTituloLista)
        tvListaRegistros = findViewById(R.id.tvListaRegistros)
    }

    private fun setupListeners() {
        rgCategoria.setOnCheckedChangeListener { _, checkedId ->
            limpiarCampos()
            if (checkedId == R.id.rbBovinos) {
                cardFormBovinos.visibility = View.VISIBLE
                cardFormCultivos.visibility = View.GONE
                tvTituloLista.text = getString(R.string.title_lista_bovinos)
            } else {
                cardFormBovinos.visibility = View.GONE
                cardFormCultivos.visibility = View.VISIBLE
                tvTituloLista.text = getString(R.string.title_lista_cultivos)
            }
            actualizarLista()
        }

        btnGuardar.setOnClickListener { guardar() }
        btnBuscar.setOnClickListener { buscar() }
        btnModificar.setOnClickListener { modificar() }
        btnEliminar.setOnClickListener { eliminar() }
        btnLimpiar.setOnClickListener {
            limpiarCampos()
            Toast.makeText(this, "Campos limpiados", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isModoBovinos(): Boolean {
        return rgCategoria.checkedRadioButtonId == R.id.rbBovinos
    }

    // --- OPERACIONES CRUD (Local + Firebase) ---

    private fun guardar() {
        if (isModoBovinos()) {
            val letra = etLetra.text.toString().trim().uppercase()
            val numero = etNumero.text.toString().trim()
            val color = etColor.text.toString().trim()
            val raza = etRaza.text.toString().trim()
            val obs = etObsBovino.text.toString().trim()

            if (letra.isEmpty() || numero.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese la Letra y el Número del bovino", Toast.LENGTH_SHORT).show()
                return
            }

            // Verificar si ya existe en local
            val existente = dbHelper.buscarBovino(letra, numero)
            if (existente != null) {
                Toast.makeText(this, "Ya existe un bovino registrado con Letra $letra y Número $numero", Toast.LENGTH_LONG).show()
                return
            }

            val bovino = Bovino(
                numero = numero,
                letra = letra,
                color = color,
                raza = raza,
                observaciones = obs,
            )

            val id = dbHelper.guardarBovino(bovino)
            if (id > 0) {
                val bovinoConId = bovino.copy(id = id.toInt())
                // Guardar también en Firebase Firestore
                firebaseHelper.guardarBovino(bovinoConId) { firebaseSuccess ->
                    val msg = if (firebaseSuccess) {
                        "🐄 Bovino registrado en Local y Firebase 🔥 (ID: $id)"
                    } else {
                        "🐄 Bovino registrado en Local (ID: $id)"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
                limpiarCampos()
                actualizarLista()
            } else {
                Toast.makeText(this, "Error al guardar el bovino", Toast.LENGTH_SHORT).show()
            }

        } else {
            val codigo = etCodigo.text.toString().trim().uppercase()
            val nombre = etNombreCultivo.text.toString().trim()
            val hectareasStr = etHectareas.text.toString().trim()
            val estado = etEstado.text.toString().trim()
            val obs = etObsCultivo.text.toString().trim()

            if (codigo.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese el Código y Nombre del cultivo", Toast.LENGTH_SHORT).show()
                return
            }

            val existente = dbHelper.buscarCultivo(codigo)
            if (existente != null) {
                Toast.makeText(this, "Ya existe un cultivo con el Código $codigo", Toast.LENGTH_LONG).show()
                return
            }

            val hectareas = hectareasStr.toDoubleOrNull() ?: 0.0
            val cultivo = Cultivo(
                codigo = codigo,
                nombre = nombre,
                hectareas = hectareas,
                estado = estado,
                observaciones = obs,
            )

            val id = dbHelper.guardarCultivo(cultivo)
            if (id > 0) {
                val cultivoConId = cultivo.copy(id = id.toInt())
                // Guardar también en Firebase Firestore
                firebaseHelper.guardarCultivo(cultivoConId) { firebaseSuccess ->
                    val msg = if (firebaseSuccess) {
                        "🌱 Cultivo registrado en Local y Firebase 🔥 (ID: $id)"
                    } else {
                        "🌱 Cultivo registrado en Local (ID: $id)"
                    }
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
                limpiarCampos()
                actualizarLista()
            } else {
                Toast.makeText(this, "Error al guardar el cultivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buscar() {
        if (isModoBovinos()) {
            val letra = etLetra.text.toString().trim().uppercase()
            val numero = etNumero.text.toString().trim()

            if (letra.isEmpty() || numero.isEmpty()) {
                Toast.makeText(this, "Ingrese la Letra y Número para buscar", Toast.LENGTH_SHORT).show()
                return
            }

            val bovino = dbHelper.buscarBovino(letra, numero)
            if (bovino != null) {
                currentBovinoId = bovino.id
                etLetra.setText(bovino.letra)
                etNumero.setText(bovino.numero)
                etColor.setText(bovino.color)
                etRaza.setText(bovino.raza)
                etObsBovino.setText(bovino.observaciones)
                Toast.makeText(this, "🐄 Bovino encontrado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se encontró el bovino $letra-$numero", Toast.LENGTH_SHORT).show()
            }

        } else {
            val codigo = etCodigo.text.toString().trim().uppercase()

            if (codigo.isEmpty()) {
                Toast.makeText(this, "Ingrese el Código del cultivo para buscar", Toast.LENGTH_SHORT).show()
                return
            }

            val cultivo = dbHelper.buscarCultivo(codigo)
            if (cultivo != null) {
                currentCultivoId = cultivo.id
                etCodigo.setText(cultivo.codigo)
                etNombreCultivo.setText(cultivo.nombre)
                etHectareas.setText(cultivo.hectareas.toString())
                etEstado.setText(cultivo.estado)
                etObsCultivo.setText(cultivo.observaciones)
                Toast.makeText(this, "🌱 Cultivo encontrado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se encontró el cultivo con código $codigo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun modificar() {
        if (isModoBovinos()) {
            val letra = etLetra.text.toString().trim().uppercase()
            val numero = etNumero.text.toString().trim()

            var idToUpdate = currentBovinoId
            if (idToUpdate == null) {
                val encontrado = dbHelper.buscarBovino(letra, numero)
                idToUpdate = encontrado?.id
            }

            if (idToUpdate == null) {
                Toast.makeText(this, "Primero busque o ingrese un bovino existente para modificar", Toast.LENGTH_SHORT).show()
                return
            }

            val color = etColor.text.toString().trim()
            val raza = etRaza.text.toString().trim()
            val obs = etObsBovino.text.toString().trim()

            val bovinoActualizado = Bovino(
                id = idToUpdate,
                numero = numero,
                letra = letra,
                color = color,
                raza = raza,
                observaciones = obs,
            )

            val filasAfec = dbHelper.modificarBovino(bovinoActualizado)
            if (filasAfec > 0) {
                firebaseHelper.guardarBovino(bovinoActualizado) { }
                Toast.makeText(this, "🐄 Bovino modificado correctamente (Sincronizado con Firebase 🔥)", Toast.LENGTH_SHORT).show()
                limpiarCampos()
                actualizarLista()
            } else {
                Toast.makeText(this, "No se pudo modificar el bovino", Toast.LENGTH_SHORT).show()
            }

        } else {
            val codigo = etCodigo.text.toString().trim().uppercase()

            var idToUpdate = currentCultivoId
            if (idToUpdate == null) {
                val encontrado = dbHelper.buscarCultivo(codigo)
                idToUpdate = encontrado?.id
            }

            if (idToUpdate == null) {
                Toast.makeText(this, "Primero busque o ingrese un cultivo existente para modificar", Toast.LENGTH_SHORT).show()
                return
            }

            val nombre = etNombreCultivo.text.toString().trim()
            val hectareasStr = etHectareas.text.toString().trim()
            val estado = etEstado.text.toString().trim()
            val obs = etObsCultivo.text.toString().trim()
            val hectareas = hectareasStr.toDoubleOrNull() ?: 0.0

            val cultivoActualizado = Cultivo(
                id = idToUpdate,
                codigo = codigo,
                nombre = nombre,
                hectareas = hectareas,
                estado = estado,
                observaciones = obs,
            )

            val filasAfec = dbHelper.modificarCultivo(cultivoActualizado)
            if (filasAfec > 0) {
                firebaseHelper.guardarCultivo(cultivoActualizado) { }
                Toast.makeText(this, "🌱 Cultivo modificado correctamente (Sincronizado con Firebase 🔥)", Toast.LENGTH_SHORT).show()
                limpiarCampos()
                actualizarLista()
            } else {
                Toast.makeText(this, "No se pudo modificar el cultivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun eliminar() {
        if (isModoBovinos()) {
            val letra = etLetra.text.toString().trim().uppercase()
            val numero = etNumero.text.toString().trim()

            var idToDelete = currentBovinoId
            if (idToDelete == null && letra.isNotEmpty() && numero.isNotEmpty()) {
                val encontrado = dbHelper.buscarBovino(letra, numero)
                idToDelete = encontrado?.id
            }

            if (idToDelete == null) {
                Toast.makeText(this, "Ingrese o busque el bovino que desea eliminar", Toast.LENGTH_SHORT).show()
                return
            }

            val filasAfec = dbHelper.eliminarBovino(idToDelete)
            if (filasAfec > 0) {
                if (letra.isNotEmpty() && numero.isNotEmpty()) {
                    firebaseHelper.eliminarBovino(letra, numero) { }
                }
                Toast.makeText(this, "🐄 Bovino eliminado correctamente (Sincronizado con Firebase 🔥)", Toast.LENGTH_SHORT).show()
                limpiarCampos()
                actualizarLista()
            } else {
                Toast.makeText(this, "No se pudo eliminar el bovino", Toast.LENGTH_SHORT).show()
            }

        } else {
            val codigo = etCodigo.text.toString().trim().uppercase()

            var idToDelete = currentCultivoId
            if (idToDelete == null && codigo.isNotEmpty()) {
                val encontrado = dbHelper.buscarCultivo(codigo)
                idToDelete = encontrado?.id
            }

            if (idToDelete == null) {
                Toast.makeText(this, "Ingrese o busque el cultivo que desea eliminar", Toast.LENGTH_SHORT).show()
                return
            }

            val filasAfec = dbHelper.eliminarCultivo(idToDelete)
            if (filasAfec > 0) {
                if (codigo.isNotEmpty()) {
                    firebaseHelper.eliminarCultivo(codigo) { }
                }
                Toast.makeText(this, "🌱 Cultivo eliminado correctamente (Sincronizado con Firebase 🔥)", Toast.LENGTH_SHORT).show()
                limpiarCampos()
                actualizarLista()
            } else {
                Toast.makeText(this, "No se pudo eliminar el cultivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun limpiarCampos() {
        currentBovinoId = null
        currentCultivoId = null

        etLetra.text?.clear()
        etNumero.text?.clear()
        etColor.text?.clear()
        etRaza.text?.clear()
        etObsBovino.text?.clear()

        etCodigo.text?.clear()
        etNombreCultivo.text?.clear()
        etHectareas.text?.clear()
        etEstado.text?.clear()
        etObsCultivo.text?.clear()
    }

    private fun actualizarLista() {
        if (isModoBovinos()) {
            tvTituloLista.text = getString(R.string.title_lista_bovinos)
            val lista = dbHelper.obtenerTodosBovinos()
            if (lista.isEmpty()) {
                tvListaRegistros.text = "No hay bovinos registrados en la base de datos."
            } else {
                val sb = StringBuilder()
                for (b in lista) {
                    val colorText = b.color.ifEmpty { "N/A" }
                    val razaText = b.raza.ifEmpty { "N/A" }
                    sb.append("• [ID: ${b.id}] Letra: ${b.letra} | Número: ${b.numero}\n")
                    sb.append("   Color: $colorText | Raza: $razaText\n")
                    if (b.observaciones.isNotEmpty()) {
                        sb.append("   Obs: ${b.observaciones}\n")
                    }
                    sb.append("\n")
                }
                tvListaRegistros.text = sb.toString().trimEnd()
            }
        } else {
            tvTituloLista.text = getString(R.string.title_lista_cultivos)
            val lista = dbHelper.obtenerTodosCultivos()
            if (lista.isEmpty()) {
                tvListaRegistros.text = "No hay cultivos registrados en la base de datos."
            } else {
                val sb = StringBuilder()
                for (c in lista) {
                    val estadoText = c.estado.ifEmpty { "N/A" }
                    sb.append("• [ID: ${c.id}] Código: ${c.codigo} | Nombre: ${c.nombre}\n")
                    sb.append("   Hectáreas: ${c.hectareas} ha | Estado: $estadoText\n")
                    if (c.observaciones.isNotEmpty()) {
                        sb.append("   Obs: ${c.observaciones}\n")
                    }
                    sb.append("\n")
                }
                tvListaRegistros.text = sb.toString().trimEnd()
            }
        }
    }
}
