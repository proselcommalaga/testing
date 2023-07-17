package com.bbva.enoa.platformservices.coreservice.common.services;


import com.bbva.enoa.platformservices.coreservice.common.interfaces.ICipherCredentialService;
import com.bbva.enoa.platformservices.coreservice.common.util.CipherUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Cipher credential service
 */
@Service
public class CipherCredentialServiceImpl implements ICipherCredentialService
{
    /**
     * Prefix to mark that password is encrypted
     */
    public static final String CIPHER_MARK_PREFIX = "{cipher}";

    /**
     * Password encryption key
     */
    @Value("${nova.keys.brokerUserPassword}")
    private String passwordEncryptionKey;

    @Override
    public String generateEncryptedRandomPassword(int length)
    {
        String randomPassword = CipherUtils.getRandomPassword(length);

        byte[] key = Base64.getDecoder().decode(passwordEncryptionKey);

        byte[] encryptedPassword = CipherUtils.encryptMessage(randomPassword.getBytes(), key);

        return CIPHER_MARK_PREFIX + Base64.getEncoder().encodeToString(encryptedPassword);
    }

    @Override
    public String decryptPassword(String base64EncryptedPassword)
    {
        if (base64EncryptedPassword.startsWith(CIPHER_MARK_PREFIX))
        {
            base64EncryptedPassword = base64EncryptedPassword.replace(CIPHER_MARK_PREFIX, "");
        }

        byte[] encryptedPassword = Base64.getDecoder().decode(base64EncryptedPassword);
        byte[] key = Base64.getDecoder().decode(passwordEncryptionKey);

        byte[] decryptedPasswordBytes = CipherUtils.decryptMessage(encryptedPassword, key);

        return new String(decryptedPasswordBytes);
    }

}
