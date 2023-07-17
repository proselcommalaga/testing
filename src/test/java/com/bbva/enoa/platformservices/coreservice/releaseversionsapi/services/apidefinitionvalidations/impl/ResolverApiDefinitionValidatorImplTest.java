package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.interfaces.IApiDefinitionValidator;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResolverApiDefinitionValidatorImplTest
{
	private Executor asyncExecutor = Runnable::run;

	@Mock
	private IApiDefinitionValidator theValidator;

	private List<IApiDefinitionValidator> validators = new ArrayList<>();

	@InjectMocks
	private ResolverApiDefinitionValidatorImpl resolverApiDefinitionValidator;

	@BeforeEach
	public void setUp() throws Exception
	{
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(resolverApiDefinitionValidator, "validators", validators);
		ReflectionTestUtils.setField(resolverApiDefinitionValidator, "asyncExecutor", asyncExecutor);
	}


	@Test
	public void isValidatorTest() {
		Assert.assertFalse(resolverApiDefinitionValidator.isValidator());
	}

	@Test
	public void validateAndAssociateApiTest() {
		NewReleaseVersionServiceDto newReleaseVersionServiceDto = new NewReleaseVersionServiceDto();
		newReleaseVersionServiceDto.setServiceName("myService");
		NovaYml novaYml = new NovaYml();
		List<ValidationErrorDto> expected = new ArrayList<>();
		expected.add(mock(ValidationErrorDto.class));

		this.validators.add(this.theValidator);
		when(this.theValidator.validateAndAssociateApi(eq(novaYml), eq(newReleaseVersionServiceDto), eq(0), eq(""), eq(""))).thenReturn(expected);

		List<ValidationErrorDto> result = resolverApiDefinitionValidator.
				validateAndAssociateApi(novaYml, newReleaseVersionServiceDto, 0, "", "");
		Assert.assertEquals("validateApiTest: Results are not equals", expected, result);
	}

	@Test
	public void validateAndAssociateApiTestWithError() {
		NewReleaseVersionServiceDto newReleaseVersionServiceDto = new NewReleaseVersionServiceDto();
		newReleaseVersionServiceDto.setServiceName("myService");
		NovaYml novaYml = new NovaYml();
		List<ValidationErrorDto> expected = new ArrayList<>();
		expected.add(mock(ValidationErrorDto.class));

		this.validators.add(this.theValidator);
		when(this.theValidator.validateAndAssociateApi(eq(novaYml), eq(newReleaseVersionServiceDto), eq(0), eq(""), eq(""))).thenThrow(new RuntimeException());

		assertThrows(NovaException.class, () -> resolverApiDefinitionValidator.
				validateAndAssociateApi(novaYml, newReleaseVersionServiceDto, 0, "", "")
		);

	}
}
