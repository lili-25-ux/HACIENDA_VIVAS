package com.example.hacienda_vivas

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()

    // --- BOVINOS FIRESTORE ---

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

        db.collection("bovinos").document(docId)
            .set(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun eliminarBovino(letra: String, numero: String, onComplete: (Boolean) -> Unit) {
        val docId = "${letra}_${numero}"
        db.collection("bovinos").document(docId)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

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

    // --- CULTIVOS FIRESTORE ---

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

        db.collection("cultivos").document(docId)
            .set(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun eliminarCultivo(codigo: String, onComplete: (Boolean) -> Unit) {
        db.collection("cultivos").document(codigo)
            .delete()
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
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
