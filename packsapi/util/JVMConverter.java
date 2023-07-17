package com.bbva.enoa.platformservices.coreservice.packsapi.util;

import com.bbva.enoa.datamodel.model.release.entities.AllowedJdk;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public final class JVMConverter
{
    /**
     * Default Numeric java version value in NOVA Platform
     */
    private static final int DEFAULT_JAVA_MAYOR_VERSION_VALUE = 8;

    private JVMConverter()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    /**
     * Convert the extended version persisted as string in {@link AllowedJdk#getJvmVersion()} into the associated Java Version integer type to comparing it with Hardware pack
     * <br><br>
     * <p>
     * <strong>Note</strong>: If the JVM version passed by parameter is associated with a Java version lower than Java 11, it will convert everything into Java 8
     * </p>
     *
     * @param jvmVersion extended jdk version persisted in {@link AllowedJdk} table
     * @return the associated version of Java
     */
    public static Integer convertJVMVersionToJavaMayorVersion(final String jvmVersion)
    {
        Integer javaMayorVersion = DEFAULT_JAVA_MAYOR_VERSION_VALUE;

        try
        {
            if (Strings.isNullOrEmpty(jvmVersion))
            {
                log.debug("[JVMConverter] -> [convertJVMVersionToJavaMayorVersion]: the jvm version parameter: [{}] is null or empty. Set 8 by default", jvmVersion);
            }
            else
            {
                Integer jvmVersionSplit = Arrays.stream(jvmVersion.split("\\.")).map(Integer::parseInt).findFirst().orElse(8);

                if (jvmVersionSplit < DEFAULT_JAVA_MAYOR_VERSION_VALUE)
                {
                    log.debug("[JVMConverter] -> [convertJVMVersionToJavaMayorVersion]: the jvm version split: [{}] is lower than java mayor version default value: [{}]. Returned java mayor version default value",
                            jvmVersionSplit, DEFAULT_JAVA_MAYOR_VERSION_VALUE);
                }
                else
                {
                    javaMayorVersion = jvmVersionSplit;
                    log.debug("[JVMConverter] -> [convertJVMVersionToJavaMayorVersion]: the jvm version split: [{}] is gretter or equal than java mayor version default value: [{}]. Returned java mayor version obtained",
                            jvmVersionSplit, DEFAULT_JAVA_MAYOR_VERSION_VALUE);
                }
            }
        }
        catch (NumberFormatException e)
        {
            // If the JVM version passed by the dashboard hasn't got the correct format, we will send the legacy packs.
            log.error("[JVMConverter] - [convertJVMVersionToJavaMayorVersion]: Incorrect format of the JVM passed by the dashboard: [{}]. Error message: [{}]", jvmVersion, e.getMessage());
        }

        return javaMayorVersion;

    }
}
