package com.example.casalista.data

import com.google.firebase.firestore.DocumentSnapshot

/** Convierte un [User] en el mapa de campos que se guarda en Firestore. */
internal fun User.toMap() = mapOf(
    "name" to name,
    "email" to email,
    "role" to role.name,
    "phone" to phone,
    "active" to active,
)

/** Reconstruye un [User] desde un documento de Firestore; si el rol es desconocido, es comprador. */
internal fun DocumentSnapshot.toUser(): User {
    val role = Role.entries.find { it.name == getString("role") } ?: Role.BUYER
    val user = User(getString("name").orEmpty(), getString("email").orEmpty(), role)
    user.uid = id
    user.phone = getString("phone").orEmpty()
    user.active = getBoolean("active") ?: true
    return user
}
