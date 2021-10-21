package dev.rmaiun.soos.errors

import dev.rmaiun.errorhandling.errors.AppRuntimeException

object AlgorithmErrors extends ErrorInfo {
  case class AlgorithmNotFoundException(p: Map[String, String])
      extends AppRuntimeException("algorithmNotFound", "Algorithm is not found", app, Some(p))

}