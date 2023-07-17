package com.bbva.enoa.platformservices.coreservice.brokersapi.util;

import com.bbva.enoa.platformservices.coreservice.common.util.CipherUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CipherUtilsTest
{

    @Test
    void testGetRandomPassword()
    {
        String pass1 = CipherUtils.getRandomPassword(15);
        String pass2 = CipherUtils.getRandomPassword(15);

        assertEquals(15, pass1.length());
        assertEquals(15, pass1.length());
        assertNotEquals(pass1, pass2);
    }

    @Test
    void testEncryptMessage()
    {
        String message = "This a message in plain text";
        String key = "DGhBN8D7ONHEsP48kDTVt56lHgbGNmzN";
        byte[] encryptedBytes = CipherUtils.encryptMessage(message.getBytes(), key.getBytes());

        assertEquals("nr3oiU9Ppuxzxcgl4X3wrLFZOeNL0DiSaS3UaGR4wM+WA4NhwmiWJ1Um9Ks=", Base64.getEncoder().encodeToString(encryptedBytes));
    }

    @Test
    void testEncryptMessageWithInvalidKeyLength()
    {
        String message = "This a message in plain text";
        String key = "DGhBN8D7ONHEsP48kDTVt56lHgbGNmzN-extra";
        assertThrows(NovaException.class, () -> CipherUtils.encryptMessage(message.getBytes(), key.getBytes()));
    }

    @Test
    void testEncryptAndDecryptMessage()
    {
        String message = "This a message in plain text";
        String key = "DGhBN8D7ONHEsP48kDTVt56lHgbGNmzN";
        byte[] encryptedBytes = CipherUtils.encryptMessage(message.getBytes(), key.getBytes());

        byte[] decryptedBytes = CipherUtils.decryptMessage(encryptedBytes, key.getBytes());
        assertEquals(message, new String(decryptedBytes));
    }

    @Test
    void testDecryptMessageWithInvalidKeyLength()
    {
        String message = "This a message in plain text";
        String key = "DGhBN8D7ONHEsP48kDTVt56lHgbGNmzN-extra";
        assertThrows(NovaException.class, () -> CipherUtils.decryptMessage(message.getBytes(), key.getBytes()));
    }

}