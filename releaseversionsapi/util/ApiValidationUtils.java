package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Objects;

@Slf4j
/**
 * Utility Class related to nova.yml apis and their service type
 */
public class ApiValidationUtils
{
    /**
     * Returns true if the nova.yml has an async BackToBack api
     * @param novaYml
     * @return true if the novaYml has a backToBack api
     */
    public static boolean hasNovaymlBackToBack(NovaYml novaYml)
    {
        return Objects.nonNull( novaYml.getAsyncapisBackToBack() ) &&
                novaYml.getAsyncapisBackToBack().isNotEmpty();
    }

    /**
     * Returns true if the nova.yml has a sync served api or a async BackToFront api
     * @param novaYml
     * @return true if the novaYml has a served sync or async api
     */
    public static boolean hasNovaymlAnyApi(NovaYml novaYml)
    {
        boolean hasSyncServedApi = CollectionUtils.isNotEmpty(novaYml.getApiServed());
        boolean hasBackToFrontApi = Objects.nonNull( novaYml.getAsyncapisBackToFront() ) &&
                novaYml.getAsyncapisBackToFront().isNotEmpty();
        log.debug("The nova.yml "+ novaYml.getApplicationName()+": hasSyncServedApi="+hasSyncServedApi+" hasBackToFrontApi="+hasBackToFrontApi);
        return hasSyncServedApi || hasBackToFrontApi;
    }

    /**
     * Returns true if the nova.yml has NOT a sync served api or a async BackToFront api
     * @param novaYml
     * @return true if the novaYml has NOT a served sync or async api
     */
    public static boolean hasNotNovaymlAnApi(NovaYml novaYml)
    {
        return !hasNovaymlAnyApi(novaYml);
    }

    /**
     * Check if the novaYml is an API (API_REST or an API), and has a any served api (sync served or async backToFront)
     * @param novaYml
     * @return
     */
    public static boolean hasNovaymlAnApiImplementedAsServed(final NovaYml novaYml)
    {
        return ServiceType.isJavaApiServiceType(novaYml.getServiceType()) && hasNovaymlAnyApi(novaYml);
    }
}
