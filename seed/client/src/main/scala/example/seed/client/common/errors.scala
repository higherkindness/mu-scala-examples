package example.seed.client.common

sealed trait SeedError extends Exception { val message: String }

case class PersonNotFoundError(message: String) extends SeedError