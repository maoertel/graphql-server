package io.github.maoertel.sangriaserver.graphql

import io.github.maoertel.sangriaserver.GraphQlService
import sangria.schema._

trait GraphQlSchema {
  def schema: Schema[GraphQlService, Unit]
}

trait GraphQlTypes {
  val queryFields: List[Field[GraphQlService, Unit]]
  val mutationFields: List[Field[GraphQlService, Unit]]
}

object GraphQlSchema {

  def apply(graphQlTypes: GraphQlTypes*): GraphQlSchema = new GraphQlSchema {

    val QueryType: ObjectType[GraphQlService, Unit] = ObjectType(
      name = "Query",
      fields = graphQlTypes.toList.flatMap(_.queryFields)
    )

    val MutationType: ObjectType[GraphQlService, Unit] = ObjectType(
      name = "Mutation",
      fields = graphQlTypes.toList.flatMap(_.mutationFields)
    )

    override lazy val schema: Schema[GraphQlService, Unit] = Schema(QueryType, Some(MutationType))
  }
}
