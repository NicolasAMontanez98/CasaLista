package com.example.casalista.data

import com.google.firebase.firestore.DocumentSnapshot

internal fun User.toMap() = mapOf(
    "name" to name,
    "email" to email,
    "role" to role.name,
    "phone" to phone,
    "active" to active,
)

internal fun DocumentSnapshot.toUser(): User {
    val role = Role.entries.find { it.name == getString("role") } ?: Role.BUYER
    val user = User(getString("name").orEmpty(), getString("email").orEmpty(), role)
    user.uid = id
    user.phone = getString("phone").orEmpty()
    user.active = getBoolean("active") ?: true
    return user
}
