package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.IApiModalitySegregatable;

import java.util.List;

/**
 * Interface for building and storing new API entries into the database
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface IApiManagerServiceModalityBased<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
        extends IApiModalitySegregatable
{
    /**
     * From a swagger file builds a API sync and saves it into the database
     *
     * @param apiUploadRequest Upload request info
     * @param productId        product Id
     * @return List of errors of the validation process, if any.
     */
    ApiErrorList uploadApi(final ApiUploadRequestDto apiUploadRequest, final Integer productId);

    void createApiTask(final TaskInfoDto taskInfoDto, final A api);

    void removeApiRegistration(final A api);

    void onPolicyTaskReply(final ToDoTaskStatus taskStatus, final A api);

    void deleteApiTodoTasks(final A api);

    List<A> getApisUsingMsaDocument(final Integer msaDocumentId);

    List<A> getApisUsingAraDocument(final Integer araDocumentId);

    AI createApiImplementation(final AV apiVersion, final ReleaseVersionService releaseVersionService, final ImplementedAs implementedAs);

    AI createBehaviorApiImplementation(final AV apiVersion, final BehaviorService behaviorService, final ImplementedAs implementedAs);

    AI setConsumedApisByServedApi(final AI servedApi, final List<Integer> consumedApis);

    AI setBackwardCompatibleVersionsOfServedApi(final AI servedApi, final List<String> backwardCompatibleVersions);

    byte[] downloadProductApi(AV apiVersion, final String format, final String downloadType);
}
