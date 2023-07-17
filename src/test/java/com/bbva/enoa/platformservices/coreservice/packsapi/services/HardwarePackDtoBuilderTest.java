package com.bbva.enoa.platformservices.coreservice.packsapi.services;

import com.bbva.enoa.apirestgen.packsapi.model.HardwarePackDto;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.exceptions.BudgetsError;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class HardwarePackDtoBuilderTest
{
    @Mock
    private IBudgetsService budgetsService;
    @InjectMocks
    private HardwarePackDtoBuilder hardwarePackDtoBuilder;

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

        HardwarePack hardwarePack = new HardwarePack();
        hardwarePack.setId(id);
        hardwarePack.setCreationDate(Calendar.getInstance());
		hardwarePack.setHardwarePackType(HardwarePackType.values()[RandomUtils.nextInt(0, HardwarePackType.values().length)]);
        List<HardwarePack> packList = new ArrayList<>();
        packList.add(hardwarePack);

        when(this.budgetsService.getHardwarePackPrice(id)).thenReturn(fsPrice);

        //THEN
        HardwarePackDto[] response = this.hardwarePackDtoBuilder.build(packList);

        //VERIFY
        assertEquals(id, response[0].getId().intValue());
		Assert.assertEquals(fsPrice, response[0].getPrice().doubleValue(), 0);
    }

	@Test
	public void buildNovaException()
	{
		//WHEN
		Integer id = RandomUtils.nextInt(1, 10);

		HardwarePack hardwarePack = new HardwarePack();
		hardwarePack.setId(id);
		hardwarePack.setCreationDate(Calendar.getInstance());
		hardwarePack.setHardwarePackType(HardwarePackType.values()[RandomUtils.nextInt(0, HardwarePackType.values().length)]);
		List<HardwarePack> packList = new ArrayList<>();
		packList.add(hardwarePack);

		doThrow(new NovaException(BudgetsError.getUnexpectedError(), "")).when(this.budgetsService).getHardwarePackPrice(id);

		//THEN
		assertThrows(NovaException.class, () ->
			this.hardwarePackDtoBuilder.build(packList)
		);

	}

}
