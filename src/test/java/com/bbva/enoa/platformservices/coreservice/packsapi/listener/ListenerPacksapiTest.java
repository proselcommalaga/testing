package com.bbva.enoa.platformservices.coreservice.packsapi.listener;

import com.bbva.enoa.apirestgen.packsapi.model.BrokerPackDto;
import com.bbva.enoa.apirestgen.packsapi.model.FilesystemPackDto;
import com.bbva.enoa.apirestgen.packsapi.model.HardwarePackDto;
import com.bbva.enoa.platformservices.coreservice.packsapi.services.interfaces.IPacksService;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListenerPacksapiTest
{

    @Mock
	private NovaMetadata novaMetadata;
    @Mock
	private IPacksService packsService;

    @InjectMocks
    private ListenerPacksapi listenerPacksapi;

	@BeforeEach
	public void setUp()
	{
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getOneHardwarePack() throws Exception
	{
		//WHEN
		int id = RandomUtils.nextInt(1, 10);
		when(this.packsService.getOneHardwarePack(id)).thenReturn(new HardwarePackDto());

		//THEN
		this.listenerPacksapi.getOneHardwarePack(novaMetadata, id);

		//VERIFY
		verify(this.packsService).getOneHardwarePack(id);
	}

    @Test
    public void getFilesystemPacks() throws Exception
    {
    	//WHEN
		String filesystemType = RandomStringUtils.randomAlphabetic(10);
        when(this.packsService.getFilesystemPacks(filesystemType)).thenReturn(new FilesystemPackDto[0]);

        //THEN
        this.listenerPacksapi.getFilesystemPacks(novaMetadata, filesystemType);

        //VERIFY
		verify(this.packsService).getFilesystemPacks(filesystemType);
    }

    @Test
    public void getOneFilesystemPack() throws Exception
    {
		//WHEN
		int id = RandomUtils.nextInt(1, 10);
		when(this.packsService.getOneFilesystemPack(id)).thenReturn(new FilesystemPackDto());

		//THEN
		this.listenerPacksapi.getOneFilesystemPack(novaMetadata, id);

		//VERIFY
		verify(this.packsService).getOneFilesystemPack(id);
    }

	@Test
	public void getHardwarePacks() throws Exception
	{
		//WHEN
		String hardwarePackType = RandomStringUtils.randomAlphabetic(10);
		when(this.packsService.getHardwarePacks("11.0.11", hardwarePackType)).thenReturn(new HardwarePackDto[0]);

		//THEN
		this.listenerPacksapi.getHardwarePacks(novaMetadata, false, hardwarePackType, "11.0.11");

		//VERIFY
		verify(this.packsService).getHardwarePacks("11.0.11", hardwarePackType);
	}

	@Test
	public void getOneBrokerPack() throws Exception
	{
		//WHEN
		int id = RandomUtils.nextInt(1, 10);
		when(this.packsService.getOneBrokerPack(id)).thenReturn(new BrokerPackDto());

		//THEN
		this.listenerPacksapi.getOneBrokerPack(novaMetadata, id);

		//VERIFY
		verify(this.packsService).getOneBrokerPack(id);
	}

	@Test
	public void getBrokerPacks() throws Exception
	{
		//WHEN
		String brokerPackType = RandomStringUtils.randomAlphabetic(10);
		when(this.packsService.getBrokerPacks(brokerPackType)).thenReturn(new BrokerPackDto[0]);

		//THEN
		this.listenerPacksapi.getBrokerPacks(novaMetadata, brokerPackType);

		//VERIFY
		verify(this.packsService).getBrokerPacks(brokerPackType);
	}
}
