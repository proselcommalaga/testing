package com.bbva.enoa.platformservices.coreservice;

import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.util.Map;

public final class NovaCoreTestUtils
{
    /**
     * This method will instantiate every entry of the parameter map into the unit test environment.
     *
     * @param names map with the name of the property and its value
     */
    public static void instantiateTestEnvironmentVariables(final @NotNull Map<String, String> names){
        if(!names.isEmpty()){
            ServletRequestAttributes attrs = Mockito.mock(ServletRequestAttributes.class);
            HttpServletRequest httpServletRequestMock = Mockito.mock(HttpServletRequest.class);
            HttpSession httpSessionMock = Mockito.mock(HttpSession.class);
            ServletContext servletContextMock = Mockito.mock(ServletContext.class);
            WebApplicationContext webApplicationContextMock = Mockito.mock(WebApplicationContext.class);
            Environment environmentMock = Mockito.mock(Environment.class);
            RequestContextHolder.setRequestAttributes(attrs);
            Mockito.when(attrs.getRequest()).thenReturn(httpServletRequestMock);
            Mockito.when(httpServletRequestMock.getSession()).thenReturn(httpSessionMock);
            Mockito.when(httpSessionMock.getServletContext()).thenReturn(servletContextMock);
            Mockito.when(servletContextMock.getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")).thenReturn(webApplicationContextMock);
            Mockito.when(webApplicationContextMock.getEnvironment()).thenReturn(environmentMock);
            names.forEach((key, value) -> Mockito.when(environmentMock.getProperty(key)).thenReturn(value));
        }
    }
}
