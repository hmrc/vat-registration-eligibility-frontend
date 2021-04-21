package helpers

import utils.TimeMachine

import java.time.{Instant, LocalDate}

class FakeTimeMachine extends TimeMachine {
  override def today: LocalDate = LocalDate.of(2020, 1, 1)

  override val instant: Instant = Instant.now
}
