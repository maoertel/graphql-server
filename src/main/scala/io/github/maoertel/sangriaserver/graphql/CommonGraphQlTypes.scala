package io.github.maoertel.sangriaserver.graphql

import io.github.maoertel.sangriaserver.GraphQLViolations.ObjectIdViolation
import org.mongodb.scala.bson.ObjectId
import sangria.schema.{fields, Argument, Field, InterfaceType, ScalarAlias, StringType}

object CommonGraphQlTypes {

  implicit val ObjectIdType: ScalarAlias[ObjectId, String] = ScalarAlias[ObjectId, String](
    StringType,
    toScalar = _.toString,
    fromScalar = idString =>
      try Right(new ObjectId(idString))
      catch {
        case _: IllegalArgumentException => Left(ObjectIdViolation)
      })

  val Id: Argument[ObjectId] = Argument("id", ObjectIdType)

  trait Identifiable {
    def id: ObjectId
  }

  val IdentifiableType: InterfaceType[Unit, Identifiable] = InterfaceType(
    "Identifiable",
    "Entity that can be identified",
    fields[Unit, Identifiable](Field("id", ObjectIdType, resolve = _.value.id)))
}
