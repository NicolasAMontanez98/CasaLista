package com.example.casalista.data

import com.example.casalista.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SuspendedAccountException : Exception()

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val users = FirebaseFirestore.getInstance().collection("users")

    val isSignedIn get() = auth.currentUser != null

    suspend fun register(name: String, email: String, password: String) {
        val firebaseUser = auth.createUserWithEmailAndPassword(email, password).await().user!!
        val user = User(name, email, "", Role.BUYER)
        user.uid = firebaseUser.uid
        try {
            users.document(user.uid).set(toMap(user)).await()
        } catch (e: Exception) {
            runCatching { firebaseUser.delete().await() }
            throw e
        }
        auth.signOut()
    }

    suspend fun login(email: String, password: String): User {
        auth.signInWithEmailAndPassword(email, password).await()
        return loadCurrent()
    }

    suspend fun loadCurrent(): User {
        val firebaseUser = auth.currentUser ?: throw IllegalStateException("No hay sesión")
        val ref = users.document(firebaseUser.uid)
        var doc = ref.get().await()
        if (!doc.exists()) {
            val email = firebaseUser.email.orEmpty()
            val newUser = User(email.substringBefore("@"), email, "", Role.BUYER)
            newUser.uid = firebaseUser.uid
            ref.set(toMap(newUser)).await()
            doc = ref.get().await()
        }
        val user = toUser(doc)
        if (!user.active) {
            auth.signOut()
            throw SuspendedAccountException()
        }
        return user
    }

    suspend fun sendPasswordReset(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: FirebaseAuthInvalidUserException) {
            // Se responde igual que si existiera, para no revelar qué correos están registrados.
        }
    }

    fun logout() = auth.signOut()

    suspend fun updateProfile(user: User) {
        users.document(user.uid).update(mapOf("name" to user.name, "phone" to user.phone)).await()
    }

    suspend fun deleteAccount(user: User) {
        val ref = users.document(user.uid)
        ref.delete().await()
        try {
            auth.currentUser?.delete()?.await()
        } catch (e: Exception) {
            ref.set(toMap(user)).await()
            throw e
        }
    }

    fun errorMessage(e: Throwable): Int = when (e) {
        is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> R.string.error_credentials
        is FirebaseAuthUserCollisionException -> R.string.error_email_taken
        is FirebaseAuthWeakPasswordException -> R.string.error_password_short
        is FirebaseAuthRecentLoginRequiredException -> R.string.error_recent_login
        is SuspendedAccountException -> R.string.error_suspended
        is FirebaseNetworkException -> R.string.error_network
        else -> R.string.error_generic
    }

    private fun toMap(user: User) = mapOf(
        "name" to user.name,
        "email" to user.email,
        "role" to user.role.name,
        "phone" to user.phone,
        "active" to user.active,
    )

    private fun toUser(doc: DocumentSnapshot): User {
        val role = Role.entries.find { it.name == doc.getString("role") } ?: Role.BUYER
        val user = User(doc.getString("name").orEmpty(), doc.getString("email").orEmpty(), "", role)
        user.uid = doc.id
        user.phone = doc.getString("phone").orEmpty()
        user.active = doc.getBoolean("active") ?: true
        return user
    }
}
