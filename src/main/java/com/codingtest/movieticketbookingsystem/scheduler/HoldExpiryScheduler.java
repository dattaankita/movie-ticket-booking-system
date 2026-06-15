package com.codingtest.movieticketbookingsystem.scheduler;

import com.codingtest.movieticketbookingsystem.service.HoldExpiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HoldExpiryScheduler {

    private final HoldExpiryService holdExpiryService;

    @Scheduled(fixedRateString = "${booking.hold.expiry-check-interval-ms:30000}")
    public void expireStaleHolds() {
        int expiredCount = holdExpiryService.expireHolds();
        if (expiredCount > 0) {
            log.info("Expired {} seat hold(s)", expiredCount);
        }
    }
}
