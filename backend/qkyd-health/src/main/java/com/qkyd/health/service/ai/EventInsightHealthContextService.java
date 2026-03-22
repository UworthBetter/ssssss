package com.qkyd.health.service.ai;

import com.qkyd.health.domain.HealthDeviceBinding;
import com.qkyd.health.domain.HealthReport;
import com.qkyd.health.domain.HealthSubject;
import com.qkyd.health.domain.UeitException;
import com.qkyd.health.domain.UeitLocation;
import com.qkyd.health.domain.dto.ai.EventInsightHealthContext;
import com.qkyd.health.service.IHealthDeviceBindingService;
import com.qkyd.health.service.IHealthReportService;
import com.qkyd.health.service.IHealthSubjectService;
import com.qkyd.health.service.IUeitExceptionService;
import com.qkyd.health.service.IUeitLocationService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Minimal qkyd-health side context provider for event insight enrichment.
 */
@Service
public class EventInsightHealthContextService implements IEventInsightHealthContextService {
    private static final long ACTIVE_SIGNAL_HOURS = 6L;
    private static final long STALE_SIGNAL_HOURS = 24L;

    private final IHealthSubjectService healthSubjectService;
    private final IHealthDeviceBindingService healthDeviceBindingService;
    private final IUeitLocationService ueitLocationService;
    private final IUeitExceptionService ueitExceptionService;
    private final IHealthReportService healthReportService;

    public EventInsightHealthContextService(
            IHealthSubjectService healthSubjectService,
            IHealthDeviceBindingService healthDeviceBindingService,
            IUeitLocationService ueitLocationService,
            IUeitExceptionService ueitExceptionService,
            IHealthReportService healthReportService) {
        this.healthSubjectService = healthSubjectService;
        this.healthDeviceBindingService = healthDeviceBindingService;
        this.ueitLocationService = ueitLocationService;
        this.ueitExceptionService = ueitExceptionService;
        this.healthReportService = healthReportService;
    }

    @Override
    public EventInsightHealthContext loadContext(Long userId, Long deviceId, String abnormalType) {
        EventInsightHealthContext context = new EventInsightHealthContext();
        context.setUserId(userId);
        context.setRequestedDeviceId(deviceId);

        if (userId != null) {
            enrichSubject(context, userId);
            enrichLocation(context, userId);
            enrichHistory(context, userId, abnormalType);
            enrichLatestReport(context, userId);
        }

        enrichBinding(context, userId, deviceId);
        deriveDeviceSignal(context);
        return context;
    }

    private void enrichSubject(EventInsightHealthContext context, Long userId) {
        HealthSubject subject = healthSubjectService.selectHealthSubjectBySubjectId(userId);
        if (subject == null) {
            return;
        }
        context.setAge(subject.getAge());
        context.setSubjectName(firstNonBlank(subject.getNickName(), subject.getSubjectName()));
        context.setSubjectStatus(subject.getStatus());
    }

    private void enrichLocation(EventInsightHealthContext context, Long userId) {
        UeitLocation location = ueitLocationService.selectUeitLocationByUserId(userId);
        if (location == null) {
            return;
        }
        context.setLastKnownLocation(location.getLocation());
        context.setLastLocationTime(resolveLocationTime(location));
    }

    private void enrichHistory(EventInsightHealthContext context, Long userId, String abnormalType) {
        List<UeitException> history =
                ueitExceptionService.selectUeitExceptionListByUserId(userId.intValue());
        if (history == null) {
            return;
        }
        context.setHistoricalAbnormalCount(history.size());
        context.setRecentSameTypeCount(countSameType(history, abnormalType));
    }

    private void enrichLatestReport(EventInsightHealthContext context, Long userId) {
        HealthReport query = new HealthReport();
        query.setUserId(userId);
        List<HealthReport> reports = healthReportService.selectHealthReportList(query);
        if (reports == null || reports.isEmpty()) {
            return;
        }
        HealthReport latestReport = reports.get(0);
        context.setLatestReportSummary(latestReport.getSummary());
        context.setLatestReportRiskLevel(latestReport.getRiskLevel());
        context.setLatestReportDate(latestReport.getReportDate());
    }

    private void enrichBinding(EventInsightHealthContext context, Long userId, Long deviceId) {
        HealthDeviceBinding binding = null;
        if (deviceId != null) {
            List<HealthDeviceBinding> bindings = healthDeviceBindingService.selectByDeviceId(deviceId);
            binding = firstBinding(bindings);
        }
        if (binding == null && userId != null) {
            List<HealthDeviceBinding> bindings = healthDeviceBindingService.selectBySubjectId(userId);
            binding = firstBinding(bindings);
        }
        if (binding == null) {
            context.setHasActiveBinding(Boolean.FALSE);
            return;
        }
        context.setHasActiveBinding(Boolean.TRUE);
        context.setBoundDeviceId(binding.getDeviceId());
        context.setBindingTime(binding.getBindTime());
    }

    private void deriveDeviceSignal(EventInsightHealthContext context) {
        Date signalTime = context.getLastLocationTime();
        if (signalTime == null) {
            if (Boolean.TRUE.equals(context.getHasActiveBinding())) {
                context.setDeviceSignalStatus("bound_without_signal");
                context.setDeviceSignalReason("device has an active binding but no recent location signal");
            } else {
                context.setDeviceSignalStatus("unbound");
                context.setDeviceSignalReason("no active device binding or recent location signal");
            }
            return;
        }

        long ageHours = TimeUnit.MILLISECONDS.toHours(Math.max(0L, new Date().getTime() - signalTime.getTime()));
        if (ageHours <= ACTIVE_SIGNAL_HOURS) {
            context.setDeviceSignalStatus("active");
            context.setDeviceSignalReason("recent location signal detected within " + ACTIVE_SIGNAL_HOURS + " hours");
            return;
        }
        if (ageHours <= STALE_SIGNAL_HOURS) {
            context.setDeviceSignalStatus("stale");
            context.setDeviceSignalReason("latest location signal is older than " + ACTIVE_SIGNAL_HOURS + " hours");
            return;
        }
        context.setDeviceSignalStatus("offline");
        context.setDeviceSignalReason("no location signal has been observed within " + STALE_SIGNAL_HOURS + " hours");
    }

    private Date resolveLocationTime(UeitLocation location) {
        return location.getCreateTime() != null ? location.getCreateTime() : location.getReadTime();
    }

    private int countSameType(List<UeitException> history, String abnormalType) {
        if (history == null || history.isEmpty() || isBlank(abnormalType)) {
            return 0;
        }
        int count = 0;
        for (UeitException item : history) {
            if (item != null && abnormalType.equalsIgnoreCase(trimToEmpty(item.getType()))) {
                count++;
            }
        }
        return count;
    }

    private HealthDeviceBinding firstBinding(List<HealthDeviceBinding> bindings) {
        return bindings == null || bindings.isEmpty() ? null : bindings.get(0);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
