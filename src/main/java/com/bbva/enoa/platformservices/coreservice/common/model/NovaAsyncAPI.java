package com.bbva.enoa.platformservices.coreservice.common.model;

import com.asyncapi.v2.model.AsyncAPI;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;

@NoArgsConstructor

@AllArgsConstructor
@Data
public class NovaAsyncAPI
{
	@Nonnull
	@NonNull
	private AsyncAPI asyncAPI;

	@Nonnull
	@NonNull
	private String xBusinessUnit;

	@JsonProperty("info")
	private void getXBusinessUnitFromInfo(Map<String, Object> info) {
		xBusinessUnit = Objects.toString(info.get("x-business-unit"), StringUtils.EMPTY);
	}
}
