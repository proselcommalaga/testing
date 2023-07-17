package com.bbva.enoa.platformservices.coreservice.common.util;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utils for validating values
 *
 * @author vbazagad
 */
public class ValidationUtils
{
    /**
     * Verifies an object is not null nor empty CharSquence
     *
     * @param obj       Object to verify
     * @param exception Exception to throw if null or empty
     * @param <T>       type of exception
     * @throws T If obj is null or empty
     */
    public static <T extends Exception> void verifyNotNull(Object obj, T exception) throws T
    {

        if (obj == null)
        {
            throw exception;
        }
        else
        {
            if (obj instanceof CharSequence && StringUtils.isEmpty((CharSequence) obj))
            {
                throw exception;
            }
        }
    }

    // ---
    // URL Validation
    // ---

    private static final String SCHEME_REGEX = "^\\p{Alpha}[\\p{Alnum}\\+\\-\\.]*";
    private static final Pattern SCHEME_PATTERN = Pattern.compile(SCHEME_REGEX);
    private static final Set<String> allowedSchemes = Sets.newTreeSet(List.of("http", "https"));

    /**
     * Private method to check if the passed string is a correct scheme. This method will be invoked by {@link ValidationUtils#isValidURL(String)}.
     *
     * @param scheme that is contained in url
     * @return true if it is equals to http or https
     */
    private static boolean isValidScheme(String scheme) {
        if (scheme == null) {
            return false;
        }

        if (!SCHEME_PATTERN.matcher(scheme).matches()) {
            return false;
        }

        return allowedSchemes.contains(scheme.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Checks if an url is valid or not.
     *
     * <b>IMPORTANT</b>: this method doesn't support ftp scheme.
     *
     * @param url the string that you want to validate
     * @return true if the url is not null, it has correct syntax, and it has correct scheme
     */
    public static boolean isValidURL(final @NotNull String url) {
        if (url == null) {
            return false;
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return false;
        }

        String scheme = uri.getScheme();
        return isValidScheme(scheme);
    }
}
