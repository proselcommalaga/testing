package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Component
@Slf4j
public class Utils
{
    public <T> Stream<T> streamOfNullable(T... values)
    {
        return values == null ? Stream.empty() : Stream.of(values);
    }

    public <T> List<T> listOfNullable(T... values)
    {
        return values == null ? Collections.emptyList() : Arrays.asList(values);
    }

    public String unifyCRLF2LF(String content)
    {
        // Replace all carrier return to avoid conflicts resolving md5
        return content
                // for windows
                .replace("\r\n", "\n")
                // for Mac
                .replace("\r", "\n");
    }

    public String calculeMd5Hash(byte[] bytesOfMessage)
    {
        if (Objects.nonNull(bytesOfMessage))
        {
            try
            {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] theMD5digest = md.digest(bytesOfMessage);
                String md5Hash = this.bytesToHex(theMD5digest);
                log.trace("[Utils] -> [calculeMd5Hash]: for inputs Bytes: [{}] Getting hash: [{}] ", Arrays.toString(bytesOfMessage), md5Hash);
                log.debug("[Utils] -> [calculeMd5Hash]: Getting hash: [{}]", md5Hash);
                return md5Hash;
            }
            catch (NoSuchAlgorithmException e)
            {
                throw new NovaException(ReleaseVersionError.getValidationUnexpectedError());
            }
        }
        return StringUtils.EMPTY;
    }

    private String bytesToHex(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * This method will search for the property passed by parameter on the current web application context environment.
     * This is needed because we can't access to @Value injected attributes on static methods. So, as Spring Boot let us the Environment
     * management, we can search any property on it.
     *
     * <br><br> Please, if you want to use this method. You need to use the <b>NovaCoreTestUtils#instantiateTestEnvironmentVariables</b> functionality in your tests cases.<br><br>
     *
     * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config">Externalized Configuration by Spring Docs</a>
     *
     *
     * @param propertyName property name that we are defining in application context
     * @return a present value if this property is available
     */
    public static Optional<String> findHotProperty(final @NotNull String propertyName) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Optional<WebApplicationContext> optionalServletContext = Optional.ofNullable(WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext()));
        return optionalServletContext.map(webApplicationContext -> webApplicationContext.getEnvironment().getProperty(propertyName));
    }

}