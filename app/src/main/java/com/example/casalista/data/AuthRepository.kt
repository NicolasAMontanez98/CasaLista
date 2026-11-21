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
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await

/** Se lanza cuando el administrador desactivó la cuenta del usuario. */
class SuspendedAccountException : Exception()

/** Se lanza cuando la cuenta existe en Authentication pero su perfil se borró de Firestore. */
class AccountNotFoundException : Exception()

/**
 * Inicio de sesión, registro y perfil del usuario actual.
 * La contraseña vive en Firebase Authentication; el rol y los datos del perfil,
 * en el documento `users/{uid}` de Firestore.
 */
object AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val users = FirebaseFirestore.getInstance().collection("users")

    /** Hay una sesión de Firebase guardada en el dispositivo. */
    val isSignedIn get() = auth.currentUser != null

    /**
     * Crea la cuenta y su perfil con rol comprador. Si falla al guardar el perfil,
     * borra la cuenta recién creada para no dejarla huérfana. Al final cierra la sesión
     * para que el usuario entre con su contraseña.
     */
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

    /** Inicia sesión con correo y contraseña y devuelve el perfil del usuario. */
    suspend fun login(email: String, password: String): User {
        auth.signInWithEmailAndPassword(email, password).await()
        return loadCurrent()
    }

    /**
     * Lee el perfil del usuario con sesión abierta. Cierra la sesión y lanza una
     * excepción si el perfil no existe o la cuenta está desactivada.
     */
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

    /** Envía el correo para restablecer la contraseña. */
    suspend fun sendPasswordReset(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: FirebaseAuthInvalidUserException) {
            // Se responde igual que si existiera, para no revelar qué correos están registrados.
        }
    }

    /** Cierra la sesión de Firebase en este dispositivo. */
    fun logout() = auth.signOut()

    /** Guarda el nombre y el teléfono del perfil. */
    suspend fun updateProfile(user: User) {
        users.document(user.uid).update(mapOf("name" to user.name, "phone" to user.phone)).await()
    }

    /**
     * Elimina el perfil y luego la cuenta de Authentication. Si el segundo paso falla,
     * restaura el perfil para que no quede una cuenta sin datos.
     */
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

    /** Convierte una excepción en el texto (recurso de strings) que se muestra al usuario. */
    fun errorMessage(e: Throwable): Int = when (e) {
        is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> R.string.error_credentials
        is FirebaseAuthUserCollisionException -> R.string.error_email_taken
        is FirebaseAuthWeakPasswordException -> R.string.error_password_short
        is FirebaseAuthRecentLoginRequiredException -> R.string.error_recent_login
        is SuspendedAccountException -> R.string.error_suspended
        is AccountNotFoundException -> R.string.error_account_missing
        is FirebaseNetworkException -> R.string.error_network
        is StorageException -> R.string.error_upload
        else -> R.string.error_generic
    }

}
