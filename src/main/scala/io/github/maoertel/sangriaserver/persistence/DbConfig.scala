package io.github.maoertel.sangriaserver.persistence

import org.bson.codecs.configuration.CodecRegistry
import org.mongodb.scala.ConnectionString

case class DbConfig(
  connectionString: ConnectionString,
  dbName: String,
  user: String,
  password: Array[Char],
  codecRegistry: CodecRegistry
)
