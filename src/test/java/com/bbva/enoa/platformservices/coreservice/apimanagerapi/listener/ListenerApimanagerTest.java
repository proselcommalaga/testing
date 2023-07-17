package com.bbva.enoa.platformservices.coreservice.apimanagerapi.listener;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiMethodProfileDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiVersionDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.PolicyTaskReplyParametersDTO;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.IApiManagerService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ListenerApimanagerTest
{
    public static final String IV_USER = "xe72172";

    @Mock
    private IApiManagerService apiManagerService;

    @InjectMocks
    private ListenerApimanager listenerApimanager;


    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyMocks()
    {
        verifyNoMoreInteractions(
                apiManagerService
        );
    }

    private static NovaMetadata getNovaMetadata()
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Collections.singletonList(IV_USER));
        NovaMetadata metadata = new NovaMetadata();
        metadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
        return metadata;
    }

    @Nested
    class GetProductApis
    {
        Integer productId = 2408;

        @Test
        void ok() throws Exception
        {
            ApiDto[] expectedValue = buildArrayApiDto();

            // When
            when(apiManagerService.getProductApis(productId)).thenReturn(expectedValue);
            ApiDto[] returnValue = listenerApimanager.getProductApis(getNovaMetadata(), productId);

            verify(apiManagerService).getProductApis(productId);
            assertArrayEquals(expectedValue, returnValue);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError())).when(apiManagerService).getProductApis(productId);

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.getProductApis(getNovaMetadata(), productId));
            verify(apiManagerService).getProductApis(productId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(apiManagerService).getProductApis(productId);

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.getProductApis(getNovaMetadata(), productId));
            verify(apiManagerService).getProductApis(productId);
        }

        private ApiDto[] buildArrayApiDto()
        {
            ApiDto apiDto = new ApiDto();
            apiDto.fillRandomly(4, false, 1, 2);
            return new ApiDto[]{apiDto};
        }

    }

    @Nested
    class UploadProductApis
    {
        Integer productId = 2408;
        ApiUploadRequestDto apiUploadRequestDto = buildApiUploadRequestDto();

        @Test
        void ok() throws Exception
        {
            ApiErrorList expectedValue = buildApiErrorList();

            // When
            when(apiManagerService.uploadProductApis(apiUploadRequestDto, productId)).thenReturn(expectedValue);
            ApiErrorList returnValue = listenerApimanager.uploadProductApis(getNovaMetadata(), apiUploadRequestDto, productId);

            verify(apiManagerService).uploadProductApis(apiUploadRequestDto, productId);
            assertEquals(expectedValue, returnValue);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError()))
                    .when(apiManagerService)
                    .uploadProductApis(apiUploadRequestDto, productId);

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.uploadProductApis(getNovaMetadata(), apiUploadRequestDto, productId));
            verify(apiManagerService).uploadProductApis(apiUploadRequestDto, productId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class)
                    .when(apiManagerService)
                    .uploadProductApis(apiUploadRequestDto, productId);

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.uploadProductApis(getNovaMetadata(), apiUploadRequestDto, productId));
            verify(apiManagerService).uploadProductApis(apiUploadRequestDto, productId);
        }

        private ApiErrorList buildApiErrorList()
        {
            ApiErrorList apiErrorList = new ApiErrorList();
            apiErrorList.fillRandomly(4, false, 1, 2);
            return apiErrorList;
        }

        private ApiUploadRequestDto buildApiUploadRequestDto()
        {
            ApiUploadRequestDto apiUploadRequestDto = new ApiUploadRequestDto();
            apiUploadRequestDto.fillRandomly(4, false, 1, 2);
            return apiUploadRequestDto;
        }
    }

    @Nested
    class DeleteProductApi
    {
        Integer productId = 2408;
        Integer versionId = 909;

        @Test
        void ok() throws Exception
        {
            ApiErrorList expectedValue = buildApiErrorList();

            // When
            when(apiManagerService.deleteProductApi(productId, versionId)).thenReturn(expectedValue);
            ApiErrorList returnValue = listenerApimanager.deleteProductApi(getNovaMetadata(), productId, versionId);

            verify(apiManagerService).deleteProductApi(productId, versionId);
            assertEquals(expectedValue, returnValue);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError()))
                    .when(apiManagerService)
                    .deleteProductApi(productId, versionId);

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.deleteProductApi(getNovaMetadata(), productId, versionId));
            verify(apiManagerService).deleteProductApi(productId, versionId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(apiManagerService).deleteProductApi(productId, versionId);

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.deleteProductApi(getNovaMetadata(), productId, versionId));
            verify(apiManagerService).deleteProductApi(productId, versionId);
        }

        private ApiErrorList buildApiErrorList()
        {
            ApiErrorList apiErrorList = new ApiErrorList();
            apiErrorList.fillRandomly(4, false, 1, 2);
            return apiErrorList;
        }

    }

    @Nested
    class GetApiStatus
    {
        @Test
        void ok() throws Exception
        {
            listenerApimanager.getApiStatus(getNovaMetadata());

            verify(apiManagerService).getApiStatus();
        }

        @Test
        void novaException()
        {

            doThrow(new NovaException(ApiManagerError.getUnexpectedError()))
                    .when(apiManagerService).
                    getApiStatus();

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.getApiStatus(getNovaMetadata()));
            verify(apiManagerService).getApiStatus();
        }

        @Test
        void runtimeException()
        {

            doThrow(RuntimeException.class).when(apiManagerService).getApiStatus();

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.getApiStatus(getNovaMetadata()));
            verify(apiManagerService).getApiStatus();
        }
    }

    @Nested
    class DownloadProductApi
    {
        String format = "consumed";
        String downloadType = "JSON";
        Integer productId = 2408;
        Integer versionId = 909;

        @Test
        void ok() throws Exception
        {
            byte[] expectedValue = new byte[1];

            // When
            when(apiManagerService.downloadProductApi(productId, versionId, format, downloadType)).thenReturn(expectedValue);
            byte[] returnValue = listenerApimanager.downloadProductApi(getNovaMetadata(), format, versionId, productId, downloadType);

            verify(apiManagerService).downloadProductApi(productId, versionId, format, downloadType);
            assertEquals(expectedValue, returnValue);
        }

        @Test
        void novaException()
        {

            doThrow(new NovaException(ApiManagerError.getUnexpectedError()))
                    .when(apiManagerService).
                    downloadProductApi(productId, versionId, format, downloadType);

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.downloadProductApi(getNovaMetadata(), format, versionId, productId, downloadType));
            verify(apiManagerService).downloadProductApi(productId, versionId, format, downloadType);
        }

        @Test
        void runtimeException()
        {

            doThrow(RuntimeException.class).when(apiManagerService).downloadProductApi(productId, versionId, format, downloadType);

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.downloadProductApi(getNovaMetadata(), format, versionId, productId, downloadType));
            verify(apiManagerService).downloadProductApi(productId, versionId, format, downloadType);
        }
    }

    @Nested
    class GetPlanApiDetailList
    {
        Integer planId = 2408;

        @Test
        void ok() throws Exception
        {
            ApiPlanDetailDto apiPlanDetailDto = new ApiPlanDetailDto();

            // When
            when(apiManagerService.getPlanApiDetailList(planId)).thenReturn(apiPlanDetailDto);
            ApiPlanDetailDto returnValue = listenerApimanager.getPlanApiDetailList(getNovaMetadata(), planId);

            verify(apiManagerService).getPlanApiDetailList(planId);
            assertEquals(apiPlanDetailDto, returnValue);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError())).when(apiManagerService).getPlanApiDetailList(planId);

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.getPlanApiDetailList(getNovaMetadata(), planId));
            verify(apiManagerService).getPlanApiDetailList(planId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(apiManagerService).getPlanApiDetailList(planId);

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.getPlanApiDetailList(getNovaMetadata(), planId));
            verify(apiManagerService).getPlanApiDetailList(planId);
        }
    }

    @Nested
    class ExportProductApi
    {
        String format = "consumed";
        String downloadType = "JSON";
        Integer productId = 2408;
        Integer versionId = 909;

        @Test
        void ok() throws Exception
        {
            byte[] expectedValue = new byte[1];

            // When
            when(apiManagerService.downloadProductApi(productId, versionId, format, downloadType)).thenReturn(expectedValue);
            String returnValue = new String(listenerApimanager.exportProductApi(getNovaMetadata(), format, versionId, productId, downloadType).getBytes());

            assertEquals(new String(expectedValue), returnValue);
            verify(apiManagerService).downloadProductApi(productId, versionId, format, downloadType);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError()))
                    .when(apiManagerService)
                    .downloadProductApi(productId, versionId, format, downloadType);

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.exportProductApi(getNovaMetadata(), format, versionId, productId, downloadType));
            verify(apiManagerService).downloadProductApi(productId, versionId, format, downloadType);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(apiManagerService).downloadProductApi(productId, versionId, format, downloadType);

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.exportProductApi(getNovaMetadata(), format, versionId, productId, downloadType));
            verify(apiManagerService).downloadProductApi(productId, versionId, format, downloadType);
        }
    }

    @Nested
    class GetApiDetail
    {
        Integer apiId = 2408;
        Integer productId = 909;

        @Test
        void ok() throws Exception
        {
            ApiDetailDto apiDetailDto = buildApiDetailDto();

            // When
            when(apiManagerService.getApiDetail(apiId, productId)).thenReturn(apiDetailDto);
            ApiDetailDto returnValue = listenerApimanager.getApiDetail(getNovaMetadata(), productId, apiId);

            assertEquals(apiDetailDto, returnValue);
            verify(apiManagerService).getApiDetail(apiId, productId);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError()))
                    .when(apiManagerService)
                    .getApiDetail(apiId, productId);

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.getApiDetail(getNovaMetadata(), productId, apiId));
            verify(apiManagerService).getApiDetail(apiId, productId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(apiManagerService).getApiDetail(apiId, productId);

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.getApiDetail(getNovaMetadata(), productId, apiId));
            verify(apiManagerService).getApiDetail(apiId, productId);
        }

        private ApiDetailDto buildApiDetailDto()
        {
            ApiDetailDto apiDetailDto = new ApiDetailDto();
            apiDetailDto.fillRandomly(4, false, 1, 3);
            return apiDetailDto;
        }
    }

    @Nested
    class GetApiVersionDetail
    {
        Integer apiVersionId = 2408;
        Integer productId = 909;

        @Test
        void ok() throws Exception
        {
            ApiVersionDetailDto apiVersionDetailDto = buildApiVersionDetailDto();

            // When
            when(apiManagerService.getApiVersionDetail(apiVersionId, productId,"all")).thenReturn(apiVersionDetailDto);
            ApiVersionDetailDto returnValue = listenerApimanager.getApiVersionDetail(getNovaMetadata(), productId, apiVersionId,"all");

            assertEquals(apiVersionDetailDto, returnValue);
            verify(apiManagerService).getApiVersionDetail(apiVersionId, productId,"all");
            //----------------------------------
            // When
            when(apiManagerService.getApiVersionDetail(apiVersionId, productId,"deployed")).thenReturn(apiVersionDetailDto);
            ApiVersionDetailDto returnValuedeployed = listenerApimanager.getApiVersionDetail(getNovaMetadata(), productId, apiVersionId,"deployed");

            assertEquals(apiVersionDetailDto, returnValuedeployed);
            verify(apiManagerService).getApiVersionDetail(apiVersionId, productId,"deployed");
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError()))
                    .when(apiManagerService)
                    .getApiVersionDetail(apiVersionId, productId,"all");

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.getApiVersionDetail(getNovaMetadata(), productId, apiVersionId,"all"));
            verify(apiManagerService).getApiVersionDetail(apiVersionId, productId,"all");
            //--------------------------
            doThrow(new NovaException(ApiManagerError.getUnexpectedError()))
                    .when(apiManagerService)
                    .getApiVersionDetail(apiVersionId, productId,"deployed");
            // When
            assertThrows(NovaException.class, () -> listenerApimanager.getApiVersionDetail(getNovaMetadata(), productId, apiVersionId,"deployed"));
            verify(apiManagerService).getApiVersionDetail(apiVersionId, productId,"deployed");
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(apiManagerService).getApiVersionDetail(apiVersionId, productId,"all");

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.getApiVersionDetail(getNovaMetadata(), productId, apiVersionId,"all"));
            verify(apiManagerService).getApiVersionDetail(apiVersionId, productId,"all");
        //-----------------------------
            doThrow(RuntimeException.class).when(apiManagerService).getApiVersionDetail(apiVersionId, productId,"deployed");

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.getApiVersionDetail(getNovaMetadata(), productId, apiVersionId,"deployed"));
            verify(apiManagerService).getApiVersionDetail(apiVersionId, productId,"deployed");
        }

        private ApiVersionDetailDto buildApiVersionDetailDto()
        {
            ApiVersionDetailDto apiVersionDetailDto = new ApiVersionDetailDto();
            apiVersionDetailDto.fillRandomly(4, false, 1, 3);
            return apiVersionDetailDto;
        }
    }

    @Nested
    class CreatePolicyTask
    {

        @Test
        void ok() throws Exception
        {

            TaskInfoDto taskInfoDto = buildTaskInfoDto();
            listenerApimanager.createPolicyTask(getNovaMetadata(), taskInfoDto);

            verify(apiManagerService).createApiTask(taskInfoDto);
        }

        @Test
        void novaException()
        {
            TaskInfoDto taskInfoDto = buildTaskInfoDto();
            doThrow(new NovaException(ApiManagerError.getUnexpectedError())).when(apiManagerService).createApiTask(any());

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.createPolicyTask(getNovaMetadata(), taskInfoDto));
            verify(apiManagerService).createApiTask(taskInfoDto);
        }

        @Test
        void runtimeException()
        {
            TaskInfoDto taskInfoDto = buildTaskInfoDto();
            doThrow(RuntimeException.class).when(apiManagerService).createApiTask(any());

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.createPolicyTask(getNovaMetadata(), taskInfoDto));
            verify(apiManagerService).createApiTask(taskInfoDto);
        }

        private TaskInfoDto buildTaskInfoDto()
        {
            TaskInfoDto taskInfoDto = new TaskInfoDto();
            taskInfoDto.fillRandomly(4, false, 1, 3);
            return taskInfoDto;
        }

    }

    @Nested
    class OnPolicyTaskReply
    {
        String apiName = "apiName";
        String basePath = "/";
        String uuaa = "JGMV";
        String taskStatus = "DONE";
        Integer productId = 1;

        @Test
        void ok() throws Exception
        {
            PolicyTaskReplyParametersDTO parametersDTO = new PolicyTaskReplyParametersDTO();
            parametersDTO.setTaskStatus(taskStatus);
            parametersDTO.setUuaa(uuaa);
            parametersDTO.setApiName(apiName);
            parametersDTO.setBasePath(basePath);
            parametersDTO.setProductId(productId);
            parametersDTO.setApiTaskId(1);

            listenerApimanager.onPolicyTaskReply(getNovaMetadata(), parametersDTO);

            verify(apiManagerService).onPolicyTaskReply(parametersDTO);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError())).when(apiManagerService).onPolicyTaskReply(any(PolicyTaskReplyParametersDTO.class));

            PolicyTaskReplyParametersDTO parametersDTO = new PolicyTaskReplyParametersDTO();
            parametersDTO.setTaskStatus(taskStatus);
            parametersDTO.setUuaa(uuaa);
            parametersDTO.setApiName(apiName);
            parametersDTO.setBasePath(basePath);
            parametersDTO.setProductId(productId);
            parametersDTO.setApiTaskId(1);
            // When
            assertThrows(NovaException.class, () -> listenerApimanager.onPolicyTaskReply(getNovaMetadata(), parametersDTO));
            verify(apiManagerService).onPolicyTaskReply(parametersDTO);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(apiManagerService).onPolicyTaskReply(any(PolicyTaskReplyParametersDTO.class));

            // When
            PolicyTaskReplyParametersDTO parametersDTO = new PolicyTaskReplyParametersDTO();
            parametersDTO.setTaskStatus(taskStatus);
            parametersDTO.setUuaa(uuaa);
            parametersDTO.setApiName(apiName);
            parametersDTO.setBasePath(basePath);
            parametersDTO.setProductId(productId);
            parametersDTO.setApiTaskId(1);
            assertThrows(RuntimeException.class, () -> listenerApimanager.onPolicyTaskReply(getNovaMetadata(), parametersDTO));
            verify(apiManagerService).onPolicyTaskReply(parametersDTO);
        }

    }

    @Nested
    class GetApiTypes
    {

        @Test
        void ok() throws Exception
        {
            String[] apiTypes = new String[]{"tipo1", "tipo2"};

            when(apiManagerService.getApiTypes()).thenReturn(apiTypes);
            String[] returnValue = listenerApimanager.getApiTypes(getNovaMetadata());

            verify(apiManagerService).getApiTypes();
            assertArrayEquals(apiTypes, returnValue);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError())).when(apiManagerService).getApiTypes();

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.getApiTypes(getNovaMetadata()));
            verify(apiManagerService).getApiTypes();
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(apiManagerService).getApiTypes();

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.getApiTypes(getNovaMetadata()));
            verify(apiManagerService).getApiTypes();
        }
    }

    @Nested
    class SavePlanApiProfile
    {

        Integer planId = 909;
        ApiMethodProfileDto[] apiMethodProfileDtos = buildApiMethodProfileDtoArray();

        @Test
        void ok() throws Exception
        {

            listenerApimanager.savePlanApiProfile(getNovaMetadata(), apiMethodProfileDtos, planId);

            verify(apiManagerService).savePlanApiProfile(apiMethodProfileDtos, planId);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(ApiManagerError.getUnexpectedError())).when(apiManagerService).savePlanApiProfile(any(), any());

            // When
            assertThrows(NovaException.class, () -> listenerApimanager.savePlanApiProfile(getNovaMetadata(), apiMethodProfileDtos, planId));
            verify(apiManagerService).savePlanApiProfile(apiMethodProfileDtos, planId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(apiManagerService).savePlanApiProfile(any(), any());

            // When
            assertThrows(RuntimeException.class, () -> listenerApimanager.savePlanApiProfile(getNovaMetadata(), apiMethodProfileDtos, planId));
            verify(apiManagerService).savePlanApiProfile(apiMethodProfileDtos, planId);
        }

        private ApiMethodProfileDto[] buildApiMethodProfileDtoArray()
        {
            ApiMethodProfileDto apiMethodProfileDto = new ApiMethodProfileDto();
            apiMethodProfileDto.fillRandomly(4, false, 1, 2);
            return new ApiMethodProfileDto[]{apiMethodProfileDto};
        }

    }

}