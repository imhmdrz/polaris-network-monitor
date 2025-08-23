package ir.uni.polaris.model

data class AuthUser(
  val id: Long,
  val username: String,
  val accessToken: String
)