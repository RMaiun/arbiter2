package dev.rmaiun.arbiter2.utils

import cats.effect.unsafe.IORuntime

trait IoTestRuntime {
  implicit val ioRuntime: IORuntime = cats.effect.unsafe.implicits.global
}
