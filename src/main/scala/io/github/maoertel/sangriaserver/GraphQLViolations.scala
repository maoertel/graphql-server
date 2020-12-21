package io.github.maoertel.sangriaserver

import sangria.validation.ValueCoercionViolation

object GraphQLViolations {

  case object ObjectIdViolation extends ValueCoercionViolation("Input was not a valid ObjectId")

}
