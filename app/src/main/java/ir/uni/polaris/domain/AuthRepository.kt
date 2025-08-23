package ir.uni.polaris.domain

import ir.uni.polaris.model.AuthUser

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<AuthUser>
    suspend fun register(username: String, password: String): Result<AuthUser>
}
