package com.bbva.enoa.platformservices.coreservice.common.model.param;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@ToString
@EqualsAndHashCode
public class ServiceOperationParams
{
    private String modulePath;
    private String tag;
    private Integer versionControlServiceId;
    private String ivUser;
    private String serviceType;
    private Integer releaseVersionServiceId;
    private String releaseVersionServiceName;
    private List<LMUsedLibrariesDTO> libraries;
    private Map<String, Map<String, String>> extraProperties;

    private ServiceOperationParams()
    {
    }

    public static class ServiceOperationParamsBuilder
    {
        private String modulePath;
        private String tag;
        private Integer versionControlServiceId;
        private String ivUser;
        private String serviceType;
        private Integer releaseVersionServiceId;
        private String releaseVersionServiceName;
        private List<LMUsedLibrariesDTO> libraries;
        private Map<String, Map<String, String>> extraProperties;

        public ServiceOperationParamsBuilder modulePath(String modulePath)
        {
            this.modulePath = modulePath;
            return this;
        }

        public ServiceOperationParamsBuilder tag(String tag)
        {
            this.tag = tag;
            return this;
        }

        public ServiceOperationParamsBuilder versionControlServiceId(Integer versionControlServiceId)
        {
            this.versionControlServiceId = versionControlServiceId;
            return this;
        }

        public ServiceOperationParamsBuilder ivUser(String ivUser)
        {
            this.ivUser = ivUser;
            return this;
        }

        public ServiceOperationParamsBuilder serviceType(String serviceType)
        {
            this.serviceType = serviceType;
            return this;
        }

        public ServiceOperationParamsBuilder releaseVersionServiceId(Integer releaseVersionServiceId)
        {
            this.releaseVersionServiceId = releaseVersionServiceId;
            return this;
        }

        public ServiceOperationParamsBuilder releaseVersionServiceName(String releaseVersionServiceName)
        {
            this.releaseVersionServiceName = releaseVersionServiceName;
            return this;
        }

        public ServiceOperationParamsBuilder libraries(List<LMUsedLibrariesDTO> libraries)
        {
            this.libraries = new ArrayList<>(libraries);
            return this;
        }

        public ServiceOperationParamsBuilder extraProperties(Map<String, Map<String, String>> extraProperties)
        {
            this.extraProperties = extraProperties;
            return this;
        }

        public ServiceOperationParams build()
        {
            ServiceOperationParams params = new ServiceOperationParams();
            params.modulePath = this.modulePath;
            params.tag = this.tag;
            params.versionControlServiceId = this.versionControlServiceId;
            params.ivUser = this.ivUser;
            params.serviceType = this.serviceType;
            params.releaseVersionServiceId = this.releaseVersionServiceId;
            params.releaseVersionServiceName = this.releaseVersionServiceName;
            params.libraries = this.libraries;
            params.extraProperties = this.extraProperties;
            return params;
        }
    }
}
