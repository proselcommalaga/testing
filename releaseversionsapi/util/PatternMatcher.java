package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * To resolve patterns to match a subject
 */
public final class PatternMatcher
{


    /**
     * private constructor
     */
    private PatternMatcher()
    {
    }

    /**
     * Matches any given pattern
     *
     * @param pattern Pattern
     * @param subject characters to match with the pattern
     * @return match result
     */
    public static final boolean customPattern(String pattern, String subject)
    {
        Pattern patternObj = Pattern.compile(pattern);
        Matcher matcher = patternObj.matcher(subject);

        return matcher.matches();
    }

    /**
     * Matches the subject with a ^[a-zA-Z0-9]+$ pattern
     *
     * @param subject characters to match with the pattern
     * @return match result
     */
    public static final boolean patternazAZ09(String subject)
    {
        boolean result = false;

        if (!Strings.isNullOrEmpty(subject))
        {
            String patternString = "^[a-zA-Z0-9]+$";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(subject);

            result = matcher.matches();
        }

        return result;
    }

}
