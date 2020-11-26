package de.samply.share.broker.monitoring;

import de.samply.share.broker.utils.Utils;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IcingaController controls the sending of periodic reports to Icinga.
 */
@ApplicationScoped
public class IcingaController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IcingaController.class);
  private static final int VERSION_REPORT_SEND_QUEUE_CAPACITY = 100;

  private final Map<Integer, Instant> lastBankSendInstants = new ConcurrentHashMap<>();

  private final Executor sender = new ThreadPoolExecutor(
      1, 1, 0L, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<>(VERSION_REPORT_SEND_QUEUE_CAPACITY),
      new ThreadPoolExecutor.DiscardOldestPolicy());


  /**
   * Send version of the bridgehead every minute to icinga.
   * @param userAgent the user agent with the information of the bridgehead.
   * @param bankId the bank id of the bridgehead
   */
  public void asyncSendVersionReportEveryMinute(String userAgent, int bankId) {
    if (updateBankLastSendInstantIfExpired(Clock.systemUTC(), bankId)) {
      LOGGER.debug("Schedule sending of version report to bank with ID: " + bankId);
      sender.execute(() -> Utils.sendVersionReportsToIcinga(bankId, userAgent));
    }
  }

  /**
   * Updates the instant at which the last version report of a bank was send to the current instant
   * taken from {@code clock} if either no instant was recorded or the already recorded instant is
   * older than one minute.
   *
   * @param clock  the clock to use to obtain the current instant
   * @param bankId the ID of the bank to update the instant
   * @return {@code true} iff the instant was updated
   */
  boolean updateBankLastSendInstantIfExpired(Clock clock, int bankId) {
    Instant instant = clock.instant();
    return lastBankSendInstants.merge(bankId, instant,
        (old, val) -> ChronoUnit.MINUTES.between(old, val) >= 1 ? val : old
    ).equals(instant);
  }
}
