package de.samply.share.broker.monitoring;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IcingaControllerTest {

  private IcingaController controller;

  @BeforeEach
  void setUp() {
    controller = new IcingaController();
  }

  @Test
  void initialMappingIsAlwaysUpdated() {
    boolean updated = controller.updateBankLastSendInstantIfExpired(Clock.systemUTC(), 0);

    assertTrue(updated);
  }

  @Test
  void subMinuteUpdateDoesntUpdate() {
    Clock initialClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    controller.updateBankLastSendInstantIfExpired(initialClock, 0);
    Clock offsetClock = Clock.offset(initialClock, Duration.ofSeconds(59));

    boolean updated = controller.updateBankLastSendInstantIfExpired(offsetClock, 0);

    assertFalse(updated);
  }

  @Test
  void superMinuteUpdateDoesntUpdate() {
    Clock initialClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    controller.updateBankLastSendInstantIfExpired(initialClock, 0);
    Clock offsetClock = Clock.offset(initialClock, Duration.ofSeconds(60));

    boolean updated = controller.updateBankLastSendInstantIfExpired(offsetClock, 0);

    assertTrue(updated);
  }
}
