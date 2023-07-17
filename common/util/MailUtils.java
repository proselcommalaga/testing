package com.bbva.enoa.platformservices.coreservice.common.util;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

public final class MailUtils
{
    public static String[] getEmailAddressesArray(String emailAddresses)
    {
        if (emailAddresses == null)
        {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        return emailAddresses.split(",");
    }

    /**
     * Check that a list of emails is valid
     *
     * @param emailArray array of email addresses
     * @return true if all emails are valid
     */
    public static boolean checkEmailValid(String[] emailArray)
    {
        String emailPattern = "^[_a-z0-9-]+(\\.[_a-z0-9-]+)*@" +
                "[a-z0-9-]+(\\.[a-z0-9-]+)*(\\.[a-z]{2,4})$";

        return Arrays.stream(emailArray)
                .allMatch(email -> email.trim().toLowerCase().matches(emailPattern));
    }
}
