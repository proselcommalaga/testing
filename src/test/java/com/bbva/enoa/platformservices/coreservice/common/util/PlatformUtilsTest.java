package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.util.EtherServiceNamingUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.text.MessageFormat;
import java.util.Map;

import static com.bbva.enoa.platformservices.coreservice.common.Constants.ATENEA_LOG_ENDPOINT;
import static com.bbva.enoa.platformservices.coreservice.common.Constants.ATENEA_METRICS_ENDPOINT;

public class PlatformUtilsTest
{
    @Test
    public void testBuildAteneaMetricURL()
    {
        // given
        final String baseURL = "https://atenea.work-01.nextgen.igrupobbva/atenea/?dURL={0}&dNS={1}{2}";

        final Environment         environment   = Environment.INT;
        final Map<String, String> baseAteneaURL = Map.of(environment.name(), baseURL);
        final String namespace = RandomStringUtils.randomAlphabetic(6);

        // when
        final String ateneaURL = PlatformUtils.buildAteneaMetricURL(baseAteneaURL, environment, namespace);

        // then
        Assertions.assertEquals(MessageFormat.format(baseURL, ATENEA_METRICS_ENDPOINT, namespace, ""), ateneaURL);
    }

    @Test
    public void testBuildWORKAteneaURL()
    {
        // given
        final String baseURL = "https://atenea.work-01.nextgen.igrupobbva/atenea/?dURL={0}&dNS={1}{2}";

        final Environment         environment   = Environment.PRE;
        final Map<String, String> baseAteneaURL = Map.of(environment.name(), baseURL);
        final String namespace = RandomStringUtils.randomAlphabetic(6);
        final String releaseName = RandomStringUtils.randomAlphabetic(6);

        // when
        final String ateneaURL = PlatformUtils.buildAteneaLogURL(baseAteneaURL, environment, namespace, releaseName);

        // then
        Assertions.assertEquals(MessageFormat.format(baseURL, ATENEA_LOG_ENDPOINT, namespace, "&TFmrId=" + releaseName.toLowerCase())+"_*", ateneaURL);
    }

    @Test
    public void testBuildLIVEAteneaURL()
    {
        // given
        final String baseURL = "https://atenea.live-01.nextgen.igrupobbva/atenea/?dURL={0}&dNS={1}{2}";

        final Environment environment = Environment.PRO;
        final Map<String, String> baseAteneaURL = Map.of(environment.name(), baseURL);
        final String namespace = RandomStringUtils.randomAlphabetic(6);
        final String releaseName = RandomStringUtils.randomAlphabetic(6);

        // when
        final String ateneaURL = PlatformUtils.buildAteneaLogURL(baseAteneaURL, environment, namespace, releaseName);

        // then
        Assertions.assertEquals(MessageFormat.format(baseURL, ATENEA_LOG_ENDPOINT, namespace, "&TFmrId=" + releaseName.toLowerCase())+"_*", ateneaURL);
    }

    @Test
    public void testBuildWORKAteneaURLWithServiceName()
    {
        // given
        final String baseURL = "https://atenea.work-01.nextgen.igrupobbva/atenea/?dURL={0}&dNS={1}{2}";

        final Environment environment = Environment.INT;
        final Map<String, String> baseAteneaURL = Map.of(environment.name(), baseURL);
        final String namespace = RandomStringUtils.randomAlphabetic(6);
        final String releaseName = RandomStringUtils.randomAlphabetic(6);
        final String serviceName = RandomStringUtils.randomAlphabetic(10);
        final String monitoredResourceId = EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName);


        // when
        final String ateneaURL = PlatformUtils.buildAteneaLogURL(baseAteneaURL, environment, namespace, releaseName, serviceName);

