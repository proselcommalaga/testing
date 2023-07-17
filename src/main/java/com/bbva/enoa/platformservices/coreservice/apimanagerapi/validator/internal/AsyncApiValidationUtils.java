package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.asyncapi.v2.model.AsyncAPI;
import com.asyncapi.v2.model.info.Contact;
import com.asyncapi.v2.model.info.Info;
import com.asyncapi.v2.model.server.Server;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.common.util.NovaCustomObjectMapperFactory;
import com.bbva.enoa.platformservices.coreservice.common.model.NovaAsyncAPI;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.common.util.ValidationUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@AllArgsConstructor
class AsyncApiValidationUtils
{
    private static final Logger LOG = LoggerFactory.getLogger(AsyncApiValidationUtils.class);
    private static final String MINOR_ASYNCAPI_SUPPORTED = "2";
    private static final Pattern INFO_TITLE_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_]+$");
    private static final Pattern INFO_VERSION_PATTERN = Pattern.compile(com.bbva.enoa.platformservices.coreservice.common.Constants.PATTERN_VERSION_REGEX);
    private final NovaCustomObjectMapperFactory novaCustomObjectMapperFactory;

    NovaAsyncAPI parseDefinitionFile(final String content) throws DefinitionFileException
    {
        try
        {
            ObjectMapper mapper = this.novaCustomObjectMapperFactory.create(new YAMLFactory());
            // Don't throw an exception when json has extra fields you are
            // not serializing on. For x- params
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            //Create NovaAsyncApi object and validate it
            NovaAsyncAPI asyncApi = mapper.readValue(content, NovaAsyncAPI.class);
            asyncApi.setAsyncAPI(mapper.readValue(content, AsyncAPI.class));
            return asyncApi;
        }
        catch (IOException e)
        {
            LOG.error("[AsyncApiValidationUtils] -> [parseDefinitionFile]: Error parsing asyncapi: ", e);
            throw new DefinitionFileException(List.of("Yaml exception: error parsing the Yaml file. The asyncapi file does not complies with Yaml format. Errors: " + e));
        }
    }

    /**
     * Validate info version
     *
     * @param asynapiVersion asynapi Version
     * @return error list
     */
    List<String> validateAsyncSpecVersion(final String asynapiVersion)
    {
        final List<String> errorList = new ArrayList<>();

        // Create a Pattern object
        Pattern r = Pattern.compile(com.bbva.enoa.platformservices.coreservice.common.Constants.PATTERN_VERSION_REGEX);
        // Now create matcher object.
        Matcher m = r.matcher(asynapiVersion);
        if (!m.find())
        {
            errorList.add("Invalid format in version: Version only can contain this "
                    + "set of characters: (0-9 .). Also its pattern should be {major}.{minor}.{fix}");
        }
        //Check the major asyncapi version, must be >= 2
        if (MINOR_ASYNCAPI_SUPPORTED.compareTo(asynapiVersion.split("\\.")[0]) > 0)
        {
            errorList.add("Invalid version: Only 2.x.x version of asyncapi is supported");
        }

        return errorList;
    }

    /**
     * Validate the INFO field
     *
     * @param novaAsyncAPI yml content
     * @param product      Nova product
     * @param apiType      Type of api
     * @return List of errors
     */
    List<String> validateInfo(final NovaAsyncAPI novaAsyncAPI, final Product product, final ApiType apiType)
    {
        final List<String> errorList = new ArrayList<>();

        Info info = novaAsyncAPI.getAsyncAPI().getInfo();
        if (Objects.isNull(info))
        {
            errorList.add("Info not found. Please add a info field with, at least, the title "
                    + "and fulfill it with the api's name, a version and a description.");
        }
        else
        {
            errorList.addAll(this.validateInfoTitle(info));

            errorList.addAll(this.validateInfoDescription(info));

            // The UUAA is included in info.x-business-unit
            // But this parameter is not suported on AsyncAPI library, so it is read into
            // the NovaAsyncAPI.xBusinessUnit atribute
            errorList.addAll(validateInfoBusinessUnit(novaAsyncAPI.getXBusinessUnit(), product));

            errorList.addAll(validateInfoVersion(info));

            if (ApiType.EXTERNAL != apiType)
            {
                errorList.addAll(validateInfoContact(info, product));
                // TODO: Pending to add a new validation to the schema uploaded.
            }else{
                errorList.addAll(validateInfoURL(info));
            }
        }

        return errorList;
    }

    /**
     * Validate info title
     *
     * @param info info
     * @return error list
     */
    private List<String> validateInfoTitle(final Info info)
    {
        final List<String> errorList = new ArrayList<>();

        if (Objects.isNull(info.getTitle()))
        {
            errorList.add("Info Title not found. Please add a title field to the info field "
                    + "and fulfill it with the api's name.");
        }
        else if (info.getTitle().length() > Constants.MAX_NAME_LENGTH)
        {
            errorList.add("Too long Info Title. Title length is larger than "
                    + Constants.MAX_NAME_LENGTH + " characters. Please do not exceed the " + "maximum length.");
        }
        else
        {
            Matcher m = INFO_TITLE_PATTERN.matcher(info.getTitle());
            if (!m.find())
            {
                errorList.add("Invalid format Info Title: only can contain this set of "
                        + "characters: a-z A-Z 0-9 - _");
            }
        }

        return errorList;
    }

    /**
     * Validate info description
     *
     * @param info info
     * @return error list
     */
    private List<String> validateInfoDescription(final Info info)
    {
        final List<String> errorList = new ArrayList<>();

        if (Objects.isNull(info.getDescription()))
        {
            errorList.add("Info Description not found. Please add a description field "
                    + "to the info field and fulfill it.");
        }
        else if (info.getDescription().length() > Constants.MAX_DESCRIPTION_LENGTH)
        {
            errorList.add("Too long Info Description: Description length is larger than "
                    + Constants.MAX_DESCRIPTION_LENGTH + " characters. Please do " + "not exceed the maximum length.");
        }

        return errorList;
    }

    /**
     * Validate x-business-unit (UUAA)
     *
     * @param xBusinessUnit xBusinessUnit	(UUAA)
     * @param product       product
     * @return error list
     */
    private List<String> validateInfoBusinessUnit(final String xBusinessUnit, final Product product)
    {
        final List<String> errorList = new ArrayList<>();

        if (Strings.isNullOrEmpty(xBusinessUnit))
        {
            errorList.add("Info business-unit (UUAA) is mandatory.");
        }
        else
        {
            if ((Strings.isNullOrEmpty(product.getUuaa()) ||
                    !product.getUuaa().equalsIgnoreCase(xBusinessUnit)))
            {
                errorList.add("Info Asyncapi business-unit do not match with the product UUAA. The business-unit "
                        + "and the product UUAA must be the same. The actual xBusinessUnit=["+xBusinessUnit+"] "
                        + "and the product UUAA=["+product.getUuaa()+"]");
            }
        }

        return errorList;
    }

    /**
     * Validate info version
     *
     * @param info info
     * @return error list
     */
    private List<String> validateInfoVersion(final Info info)
    {
        final List<String> errorList = new ArrayList<>();

        if (Objects.isNull(info.getVersion()))
        {
            errorList.add("Info Version not found. Please add a version field to the info field "
                    + "and fulfill it.");
        }
        else if (info.getVersion().length() > Constants.MAX_NAME_LENGTH)
        {
            errorList.add("Too long Info Version: Version length is larger than " +
                    Constants.MAX_NAME_LENGTH + " characters. Please do not" + " exceed the maximum length.");
        }
        else
        {
            // Now create matcher object.
            Matcher m = INFO_VERSION_PATTERN.matcher(info.getVersion());
            if (!m.find())
            {
                errorList.add("Invalid format Info Version: Version only can contain this set of characters: "
                        + "(0-9 .). Also its pattern should be {major}.{minor}.{fix}");
            }
        }

        return errorList;
    }

    /**
     * Check for forbidden nodes
     *
     * @param asyncAPI asyncAPI definition
     * @return error list
     */
    List<String> validateForbiddenNodes(final AsyncAPI asyncAPI)
    {
        final List<String> errorList = new ArrayList<>();

        Map<String, Server> servers = asyncAPI.getServers();
        if (Objects.nonNull(servers) && servers.size() > 0)
        {
            errorList.add("AsyncApi servers node is forbidden.");
        }

        return errorList;
    }

    /**
     * This method will validate the <b>Info</b> field associated to the Async API uploaded
     *
     * @param info associated to the AsyncAPI
     * @param product associated to the new uploaded API
     * @return a list with the optional errors found.
     */
    private List<String> validateInfoContact(final @NotNull Info info, final @NotNull Product product)
    {
        List<String> errorList = new ArrayList<>();
        if (info.getContact() == null)
        {
            errorList.add("Swagger error: Info -> Contact not found. Please add the contact field to the info field and fulfill" + " it.");
        }
        else
        {
            if (info.getContact().getName() == null || !info.getContact().getName().equalsIgnoreCase(product.getName()))
            {
                errorList.add("Swagger error: Info -> Contact -> Name: Must be the product name. Expected name: " +
                        product.getName() + " - Actual name: " + info.getContact().getName());
            }

            Utils.findHotProperty("nova.url").ifPresent(dashboardUrl -> {
                String productURL = getProductEncodedURL(dashboardUrl, product);
                if (info.getContact().getUrl() == null || !info.getContact().getUrl().equalsIgnoreCase(productURL))
                {
                    errorList.add("Swagger error: Info -> Contact -> URL: Must be the URL of the product on the NOVA platform. Expected URL: " +
                            productURL + " - Actual URL: " + info.getContact().getUrl());
                }
            });

            if (info.getContact().getEmail() == null || !info.getContact().getEmail().equalsIgnoreCase(product.getEmail()))
            {
                errorList.add("Swagger error: Info -> Contact -> Email: Must be the product contact e-mail. Expected email: " +
                        product.getEmail() + " - Actual email: " + info.getContact().getEmail());
            }
        }
        return errorList;
    }

    /**
     * Extract the url of the user API's product.
     *
     * @param dashboardUrl entry path of the server
     * @param product associated to the user
     * @return the encoded url of the product
     */
    private String getProductEncodedURL(final String dashboardUrl, final Product product)
    {
        return dashboardUrl +
                Constants.URL_PATH +
                UriUtils.encodePath(product.getName(), "UTF-8") +
                '/' +
                product.getId();
    }

    /**
     * It will check>
     * 1. If the contact field is present.
     * 2. If the URL field is present and not blank.
     * 3. If the URL is a valid one (we're using the Apache Commons Validator, refer to <a href="https://commons.apache.org/proper/commons-validator/apidocs/org/apache/commons/validator/routines/UrlValidator.html">Apache Javadoc</a>)
     *
     * @param info uploaded by the user
     * @return a list with the possible errors
     */
    private List<String> validateInfoURL(final @NotNull Info info) {
        List<String> errorList = new ArrayList<>();

        Optional<Contact> optionalContact = Optional.ofNullable(info.getContact());

        if(optionalContact.isPresent()){

            String contactUrl = info.getContact().getUrl();

            if(Strings.isNullOrEmpty(contactUrl)){
                errorList.add("Swagger error: Info -> Contact -> URL: The URL must be filled.");
            }else{
                if(!ValidationUtils.isValidURL(contactUrl)){
                    errorList.add("Swagger error: Info -> Contact -> URL: The URL must be a valid one. Check if it contains the http/https protocol.");
                }
            }

        }else{
            errorList.add("Swagger error: Info -> Contact not found. Please add the contact field to the info field and fulfill it.");
        }

        return errorList;
    }

}
