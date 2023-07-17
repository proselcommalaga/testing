package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CipherUtils
{

    public static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding"; // Recommended option by Sonar
    public static final String AES_CIPHER_ALGORITHM = "AES";
    public static final String IV = "0123456789012345"; // Not needed generate a random IV
    public static final int GCM_TAG_LENGTH = 16;

    /**
     * Encrypt message with AES
     *
     * @param message  text to encrypt
     * @param keyBytes key
     * @return encrypted message
     */
    public static byte[] encryptMessage(byte[] message, byte[] keyBytes)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            SecretKey secretKey = new SecretKeySpec(keyBytes, AES_CIPHER_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
            return cipher.doFinal(message);
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException
                | NoSuchPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e)
        {
            throw new NovaException(BrokerError.getUnexpectedError(), e.getMessage());
        }
    }

    /**
     * Decrypt message with AES
     *
     * @param encryptedMessage text to decrypt
     * @param keyBytes         key
     * @return plain text
     */
    public static byte[] decryptMessage(byte[] encryptedMessage, byte[] keyBytes)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            SecretKey secretKey = new SecretKeySpec(keyBytes, AES_CIPHER_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
            return cipher.doFinal(encryptedMessage);
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException
                | NoSuchPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e)
        {
            throw new NovaException(BrokerError.getUnexpectedError(), e.getMessage());
        }
    }

    /**
     * Generate a random alphanumeric password (exclude some confusing characters like O and 0)
     *
     * @param length number of characters to generate
     * @return the generated password
     */
    public static String getRandomPassword(int length)
    {
        String validChars = "123456789abcdefghijklmnpqrstuvwxyzABCDEFGHIJKLMNPQRSTUVWXYZ";
        SecureRandom secureRandom = new SecureRandom();

        return secureRandom.ints(length, 0, validChars.length()).mapToObj(validChars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }

    private CipherUtils()
    {
        throw new IllegalStateException("Utility class");
    }
}
