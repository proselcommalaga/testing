package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.platformservices.coreservice.common.services.CipherCredentialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CipherCredentialServiceImplTest
{

    @InjectMocks
    private CipherCredentialServiceImpl cipherCredentialService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.cipherCredentialService, "passwordEncryptionKey", "LnvZXwFFfgrTVQoiVrCDHrncV0PoG8wjrLtOujocwU4=");
    }

    @Test
    void testGenerateEncryptedRandomPassword()
    {
        String encryptedPassword = cipherCredentialService.generateEncryptedRandomPassword(15);

        assertTrue(encryptedPassword.startsWith("{cipher}"));
    }

    @Test
    void testGenerateEncryptedRandomPassword_IsRandom()
    {
        String encryptedPassword1 = cipherCredentialService.generateEncryptedRandomPassword(15);
        String encryptedPassword2 = cipherCredentialService.generateEncryptedRandomPassword(15);

        assertNotEquals(encryptedPassword1, encryptedPassword2);
    }

    @Test
    void testDecryptPassword()
    {
        String encryptedPassword = cipherCredentialService.generateEncryptedRandomPassword(15);

        String textPlainPassword = cipherCredentialService.decryptPassword(encryptedPassword);

        assertEquals(15, textPlainPassword.length());
    }

    @Test
    void testDecryptPassworWithCipherPrefix()
    {
        String encryptedPassword = cipherCredentialService.generateEncryptedRandomPassword(15);

        String textPlainPassword = cipherCredentialService.decryptPassword(encryptedPassword.replace(CipherCredentialServiceImpl.CIPHER_MARK_PREFIX, ""));

        assertEquals(15, textPlainPassword.length());
    }

}