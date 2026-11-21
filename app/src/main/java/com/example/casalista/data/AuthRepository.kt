package com.example.casalista.data

import com.example.casalista.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SuspendedAccountException : Exception()

class AccountNotFoundException : Exception()

object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val users = FirebaseFirestore.getInstance().collection("users")

    val isSignedIn get() = auth.currentUser != null

    suspend fun register(name: String, email: String, password: String) {
        val firebaseUser = auth.createUserWithEmailAndPassword(email, password).await().user!!
        val user = User(name, email, Role.BUYER)
        user.uid = firebaseUser.uid
        try {
            users.document(user.uid).set(user.toMap()).await()
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
        val doc = ref.get().await()
        if (!doc.exists()) {
            auth.signOut()
            throw AccountNotFoundException()
        }
        val user = doc.toUser()
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
            ref.set(user.toMap()).await()
            throw e
        }
    }

    fun errorMessage(e: Throwable): Int = when (e) {
        is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> R.string.error_credentials
        is FirebaseAuthUserCollisionException -> R.string.error_email_taken
        is FirebaseAuthWeakPasswordException -> R.string.error_password_short
        is FirebaseAuthRecentLoginRequiredException -> R.string.error_recent_login
        is SuspendedAccountException -> R.string.error_suspended
        is AccountNotFoundException -> R.string.error_account_missing
        is FirebaseNetworkException -> R.string.error_network
        else -> R.string.error_generic
    }

}
