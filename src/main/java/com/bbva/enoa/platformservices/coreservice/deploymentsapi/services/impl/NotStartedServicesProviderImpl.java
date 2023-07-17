package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.platformservices.coreservice.common.repositories.ServiceExecutionHistoryRepository;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.INotStartedServicesProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;

@Service
public class NotStartedServicesProviderImpl implements INotStartedServicesProvider
{
    private final ServiceExecutionHistoryRepository repository;
    private final Integer daysBeforeNow;

    @Autowired
    public NotStartedServicesProviderImpl(ServiceExecutionHistoryRepository repository, @Value("#{new Integer('${nova.storageDays.serviceExecutionHistory:90}')}") final Integer daysBeforeNow)
    {
        this.repository = repository;
        this.daysBeforeNow = daysBeforeNow;
    }

    @Override
    public List<String> getNotStartedVersionedNamesFromServiceExecutionHistory(List<String> versionedFinalNamesToCheck, final String environment)
    {
        final LocalDateTime now = LocalDate.now().atStartOfDay();
        final LocalDateTime startDateTime = now.minus(daysBeforeNow.longValue(), ChronoUnit.DAYS);
        final long startDateMillis = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Calendar startDate = Calendar.getInstance();
        startDate.setTimeInMillis(startDateMillis);
        final List<String> executedFinalNameVersions = repository.findExecutedVersionedFinalNamesMatching(versionedFinalNamesToCheck, environment, startDate);
        versionedFinalNamesToCheck.removeAll(executedFinalNameVersions);
        return versionedFinalNamesToCheck;
    }
}
