package com.example.hacienda_vivas

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()
    private val rtdb = FirebaseDatabase.getInstance("https://hacienda-vivas-default-rtdb.firebaseio.com/").reference

    // --- BOVINOS (FIRESTORE & REALTIME DATABASE) ---

    fun guardarBovino(bovino: Bovino, onComplete: (Boolean) -> Unit) {
        val docId = "${bovino.letra}_${bovino.numero}"
        val data = hashMapOf(
            "id" to bovino.id,
            "letra" to bovino.letra,
            "numero" to bovino.numero,
            "color" to bovino.color,
            "raza" to bovino.raza,
            "observaciones" to bovino.observaciones,
        )

        // Guardar en Realtime Database
        rtdb.child("bovinos").child(docId).setValue(data)

        // Guardar en Firestore
        db.collection("bovinos").document(docId)
            .set(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun eliminarBovino(letra: String, numero: String, onComplete: (Boolean) -> Unit) {
        val docId = "${letra}_$numero"

        // Eliminar de Realtime Database
        rtdb.child("bovinos").child(docId).removeValue()

        // Eliminar de Firestore
        db.collection("bovinos").document(docId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun buscarBovinoRealtime(letra: String, numero: String, onResult: (Bovino?) -> Unit) {
        val docId = "${letra}_${numero}"
        rtdb.child("bovinos").child(docId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val b = parseBovinoSnapshot(snapshot)
                    onResult(b)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun escucharBovinosRealtime(onDataChange: (List<Bovino>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Bovino>()
                for (child in snapshot.children) {
                    val b = parseBovinoSnapshot(child)
                    if (b != null) {
                        lista.add(b)
                    }
                }
                onDataChange(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                onDataChange(emptyList())
            }
        }
        rtdb.child("bovinos").addValueEventListener(listener)
        return listener
    }

    fun removerListenerBovinos(listener: ValueEventListener) {
        rtdb.child("bovinos").removeEventListener(listener)
    }

    private fun parseBovinoSnapshot(snapshot: DataSnapshot): Bovino? {
        val id = (snapshot.child("id").value as? Long)?.toInt() ?: 0
        val numero = snapshot.child("numero").getValue(String::class.java) ?: ""
        val letra = snapshot.child("letra").getValue(String::class.java) ?: ""
        val color = snapshot.child("color").getValue(String::class.java) ?: ""
        val raza = snapshot.child("raza").getValue(String::class.java) ?: ""
        val obs = snapshot.child("observaciones").getValue(String::class.java) ?: ""
        if (numero.isEmpty() && letra.isEmpty()) return null
        return Bovino(
            id = id,
            numero = numero,
            letra = letra,
            color = color,
            raza = raza,
            observaciones = obs,
        )
    }

    // --- CULTIVOS (FIRESTORE & REALTIME DATABASE) ---

    fun guardarCultivo(cultivo: Cultivo, onComplete: (Boolean) -> Unit) {
        val docId = cultivo.codigo
        val data = hashMapOf(
            "id" to cultivo.id,
            "codigo" to cultivo.codigo,
            "nombre" to cultivo.nombre,
            "hectareas" to cultivo.hectareas,
            "estado" to cultivo.estado,
            "observaciones" to cultivo.observaciones,
        )

        // Guardar en Realtime Database
        rtdb.child("cultivos").child(docId).setValue(data)

        // Guardar en Firestore
        db.collection("cultivos").document(docId)
            .set(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun eliminarCultivo(codigo: String, onComplete: (Boolean) -> Unit) {
        // Eliminar de Realtime Database
        rtdb.child("cultivos").child(codigo).removeValue()

        // Eliminar de Firestore
        db.collection("cultivos").document(codigo)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun buscarCultivoRealtime(codigo: String, onResult: (Cultivo?) -> Unit) {
        rtdb.child("cultivos").child(codigo).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val c = parseCultivoSnapshot(snapshot)
                    onResult(c)
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun escucharCultivosRealtime(onDataChange: (List<Cultivo>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = mutableListOf<Cultivo>()
                for (child in snapshot.children) {
                    val c = parseCultivoSnapshot(child)
                    if (c != null) {
                        lista.add(c)
                    }
                }
                onDataChange(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                onDataChange(emptyList())
            }
        }
        rtdb.child("cultivos").addValueEventListener(listener)
        return listener
    }

    fun removerListenerCultivos(listener: ValueEventListener) {
        rtdb.child("cultivos").removeEventListener(listener)
    }

    private fun parseCultivoSnapshot(snapshot: DataSnapshot): Cultivo? {
        val id = (snapshot.child("id").value as? Long)?.toInt() ?: 0
        val codigo = snapshot.child("codigo").getValue(String::class.java) ?: ""
        val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
        val hectareas = (snapshot.child("hectareas").value as? Number)?.toDouble() ?: 0.0
        val estado = snapshot.child("estado").getValue(String::class.java) ?: ""
        val obs = snapshot.child("observaciones").getValue(String::class.java) ?: ""
        if (codigo.isEmpty() && nombre.isEmpty()) return null
        return Cultivo(
            id = id,
            codigo = codigo,
            nombre = nombre,
            hectareas = hectareas,
            estado = estado,
            observaciones = obs
        )
    }

    // --- METODOS FIRESTORE (por compatibilidad) ---

    fun escucharBovinos(onDataChange: (List<Bovino>) -> Unit): ListenerRegistration {
        return db.collection("bovinos")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val lista = mutableListOf<Bovino>()
                for (doc in snapshot.documents) {
                    val b = Bovino(
                        id = (doc.getLong("id") ?: 0L).toInt(),
                        numero = doc.getString("numero") ?: "",
                        letra = doc.getString("letra") ?: "",
                        color = doc.getString("color") ?: "",
                        raza = doc.getString("raza") ?: "",
                        observaciones = doc.getString("observaciones") ?: "",
                    )
                    lista.add(b)
                }
                onDataChange(lista)
            }
    }

    fun escucharCultivos(onDataChange: (List<Cultivo>) -> Unit): ListenerRegistration {
        return db.collection("cultivos")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val lista = mutableListOf<Cultivo>()
                for (doc in snapshot.documents) {
                    val c = Cultivo(
                        id = (doc.getLong("id") ?: 0L).toInt(),
                        codigo = doc.getString("codigo") ?: "",
                        nombre = doc.getString("nombre") ?: "",
                        hectareas = doc.getDouble("hectareas") ?: 0.0,
                        estado = doc.getString("estado") ?: "",
                        observaciones = doc.getString("observaciones") ?: "",
                    )
                    lista.add(c)
                }
                onDataChange(lista)
            }
    }
}
