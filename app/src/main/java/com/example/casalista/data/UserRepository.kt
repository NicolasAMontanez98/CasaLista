package com.example.casalista.data

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserRepository {
    private val users = FirebaseFirestore.getInstance().collection("users")

    suspend fun getAll(): List<User> =
        users.get().await().documents.map { it.toUser() }

    suspend fun get(uid: String): User? {
        val doc = users.document(uid).get().await()
        return if (doc.exists()) doc.toUser() else null
    }

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

    suspend fun delete(user: User) {
        users.document(user.uid).delete().await()
    }

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

fun filterUsers(users: List<User>, query: String, role: Role?): List<User> {
    val text = query.trim()
    return users
        .filter { role == null || it.role == role }
        .filter { it.name.contains(text, ignoreCase = true) || it.email.contains(text, ignoreCase = true) }
        .sortedBy { it.name }
}
