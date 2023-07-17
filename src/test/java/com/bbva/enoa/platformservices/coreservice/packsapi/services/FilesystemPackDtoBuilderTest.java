package com.bbva.enoa.platformservices.coreservice.packsapi.services;

import com.bbva.enoa.apirestgen.packsapi.model.FilesystemPackDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.exceptions.BudgetsError;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class FilesystemPackDtoBuilderTest
{
    @Mock
    private IBudgetsService budgetsService;
    @InjectMocks
    private FilesystemPackDtoBuilder filesystemPackDtoBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void build()
    {
    	//WHEN
		Integer id = RandomUtils.nextInt(1, 10);
		double fsPrice = RandomUtils.nextDouble(1, 10);

        FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setId(id);
        filesystemPack.setCreationDate(Calendar.getInstance());

		filesystemPack.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
		filesystemPack.setFilesystemType(FilesystemType.values()[RandomUtils.nextInt(0, FilesystemType.values().length)]);
        List<FilesystemPack> packList = new ArrayList<>();
        packList.add(filesystemPack);

        when(this.budgetsService.getFilesystemPackPrice(id)).thenReturn(fsPrice);

        //THEN
		FilesystemPackDto[] response = this.filesystemPackDtoBuilder.build(packList);

		//VERIFY
		assertEquals(id.intValue(), response[0].getId().intValue());
		assertEquals(fsPrice, response[0].getPrice().doubleValue(), 0);
    }

	@Test
	public void buildNovaException()
	{
		//WHEN
		Integer id = RandomUtils.nextInt(1, 10);
		double fsPrice = RandomUtils.nextDouble(1, 10);

		FilesystemPack filesystemPack = new FilesystemPack();
		filesystemPack.setId(id);
		filesystemPack.setCreationDate(Calendar.getInstance());

		filesystemPack.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
		filesystemPack.setFilesystemType(FilesystemType.values()[RandomUtils.nextInt(0, FilesystemType.values().length)]);
		List<FilesystemPack> packList = new ArrayList<>();
		packList.add(filesystemPack);

		doThrow(new NovaException(BudgetsError.getUnexpectedError(), "")).when(this.budgetsService).getFilesystemPackPrice(id);

		//THEN
		assertThrows(NovaException.class, () ->
			this.filesystemPackDtoBuilder.build(packList)
		);
	}

}