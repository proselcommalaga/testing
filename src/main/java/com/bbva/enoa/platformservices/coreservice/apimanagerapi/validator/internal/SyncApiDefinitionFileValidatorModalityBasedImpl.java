package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.common.util.SwaggerConverter;
import com.bbva.enoa.platformservices.coreservice.common.util.ValidationUtils;
import com.google.common.base.Strings;
import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;
import org.yaml.snakeyaml.error.YAMLException;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Sync api definition file validator modality based.
 */
@Component
public class SyncApiDefinitionFileValidatorModalityBasedImpl implements IDefinitionFileValidatorModalityBased<Swagger>
{
    private static final Logger LOG = LoggerFactory.getLogger(SyncApiDefinitionFileValidatorModalityBasedImpl.class);
    private static final Pattern SWAGGER_TITLE_VALID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_]+$");
    private static final Pattern SWAGGER_VERSION_VALID_PATTERN = Pattern.compile(com.bbva.enoa.platformservices.coreservice.common.Constants.PATTERN_VERSION_REGEX);
    private final String dashboardUrl;
    private final SwaggerConverter swaggerConverter;

    @Autowired
    public SyncApiDefinitionFileValidatorModalityBasedImpl(
            final @Value("${nova.url}") String dashboardUrl, final SwaggerConverter swaggerConverter
    )
    {
        this.dashboardUrl = dashboardUrl;
        this.swaggerConverter = swaggerConverter;
    }

    @Override
    public Swagger parseAndValidate(final String content, final Product product, final ApiType apiType) throws DefinitionFileException
    {
        List<String> errorList = new ArrayList<>();
        // Parsing Swagger into object
        Swagger swagger = this.parseDefinitionFile(content);
        // Validating that all required fields are OK
        if (swagger == null)
        {
            errorList.add("Swagger parse error: the swagger content or swagger file not found, swagger is null. Please, ensure that the swagger exists and it has contents.");
        }
        else
        {
            // Info field validation
            errorList.addAll(this.validateInfoSwagger(swagger, product, apiType));

            if (swagger.getPaths() == null || swagger.getPaths().isEmpty())
            {
                errorList.add("Swagger error: no path found, at least one resource must be exposed. Paths field is required.");
            }
            else
            {
                for (String key : swagger.getPaths().keySet())
                {
                    // Path field validation
                    errorList.addAll(this.validatePathSwagger(swagger, key));
                }
            }
        }
        if (!errorList.isEmpty())
        {
            throw new DefinitionFileException(errorList);
        }
        else
        {
            return swagger;
        }
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.SYNC == modality;
    }

    private Swagger parseDefinitionFile(final String content) throws DefinitionFileException
    {
        try
        {
            // Parsing Swagger into object
            return this.swaggerConverter.parseSwaggerFromString(content);
        }
        catch (YAMLException e)
        {
            LOG.error("[SyncApiDefinitionFileValidatorImpl] -> [parseDefinitionFile]: Error parsing swagger with yaml: ", e);
            throw new DefinitionFileException(List.of("Yaml exception: error parsing the Yaml file. The swagger file does not complies with Yaml format. Errors: " + e));
        }

    }

    /**
     * Validate the INFO field
     *
     * @param swagger Swagger content
     * @param product Nova product
     * @param apiType Api Type
     */
    private List<String> validateInfoSwagger(final Swagger swagger, final Product product, final ApiType apiType)
    {
        List<String> errorList = new ArrayList<>();
        if (swagger.getInfo() == null)
        {
            errorList.add("Swagger error: Info not found. Please add a swagger info field with, at least, the title and fulfill it with " + "the api's name, a version and a description.");
        }
        else
        {
            errorList.addAll(this.validateInfoTitle(swagger));

            errorList.addAll(this.validateInfoDescription(swagger));

            errorList.addAll(this.validateInfoBusinessUnit(swagger, product, apiType));

            errorList.addAll(this.validateInfoVersion(swagger));

            errorList.addAll(this.validateInfoExternalApiValidations(swagger, apiType));

            if (ApiType.EXTERNAL != apiType)
            {
                errorList.addAll(this.validateInfoContact(swagger, product));
                errorList.addAll(this.validateInfoScheme(swagger));
            }
            else
            {
                errorList.addAll(SyncApiDefinitionFileValidatorModalityBasedImpl.validateInfoURL(swagger));
            }
        }
        return errorList;
    }

    /**
     * Validate the PATH field
     *
     * @param swagger Swagger content
     * @param key     Path key
     */
    private List<String> validatePathSwagger(final Swagger swagger, final String key)
    {
        List<String> errorList = new ArrayList<>();
        if (swagger.getPath(key).getOperationMap() == null || swagger.getPath(key).getOperationMap().isEmpty())
        {
            errorList.add("Swagger error: no method defined in path " + key + ". Any path requires a HTTP method like for example GET or " + "POST.");
        }
        else
        {
            errorList.addAll(this.validateOperationMap(swagger, key));
        }
        return errorList;
    }

    /**
     * Validate operation map
     *
     * @param swagger swagger
     * @param key     key
     */
    private List<String> validateOperationMap(Swagger swagger, String key)
    {
        List<String> errorList = new ArrayList<>();
        Map<HttpMethod, Operation> operationMap = swagger.getPath(key).getOperationMap();
        for (Map.Entry<HttpMethod, Operation> entry : operationMap.entrySet())
        {
            HttpMethod httpMethod = entry.getKey();
            Operation operation = entry.getValue();
            String pathKeydescription = "path " + key + " and verb " + httpMethod;
            if (operation.getDescription() == null)
            {
                errorList.add("Swagger error: description not defined for " + pathKeydescription + ". Please add a description field and fulfill it.");
            }
            else if (operation.getDescription().length() > Constants.MAX_NAME_LENGTH)
            {
                errorList.add("Too long: description length for " + pathKeydescription + " is larger than " + Constants.MAX_NAME_LENGTH + " characters. Please do not exceed the maximum length.");
            }
            if (operation.getOperationId() == null)
            {
                errorList.add("Swagger error: operationId not defined for " + pathKeydescription + ". Please add an operationId with the method name.");
            }
            if (operation.getResponses() == null)
            {
                errorList.add("Swagger error: responses not defined for path " + key + " and verb " + httpMethod + ". Please add a responses field for this method.");
            }

            errorList.addAll(this.validateOperationParameters(operation, key, httpMethod.name()));
        }
        return errorList;
    }

    /**
     * Validate operation map
     *
     * @param operation operation
     * @param key       key
     * @param method    httpMethod
     */
    private List<String> validateOperationParameters(Operation operation, String key, String method)
    {
        List<Parameter> parameters = operation.getParameters();
        List<String> errorList = new ArrayList<>();

        for (Parameter parameter : parameters)
        {
            if (parameter.getName() == null)
            {
                errorList.add("Swagger error: there is a missing parameter name for method " + method + " in path " + key + ". Please define a name for every parameter.");
            }
            else if (parameter.getIn().equalsIgnoreCase("path") && !parameter.getRequired())
            {
                errorList.add("Swagger error: parameter " + parameter.getName() + " in method " + method + " for path " + key + "must be required. Please add the field 'required: true' to the parameter: " + parameter.getName());
            }
        }
        return errorList;
    }

    /**
     * Validate contact
     *
     * @param swagger swagger
     * @param product product
     */
    private List<String> validateInfoContact(Swagger swagger, Product product)
    {
        List<String> errorList = new ArrayList<>();
        if (swagger.getInfo().getContact() == null)
        {
            errorList.add("Swagger error: Info -> Contact not found. Please add the contact field to the info field and fulfill" + " it.");
        }
        else
        {
            if (swagger.getInfo().getContact().getName() == null || !swagger.getInfo().getContact().getName().equalsIgnoreCase(product.getName()))
            {
                errorList.add("Swagger error: Info -> Contact -> Name: Must be the product name. Expected name: " +
                        product.getName() + " - Actual name: " + swagger.getInfo().getContact().getName());
            }
            String productURL = this.getEncodedURL(product);
            if (swagger.getInfo().getContact().getUrl() == null || !swagger.getInfo().getContact().getUrl().equalsIgnoreCase(productURL))
            {
                errorList.add("Swagger error: Info -> Contact -> URL: Must be the URL of the product on the NOVA platform. Expected URL: " +
                        productURL + " - Actual URL: " + swagger.getInfo().getContact().getUrl());
            }
            if (swagger.getInfo().getContact().getEmail() == null || !swagger.getInfo().getContact().getEmail().equalsIgnoreCase(product.getEmail()))
            {
                errorList.add("Swagger error: Info -> Contact -> Email: Must be the product contact e-mail. Expected email: " +
                        product.getEmail() + " - Actual email: " + swagger.getInfo().getContact().getEmail());
            }
        }
        return errorList;
    }

    /**
     * Get url encoded
     *
     * @param product product
     */
    private String getEncodedURL(Product product)
    {
        return this.dashboardUrl +
                Constants.URL_PATH +
                UriUtils.encodePath(product.getName(), "UTF-8") +
                Constants.PATH_SLASH +
                product.getId();
    }

    /**
     * Validate info version
     *
     * @param swagger swagger
     */
    private List<String> validateInfoVersion(Swagger swagger)
    {
        List<String> errorList = new ArrayList<>();
        if (swagger.getInfo().getVersion() == null)
        {
            errorList.add("Swagger error: Info -> Version not found. Please add a version field to the info field and fulfill" + " it.");
        }
        else if (swagger.getInfo().getVersion().length() > Constants.MAX_NAME_LENGTH)
        {
            errorList.add("Too long: Info -> Version length is larger than " + Constants.MAX_NAME_LENGTH + " characters. Please do not" + " exceed the maximum length.");
        }
        else
        {
            Matcher m = SWAGGER_VERSION_VALID_PATTERN.matcher(swagger.getInfo().getVersion());
            if (!m.matches())
            {
                errorList.add("Invalid format: Info -> Version only can contain this set of characters: (0-9 .). Also its pattern should be {major}.{minor}.{fix}");
            }
        }
        return errorList;
    }


    /**
     * Validate info external api validations list.
     *
     * @param swagger the swagger
     * @param apiType the api type
     * @return the list
     */
    private List<String> validateInfoExternalApiValidations(final Swagger swagger, final ApiType apiType)
    {
        List<String> errorList = new ArrayList<>();
        Map<String, Object> vendorExtensionMap = swagger.getVendorExtensions();

        if (vendorExtensionMap == null)
        {
            errorList.add("Swagger error: any NOVA extensions found. All Swagger almost needs mandatory 'x-generator-properties' extension.");
        }
        else
        {
            Map<String, Object> customProperties = (Map<String, Object>) vendorExtensionMap.get("x-generator-properties");

            // Double check for bussines unit mandatory param
            if(null == customProperties || customProperties.isEmpty())
            {
                errorList.add("Swagger error: any NOVA exceptions found. All Swagger at least needs mandatory 'x-generator-properties' extension filled with 'x-bussiness-unit' property.");

            }else{

                // Define the value for the parameter, in case not defined set to false as default
                boolean isXParameterExternalDefined;
                if(null == customProperties.get(Constants.GENERATOR_EXTERNAL_API_PARAM))
                {
                    LOG.debug("[SyncApiDefinitionFileValidatorModalityBasedImpl]->[validateInfoExternalApiValidations]: The 'x-external-api' parameter is not defined, setting default parameter as FALSE");
                    isXParameterExternalDefined = false;
                }
                else {isXParameterExternalDefined = Boolean.parseBoolean(customProperties.get(Constants.GENERATOR_EXTERNAL_API_PARAM).toString());}

                // Checking wrong possibilities for the external parameter
                if (!isXParameterExternalDefined && apiType.isExternal())
                {
                    errorList.add("Swagger error: 'x-external-api' is a mandatory parameter for external apis. Please add the definition parameter 'x-external-api: true' in the swagger.");
                }
                else if (isXParameterExternalDefined && !apiType.isExternal())
                {
                    errorList.add("Swagger error: x-external-api is a forbidden parameter for not external apis. Please remove the definition parameter in the swagger.");
                }
                else
                {
                    LOG.debug("[SyncApiDefinitionFileValidatorModalityBasedImpl]->[validateInfoExternalApiValidations]: The external api validations are set to [{}] for swagger type [{}]",
                            null == customProperties.get(Constants.GENERATOR_EXTERNAL_API_PARAM) ? Boolean.FALSE : customProperties.get(Constants.GENERATOR_EXTERNAL_API_PARAM), apiType.getApiType());
                }
            }


        }

        return errorList;
    }


    /**
     * Validate info business unit
     *
     * @param swagger swagger
     * @param product product
     */
    private List<String> validateInfoBusinessUnit(Swagger swagger, Product product, final ApiType apiType)
    {
        List<String> errorList = new ArrayList<>();
        Map<String, Object> vendorExtensionMap = swagger.getVendorExtensions();

        if (vendorExtensionMap == null)
        {
            errorList.add("Swagger error: any NOVA extensions found. All Swagger almost needs mandatory 'x-generator-properties' extension.");
        }
        else
        {
            Map<String, Object> customProperties = (Map<String, Object>) vendorExtensionMap.get("x-generator-properties");
            if (customProperties == null || customProperties.get("business-unit") == null)
            {
                errorList.add("Swagger error: business-unit -> business-unit not found. Please add a business-unit in the swagger.");
            }
            else
            {
                if (ApiType.EXTERNAL != apiType && (Strings.isNullOrEmpty(product.getUuaa())
                        || !product.getUuaa().equalsIgnoreCase(customProperties.get("business-unit").toString())))
                {
                    errorList.add("Swagger error: business-unit -> business-unit do not match with the product UUAA. The business-unit and the product UUAA must be the same.");
                }
            }
        }
        return errorList;
    }

    /**
     * Validate info description
     *
     * @param swagger swagger
     */
    private List<String> validateInfoDescription(Swagger swagger)
    {
        List<String> errorList = new ArrayList<>();
        if (swagger.getInfo().getDescription() == null)
        {
            errorList.add("Swagger error: Info -> Description not found. Please add a description field to the info field and fulfill" + " it.");
        }
        else if (swagger.getInfo().getDescription().length() > Constants.MAX_DESCRIPTION_LENGTH)
        {
            errorList.add("Too long: Info -> Description length is larger than " + Constants.MAX_DESCRIPTION_LENGTH + " characters. Please do " + "not exceed the maximum length.");
        }
        return errorList;
    }

    /**
     * Validate info title
     *
     * @param swagger swagger
     */
    private List<String> validateInfoTitle(Swagger swagger)
    {
        List<String> errorList = new ArrayList<>();
        if (swagger.getInfo().getTitle() == null)
        {
            errorList.add("Swagger error: Info -> Title not found. Please add a title field to the info field and fulfill it with the" + " api's name.");
        }
        else if (swagger.getInfo().getTitle().length() > Constants.MAX_NAME_LENGTH)
        {
            errorList.add("Too long: Info -> Title length is larger than " + Constants.MAX_NAME_LENGTH + " characters. Please do not exceed the " + "maximum length.");
        }
        else
        {
            Matcher m = SWAGGER_TITLE_VALID_PATTERN.matcher(swagger.getInfo().getTitle());
            if (!m.find())
            {
                errorList.add("Invalid format: Info -> Title only can contain this set of characters: a-z A-Z 0-9 - _");
            }
        }
        return errorList;
    }

    /**
     * Validate info title
     *
     * @param swagger swagger
     */
    private List<String> validateInfoScheme(Swagger swagger)
    {
        List<String> errorList = new ArrayList<>();
        if (swagger.getSchemes() == null)
        {
            errorList.add("Swagger error: Schemes -> schemes field is mandatory.");
        }
        else if (!swagger.getSchemes().contains(Scheme.HTTPS) || !swagger.getSchemes().contains(Scheme.HTTP))
        {
            errorList.add("Swagger error: Schemes -> schemes field values must include http and https.");
        }
        return errorList;
    }

    /**
     * It will check>
     * 1. If the contact field is present.
     * 2. If the URL field is present and not blank.
     * 3. If the URL is a valid one (we're using the Apache Commons Validator, refer to <a href="https://commons.apache.org/proper/commons-validator/apidocs/org/apache/commons/validator/routines/UrlValidator.html">Apache Javadoc</a>)
     *
     * @param swagger uploaded by the user
     * @return a list with the possible errors
     */
    private static List<String> validateInfoURL(final @NotNull Swagger swagger)
    {
        List<String> errorList = new ArrayList<>();

        Optional<Contact> optionalContact = Optional.ofNullable(swagger.getInfo().getContact());

        if (optionalContact.isPresent())
        {

            String contactUrl = swagger.getInfo().getContact().getUrl();

            if (Strings.isNullOrEmpty(contactUrl))
            {
                errorList.add("Swagger error: Info -> Contact -> URL: The URL must be filled.");
            }
            else
            {
                if (!ValidationUtils.isValidURL(contactUrl))
                {
                    errorList.add("Swagger error: Info -> Contact -> URL: The URL must be a valid one. Check if it contains the http/https protocol.");
                }
            }

        }
        else
        {
            errorList.add("Swagger error: Info -> Contact not found. Please add the contact field to the info field and fulfill it.");
        }

        return errorList;
    }
}
