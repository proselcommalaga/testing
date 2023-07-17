package com.bbva.enoa.platformservices.coreservice.common.interfaces;

/**
 * Cipher credential service interface
 */
public interface ICipherCredentialService
{
    /**
     * Generate a new random password that is encrypted with AES key
     *
     * @param length number of characters to generate
     * @return a new encrypted random password in base64
     */
    String generateEncryptedRandomPassword(int length);

    /**
     * Decrypt the password
     *
     * @param base64EncryptedPassword Encrypted password in base64
     * @return plain password
     */
    String decryptPassword(String base64EncryptedPassword);
}
