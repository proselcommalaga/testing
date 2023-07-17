package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import org.apache.commons.collections.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NovaYmlAsyncApi
{

	/**
	 * Private Constructor
	 */
	private NovaYmlAsyncApi() {	}

	/**
	 * Private Constructor
	 */
	private NovaYmlAsyncApi(List<String> asyncApis) {
		this.asyncApis = new HashSet<>(asyncApis);
	}

	/**
	 * API
	 */
	private Set<String> asyncApis;

	/**
	 * Get asyncApi APIs
	 *
	 * @return asyncApi apis
	 */
	public Set<String> getAsyncApis()
	{
		return Set.copyOf(this.asyncApis);
	}

	/**
	 * Test if there are NOT definitions
	 * @return true if no definitions
	 */
	public boolean isEmpty()
	{
		return CollectionUtils.isEmpty(asyncApis);
	}

	/**
	 * Test if there are any definition
	 * @return true if there are definitions
	 */
	public boolean isNotEmpty()
	{
		return CollectionUtils.isNotEmpty(asyncApis);
	}

	/**
	 * Factory method
	 * @param asyncApi asyncApi definition
	 * @return NovaYmlAsyncApi instance
	 */
	public static NovaYmlAsyncApi getInstance(List<String> asyncApi)
	{
		return new NovaYmlAsyncApi(asyncApi);
	}

	@Override
	public String toString()
	{
		return String.join(", ", asyncApis);
	}
}
