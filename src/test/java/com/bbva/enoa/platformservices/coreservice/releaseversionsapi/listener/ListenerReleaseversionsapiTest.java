package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.listener;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Test for Listener
 */
public class ListenerReleaseversionsapiTest
{
    /**
     *
     */
    private static final String IV_USER = "XE00000";

    private final NovaMetadata metadata = new NovaMetadata();
    @Mock
    private IReleaseVersionService iReleaseVersionService;
    @InjectMocks
    private ListenerReleaseversionsapi listenerReleaseversionsapi;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", List.of(IV_USER));
        metadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
    }

    @Test
    public void addReleaseVersion() throws Errors
    {
        int id = RandomUtils.nextInt(1, 1000);
        RVReleaseVersionDTO dto = new RVReleaseVersionDTO();
        dto.setId(id);
        this.listenerReleaseversionsapi.addReleaseVersion(metadata, dto, id);
        verify(this.iReleaseVersionService, only()).addReleaseVersion(dto, IV_USER, id);
    }

    @Test
    public void addReleaseVersionError() throws Errors
    {
        RVReleaseVersionDTO dto = new RVReleaseVersionDTO();
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).addReleaseVersion(dto, IV_USER, 1);
        Assertions.assertThrows(NovaException.class, () -> this.listenerReleaseversionsapi.addReleaseVersion(metadata, dto, 1));
    }


    @Test
    public void subsystemBuildStatus() throws Errors
    {

        int subsystemId = RandomUtils.nextInt(1, 1000);
        String jobName = "job-" + RandomStringUtils.randomAlphabetic(10);
        String group = "group-" + RandomStringUtils.randomAlphabetic(10);
        String status = "Status";
        this.listenerReleaseversionsapi.subsystemBuildStatus(metadata, subsystemId, jobName, group, status);
        verify(this.iReleaseVersionService, only()).subsystemBuildStatus(IV_USER, subsystemId, jobName, group, status);
    }

    @Test
    public void subsystemBuildStatusError() throws Errors
    {
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).subsystemBuildStatus(IV_USER, 1, "jobName", "group", "Status");
        Assertions.assertThrows(NovaException.class, () ->
                this.listenerReleaseversionsapi.subsystemBuildStatus(metadata, 1, "jobName", "group", "Status"));
    }

    @Test
    public void archiveReleaseVersion() throws Errors
    {
        int versionId = RandomUtils.nextInt(1, 1000);
        this.listenerReleaseversionsapi.archiveReleaseVersion(metadata, versionId);
        verify(this.iReleaseVersionService, only()).archiveReleaseVersion(IV_USER, versionId);
    }

    @Test
    public void archiveReleaseVersionError() throws Errors
    {
        int versionId = -1;
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).archiveReleaseVersion(IV_USER, versionId);
        Assertions.assertThrows(NovaException.class, () -> this.listenerReleaseversionsapi.archiveReleaseVersion(metadata, versionId));
    }

    @Test
    public void getReleaseVersion() throws Errors
    {
        int id = RandomUtils.nextInt(1, 1000);
        RVReleaseVersionDTO dto = new RVReleaseVersionDTO();
        dto.setId(id);
        when(this.iReleaseVersionService.getReleaseVersion(IV_USER, id)).thenReturn(dto);
        RVReleaseVersionDTO response = this.listenerReleaseversionsapi.getReleaseVersion(metadata, id);
        assertEquals(dto, response);
    }

    @Test
    public void getReleaseVersionError() throws Errors
    {
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).getReleaseVersion(IV_USER, 1);
        Assertions.assertThrows(NovaException.class, () ->
                this.listenerReleaseversionsapi.getReleaseVersion(metadata, 1));
    }

    @Test
    public void updateReleaseVersion() throws Errors
    {
        int id = RandomUtils.nextInt(1, 1000);
        String description = "description-" + RandomStringUtils.randomAlphabetic(10);
        RVReleaseVersionDTO releaseVersionDto = new RVReleaseVersionDTO();
        releaseVersionDto.setId(id);
        releaseVersionDto.setDescription(description);
        when(this.iReleaseVersionService.updateReleaseVersion(IV_USER, 1, description)).thenReturn(releaseVersionDto);
        RVReleaseVersionDTO response = this.listenerReleaseversionsapi.updateReleaseVersion(metadata, 1, description);
        assertEquals(releaseVersionDto, response);
    }

    @Test
    public void updateReleaseVersionError() throws Errors
    {
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).updateReleaseVersion(IV_USER, 1, "Desc");
        Assertions.assertThrows(NovaException.class, () -> this.listenerReleaseversionsapi.updateReleaseVersion(metadata, 1, "Desc"));
    }


    @Test
    public void deleteReleaseVersion() throws Errors
    {
        int id = RandomUtils.nextInt(1, 1000);
        this.listenerReleaseversionsapi.deleteReleaseVersion(metadata, id);
        verify(this.iReleaseVersionService, only()).deleteReleaseVersion(IV_USER, id);
    }

    @Test
    public void deleteReleaseVersionError() throws Errors
    {
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).deleteReleaseVersion(IV_USER, 1);
        Assertions.assertThrows(NovaException.class, () -> this.listenerReleaseversionsapi.deleteReleaseVersion(metadata, 1));
    }

    @Test
    public void updateReleaseVersionIssue() throws Errors
    {
        int versionId = RandomUtils.nextInt(1, 1000);
        String issueId = RandomStringUtils.randomAlphabetic(10);
        this.listenerReleaseversionsapi.updateReleaseVersionIssue(metadata, versionId, issueId);
        verify(this.iReleaseVersionService, only()).updateReleaseVersionIssue(IV_USER, versionId, issueId);
    }

    @Test
    public void updateReleaseVersionIssueError() throws Errors
    {
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).updateReleaseVersionIssue(IV_USER, 1, "KEY");
        Assertions.assertThrows(NovaException.class, () -> this.listenerReleaseversionsapi.updateReleaseVersionIssue(metadata, 1, "KEY"));
    }

    @Test
    public void newReleaseVersion() throws Errors
    {
        int releaseId = RandomUtils.nextInt(1, 1000);
        NewReleaseVersionDto newReleaseVersionDto = new NewReleaseVersionDto();
        when(this.iReleaseVersionService.newReleaseVersion(IV_USER, releaseId)).thenReturn(newReleaseVersionDto);
        NewReleaseVersionDto response = this.listenerReleaseversionsapi.newReleaseVersion(metadata, releaseId);
        assertEquals(newReleaseVersionDto, response);
    }

    @Test
    public void newReleaseVersionError() throws Errors
    {
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).newReleaseVersion(IV_USER, 1);
        Assertions.assertThrows(NovaException.class, () -> this.listenerReleaseversionsapi.newReleaseVersion(metadata, 1));
    }

    @Test
    void createReleaseVersion() throws Errors
    {
        int releaseId = RandomUtils.nextInt(1, 1000);
        RVVersionDTO releaseVersion = new RVVersionDTO();
        releaseVersion.setId(releaseId);
        listenerReleaseversionsapi.createReleaseVersion(metadata, releaseVersion, releaseId);
        verify(this.iReleaseVersionService, only()).createReleaseVersion(releaseVersion, IV_USER, releaseId);
    }

    @Test
    void createReleaseVersionError() throws Errors
    {
        int releaseId =-1;
        RVVersionDTO releaseVersion = new RVVersionDTO();
        releaseVersion.setId(releaseId);
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).createReleaseVersion(releaseVersion, IV_USER, releaseId);
        Assertions.assertThrows(NovaException.class, () -> this.listenerReleaseversionsapi.createReleaseVersion(metadata, releaseVersion, releaseId));
    }

    @Test
    void getReleaseVersionSubsystem() throws Errors
    {
        int subsystemId = RandomUtils.nextInt(1, 1000);
        RVReleaseVersionSubsystemDTO[] expected = new  RVReleaseVersionSubsystemDTO[0];
        when(iReleaseVersionService.getReleaseVersionSubsystems(subsystemId)).thenReturn(expected);
        RVReleaseVersionSubsystemDTO[] current = listenerReleaseversionsapi.getReleaseVersionSubsystem(metadata,subsystemId);
        verify(this.iReleaseVersionService, only()).getReleaseVersionSubsystems(subsystemId);
        assertEquals(expected, current);
    }

    @Test
    void getReleaseVersionSubsystemError() throws Errors
    {
        int subsystemId = -1;
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError())).when(this.iReleaseVersionService).getReleaseVersionSubsystems(subsystemId);
        Assertions.assertThrows(NovaException.class, () -> this.listenerReleaseversionsapi.getReleaseVersionSubsystem(metadata,subsystemId));
    }

    @Test
    void releaseVersionRequest() throws Errors
    {
        int releaseVersionId = RandomUtils.nextInt(1, 1000);
        RVRequestDTO expected = new RVRequestDTO();

        when(iReleaseVersionService.releaseVersionRequest(IV_USER, releaseVersionId)).thenReturn(expected);
        RVRequestDTO current = listenerReleaseversionsapi.releaseVersionRequest(metadata,releaseVersionId);
        verify(this.iReleaseVersionService, only()).releaseVersionRequest(IV_USER, releaseVersionId);
        assertEquals(expected, current);
    }

    @Test
    void validateTags() throws Errors
    {
        int releaseId = RandomUtils.nextInt(1, 1000);
        RVValidationDTO rvValidationDTO = new RVValidationDTO();
        RVValidationResponseDTO expected = new RVValidationResponseDTO();

        when(iReleaseVersionService.validateAllTags(IV_USER, releaseId, rvValidationDTO)).thenReturn(expected);

        RVValidationResponseDTO current = listenerReleaseversionsapi.validateTags(metadata,rvValidationDTO, releaseId);
        verify(this.iReleaseVersionService, only()).validateAllTags(IV_USER, releaseId, rvValidationDTO);
        assertEquals(expected, current);
    }

    @Test
    void getReleaseVersionsStatuses() throws Errors
    {
        String[] expected = new String[]{"Status.1", "Status.2"};
        when(iReleaseVersionService.getStatuses()).thenReturn(expected);
        String[]  current = listenerReleaseversionsapi.getReleaseVersionsStatuses(metadata);
        verify(this.iReleaseVersionService, only()).getStatuses();
        assertArrayEquals(expected, current);
    }
}