        // then
        Assertions.assertEquals(MessageFormat.format(baseURL, ATENEA_LOG_ENDPOINT, namespace, "&TFmrId=" + monitoredResourceId), ateneaURL);
    }

    @Test
    public void testBuildLIVEAteneaURLWithServiceName()
    {
        // given
        final String baseURL = "https://atenea.live-01.nextgen.igrupobbva/atenea/?dURL={0}&dNS={1}{2}";

        final Environment environment = Environment.PRO;
        final Map<String, String> baseAteneaURL = Map.of(environment.name(), baseURL);
        final String namespace = RandomStringUtils.randomAlphabetic(6);
        final String releaseName = RandomStringUtils.randomAlphabetic(6);
        final String serviceName = RandomStringUtils.randomAlphabetic(10);
        final String monitoredResourceId = EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName);

        // when
        final String ateneaURL = PlatformUtils.buildAteneaLogURL(baseAteneaURL, environment, namespace, releaseName, serviceName);

        // then
        Assertions.assertEquals(MessageFormat.format(baseURL, ATENEA_LOG_ENDPOINT, namespace, "&TFmrId=" + monitoredResourceId), ateneaURL);
    }

    @Test
    public void testBuildWORKAteneaURLWithEmptyServiceName()
    {
        // given
        final String baseURL = "https://atenea.work-01.nextgen.igrupobbva/atenea/?dURL={0}&dNS={1}{2}";

        final Environment environment = Environment.INT;
        final Map<String, String> baseAteneaURL = Map.of(environment.name(), baseURL);
        final String namespace = RandomStringUtils.randomAlphabetic(6);
        final String releaseName = RandomStringUtils.randomAlphabetic(6);

        // when
        final String ateneaURL = PlatformUtils.buildAteneaLogURL(baseAteneaURL, environment, namespace, releaseName, new String[0]);

        // then
        Assertions.assertEquals(MessageFormat.format(baseURL, ATENEA_LOG_ENDPOINT, namespace, "&TFmrId=" + releaseName.toLowerCase()) + "_*", ateneaURL);
    }

    @Test
    public void testBuildLIVEAteneaURLWithONEServiceName()
    {
        // given
        final String baseURL = "https://atenea.live-01.nextgen.igrupobbva/atenea/?dURL={0}&dNS={1}{2}";

        final Environment environment = Environment.PRO;
        final Map<String, String> baseAteneaURL = Map.of(environment.name(), baseURL);
        final String namespace = RandomStringUtils.randomAlphabetic(6);
        final String releaseName = RandomStringUtils.randomAlphabetic(6);
        final String serviceName = RandomStringUtils.randomAlphabetic(10);
        final String monitoredResourceId = EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName);

        // when
        final String ateneaURL = PlatformUtils.buildAteneaLogURL(baseAteneaURL, environment, namespace, releaseName, new String[] { serviceName });

        // then
        Assertions.assertEquals(MessageFormat.format(baseURL, ATENEA_LOG_ENDPOINT, namespace, "&TFmrId=" + monitoredResourceId), ateneaURL);
    }

    @Test
    public void testBuildWORKAteneaURLWithThreeServiceNames()
    {
        // given
        final String baseURL = "https://atenea.work-01.nextgen.igrupobbva/atenea/?dURL={0}&dNS={1}{2}";

        final Environment environment = Environment.INT;
        final Map<String, String> baseAteneaURL = Map.of(environment.name(), baseURL);
        final String namespace = RandomStringUtils.randomAlphabetic(6);
        final String releaseName = RandomStringUtils.randomAlphabetic(6);
        final String serviceName1 = RandomStringUtils.randomAlphabetic(4);
        final String serviceName2 = RandomStringUtils.randomAlphabetic(7);
        final String serviceName3 = RandomStringUtils.randomAlphabetic(8);
        final String serviceQuery = MessageFormat.format("&TFmrId={0}'' or mrId=''{1}'' or mrId=''{2}",
                EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName1),
                EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName2),
                EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName3));

        // when
        final String ateneaURL = PlatformUtils.buildAteneaLogURL(baseAteneaURL, environment, namespace, releaseName, new String[] { serviceName1, serviceName2, serviceName3 });

        // then
        Assertions.assertEquals(MessageFormat.format(baseURL, ATENEA_LOG_ENDPOINT, namespace, serviceQuery), ateneaURL);
    }


    @Test
    public void testBuildLIVEAteneaURLWithThreeServiceNames()
    {
        // given
        final String baseURL = "https://atenea.live-01.nextgen.igrupobbva/atenea/?dURL={0}&dNS={1}{2}";

        final Environment environment = Environment.PRO;
        final Map<String, String> baseAteneaURL = Map.of(environment.name(), baseURL);
        final String namespace = RandomStringUtils.randomAlphabetic(6);
        final String releaseName = RandomStringUtils.randomAlphabetic(6);
        final String serviceName1 = RandomStringUtils.randomAlphabetic(4);
        final String serviceName2 = RandomStringUtils.randomAlphabetic(7);
        final String serviceName3 = RandomStringUtils.randomAlphabetic(8);
        final String serviceQuery = MessageFormat.format("&TFmrId={0}'' or mrId=''{1}'' or mrId=''{2}",
                EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName1),
                EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName2),
                EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName3));

        // when
        final String ateneaURL = PlatformUtils.buildAteneaLogURL(baseAteneaURL, environment, namespace, releaseName, new String[] { serviceName1, serviceName2, serviceName3 });

        // then
        Assertions.assertEquals(MessageFormat.format(baseURL, ATENEA_LOG_ENDPOINT, namespace, serviceQuery), ateneaURL);
    }
}