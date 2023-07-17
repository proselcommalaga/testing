package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVConfigurableFilesystem;
import com.bbva.enoa.datamodel.model.behavior.entities.BSConfigurationFilesystem;
import com.bbva.enoa.datamodel.model.behavior.entities.BSConfigurationFilesystemId;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemEther;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl.BehaviorServiceConfigurationServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

@ExtendWith(MockitoExtension.class)
class BehaviorServiceConfigurationServiceImplTest
{
    @InjectMocks
    private BehaviorServiceConfigurationServiceImpl service;

    @Test
    void mergeListTest_oneExistingFileSystemReceivedEmptyArray_removeAllFileSystems() {
        BehaviorServiceConfiguration behaviorServiceConfiguration = new BehaviorServiceConfiguration();
        behaviorServiceConfiguration.setId(60);
        List<BSConfigurationFilesystem> bsConfigurationFilesystemList = new ArrayList<>();
        FilesystemEther filesystem = new FilesystemEther();
        filesystem.setId(22);
        BSConfigurationFilesystem bsConfigurationFilesystem =
                new BSConfigurationFilesystem(new BSConfigurationFilesystemId(behaviorServiceConfiguration.getId(), filesystem.getId()),
                        behaviorServiceConfiguration, filesystem, "/etc");
        bsConfigurationFilesystemList.add(bsConfigurationFilesystem);
        behaviorServiceConfiguration.setBsConfigurationFilesystemList(bsConfigurationFilesystemList);

        List<BSConfigurationFilesystem> configurationFilesystemList =
                service.mergeFileSystems(behaviorServiceConfiguration, Collections.emptyList(), new BVConfigurableFilesystem[0]);

        assertThat(configurationFilesystemList.size(), equalTo(0));
    }

    @Test
    void mergeListTest_oneExistingFileSystemSameFilesystemReceived_updateVolumeBind() {
        BehaviorServiceConfiguration behaviorServiceConfiguration = new BehaviorServiceConfiguration();
        behaviorServiceConfiguration.setId(60);
        List<BSConfigurationFilesystem> bsConfigurationFilesystemList = new ArrayList<>();
        FilesystemEther filesystem = new FilesystemEther();
        filesystem.setId(22);
        BSConfigurationFilesystem bsConfigurationFilesystem =
                new BSConfigurationFilesystem(new BSConfigurationFilesystemId(behaviorServiceConfiguration.getId(), filesystem.getId()),
                        behaviorServiceConfiguration, filesystem, "/etc");
        bsConfigurationFilesystemList.add(bsConfigurationFilesystem);
        behaviorServiceConfiguration.setBsConfigurationFilesystemList(bsConfigurationFilesystemList);

        BVConfigurableFilesystem configurableFilesystem = new BVConfigurableFilesystem();
        configurableFilesystem.setId(22);
        configurableFilesystem.setName("Filesystem X");
        configurableFilesystem.setVolumeBind("/temp");
        BVConfigurableFilesystem[] resourceDtoArray = new BVConfigurableFilesystem[] { configurableFilesystem };
        List<Filesystem> filesystemList = new ArrayList<>();
        filesystemList.add(filesystem);

        List<BSConfigurationFilesystem> configurationFilesystemList = service.mergeFileSystems(behaviorServiceConfiguration, filesystemList, resourceDtoArray);

        assertThat(configurationFilesystemList.size(), equalTo(1));
        assertThat(configurationFilesystemList.get(0).getBehaviorServiceConfiguration(), equalTo(behaviorServiceConfiguration));
        assertThat(configurationFilesystemList.get(0).getFilesystem(), equalTo(filesystem));
        assertThat(configurationFilesystemList.get(0).getVolumeBind(), equalTo(configurableFilesystem.getVolumeBind()));
    }

    @Test
    void mergeListTest_noFileSystemSavedOneFilesystemReceived_addedFileSystem() {
        BehaviorServiceConfiguration behaviorServiceConfiguration = new BehaviorServiceConfiguration();
        behaviorServiceConfiguration.setId(60);
        List<BSConfigurationFilesystem> bsConfigurationFilesystemList = new ArrayList<>();

        behaviorServiceConfiguration.setBsConfigurationFilesystemList(bsConfigurationFilesystemList);

        BVConfigurableFilesystem configurableFilesystem = new BVConfigurableFilesystem();
        configurableFilesystem.setId(22);
        configurableFilesystem.setName("Filesystem X");
        configurableFilesystem.setVolumeBind("/temp");
        BVConfigurableFilesystem[] resourceDtoArray = new BVConfigurableFilesystem[] { configurableFilesystem };

        List<Filesystem> filesystemList = new ArrayList<>();
        FilesystemEther filesystem = new FilesystemEther();
        filesystem.setId(22);
        filesystemList.add(filesystem);

        List<BSConfigurationFilesystem> configurationFilesystemList = service.mergeFileSystems(behaviorServiceConfiguration, filesystemList, resourceDtoArray);

        assertThat(configurationFilesystemList.size(), equalTo(1));
        assertThat(configurationFilesystemList.get(0).getBehaviorServiceConfiguration(), equalTo(behaviorServiceConfiguration));
        assertThat(configurationFilesystemList.get(0).getFilesystem(), equalTo(filesystem));
        assertThat(configurationFilesystemList.get(0).getVolumeBind(), equalTo(configurableFilesystem.getVolumeBind()));
    }

    @Test
    void mergeListTest_oneFileSystemSavedReceivedDifferentFilesystem_replacedFileSystems() {
        BehaviorServiceConfiguration behaviorServiceConfiguration = new BehaviorServiceConfiguration();
        behaviorServiceConfiguration.setId(60);
        List<BSConfigurationFilesystem> bsConfigurationFilesystemList = new ArrayList<>();
        FilesystemEther oldFilesystem = new FilesystemEther();
        oldFilesystem.setId(22);
        BSConfigurationFilesystem bsConfigurationFilesystem =
                new BSConfigurationFilesystem(new BSConfigurationFilesystemId(behaviorServiceConfiguration.getId(), oldFilesystem.getId()),
                        behaviorServiceConfiguration, oldFilesystem, "/etc");
        bsConfigurationFilesystemList.add(bsConfigurationFilesystem);

        behaviorServiceConfiguration.setBsConfigurationFilesystemList(bsConfigurationFilesystemList);

        BVConfigurableFilesystem configurableFilesystem = new BVConfigurableFilesystem();
        configurableFilesystem.setId(44);
        configurableFilesystem.setName("Filesystem Y");
        configurableFilesystem.setVolumeBind("/temp");
        BVConfigurableFilesystem[] resourceDtoArray = new BVConfigurableFilesystem[] { configurableFilesystem };

        List<Filesystem> filesystemList = new ArrayList<>();
        FilesystemEther newFilesystem = new FilesystemEther();
        newFilesystem.setId(44);
        filesystemList.add(newFilesystem);

        List<BSConfigurationFilesystem> configurationFilesystemList = service.mergeFileSystems(behaviorServiceConfiguration, filesystemList, resourceDtoArray);

        assertThat(configurationFilesystemList.size(), equalTo(1));
        assertThat(configurationFilesystemList.get(0).getBehaviorServiceConfiguration(), equalTo(behaviorServiceConfiguration));
        assertThat(configurationFilesystemList.get(0).getFilesystem(), equalTo(newFilesystem));
        assertThat(configurationFilesystemList.get(0).getVolumeBind(), equalTo(configurableFilesystem.getVolumeBind()));
    }
}
