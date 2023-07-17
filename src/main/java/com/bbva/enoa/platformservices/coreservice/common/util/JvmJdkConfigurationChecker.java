package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdk;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JvmJdkConfigurationChecker
{
    private final Integer minimumMultiJdkJavaVersion;

    @Autowired
    public JvmJdkConfigurationChecker(@Value("${nova.minimumMultiJdkJavaVersion:11}") String minimumMultiJdkJavaVersionProperty)
    {
        this.minimumMultiJdkJavaVersion = Integer.parseInt(minimumMultiJdkJavaVersionProperty);
    }

    /**
     * Checks if a release version service is Java greater or equal than 11 .
     *
     * @param releaseVersionService The release version service.
     * @return true if JVM version is greater or equal than 11. False otherwise.
     */
    public boolean isMultiJdk(ReleaseVersionService releaseVersionService)
    {
        final ServiceType serviceType = ServiceType.valueOf(releaseVersionService.getServiceType());
        if (!ServiceType.isJdkSelectable(serviceType))
        {
            return false;
        }
        final AllowedJdk allowedJdk = releaseVersionService.getAllowedJdk();
        if (allowedJdk == null)
        {
            return false;
        }
        final String jvmVersion = allowedJdk.getJvmVersion();
        if (jvmVersion.isBlank())
        {
            return false;
        }
        final int javaMajorVersion = Integer.parseInt(jvmVersion.split("\\.")[0]);
        return javaMajorVersion >= minimumMultiJdkJavaVersion;
    }
}
