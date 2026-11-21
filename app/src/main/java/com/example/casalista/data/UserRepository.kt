package com.example.casalista.data

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** CRUD de usuarios en la colección `users`. Lo usa el administrador. */
object UserRepository {
    private val users = FirebaseFirestore.getInstance().collection("users")

    /** Todos los usuarios registrados. */
    suspend fun getAll(): List<User> =
        users.get().await().documents.map { it.toUser() }

    /** Un usuario por su uid, o null si no existe. */
    suspend fun get(uid: String): User? {
        val doc = users.document(uid).get().await()
        return if (doc.exists()) doc.toUser() else null
    }

    /**
     * Crea una cuenta con el rol indicado. Se usa una segunda instancia de Firebase para
     * que el administrador no pierda su propia sesión al crear la cuenta de otra persona.
     * Si no se puede guardar el perfil, se borra la cuenta creada.
     */
    suspend fun create(name: String, email: String, password: String, role: Role) {
        val secondary = secondaryAuth()
        val created = secondary.createUserWithEmailAndPassword(email, password).await().user!!
        val user = User(name, email, role)
        user.uid = created.uid
        try {
            users.document(user.uid).set(user.toMap()).await()
        } catch (e: Exception) {
            runCatching { created.delete().await() }
            throw e
        } finally {
            secondary.signOut()
        }
    }

    /** Actualiza nombre, rol, teléfono y estado (activo o suspendido). */
    suspend fun update(user: User) {
        users.document(user.uid).update(
            mapOf(
                "name" to user.name,
                "role" to user.role.name,
                "phone" to user.phone,
                "active" to user.active,
            ),
        ).await()
    }

    /** Borra el perfil. La cuenta de Authentication no se puede borrar desde el cliente. */
    suspend fun delete(user: User) {
        users.document(user.uid).delete().await()
    }

    /** Devuelve la autenticación de la instancia secundaria, creándola la primera vez. */
    private fun secondaryAuth(): FirebaseAuth {
        val main = FirebaseApp.getInstance()
        val app = try {
            FirebaseApp.getInstance("secondary")
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(main.applicationContext, main.options, "secondary")
        }
        return FirebaseAuth.getInstance(app)
    }
}

/** Filtra usuarios por texto (nombre o correo) y por rol, y los ordena por nombre. */
fun filterUsers(users: List<User>, query: String, role: Role?): List<User> {
    val text = query.trim()
    return users
        .filter { role == null || it.role == role }
        .filter { it.name.contains(text, ignoreCase = true) || it.email.contains(text, ignoreCase = true) }
        .sortedBy { it.name }
}
