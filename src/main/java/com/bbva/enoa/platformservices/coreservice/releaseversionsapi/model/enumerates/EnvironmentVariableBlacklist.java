package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.enumerates;

import lombok.extern.slf4j.Slf4j;

/**
 * List of environment variables that can not be used by libraries
 * This list contains variables used by NOVA Platform, Preconf, launcher scripts and some dangerous variables like SHELL
 */
@Slf4j
public enum EnvironmentVariableBlacklist
{
    ALLOCATED_CPUS,
    APIGATEWAY_URL,
    APPLICATION_NAME,
    CONFIG_SERVER_LABEL,
    CONFIG_SERVER_SERVICE_NAME,
    CONFIG_SERVER_URI,
    DEPLOYMENT_PLAN_ID,
    DOCKER_KEY,
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE,
    EUREKA_CLIENT_ZONE,
    FSTAB,
    HOME,
    HOSTNAME,
    LIB_CONFIG_DIR,
    LIB_TEMPLATES_DIR,
    LOADER_PATH,
    LOG_PATH,
    MEMORY,
    MGW_CONFIG_SERVER_URI,
    MGW_PORT,
    MICROGATEWAY_HOSTPORT,
    MICROGATEWAY_LOG_PATH,
    MICROGATEWAY_TIMEOUT,
    MODE,
    NEEDS_PRECONF,
    NO_GUNICORN_WORKERS,
    NOVA_APP_SCRIPT_PATH,
    NOVA_ENVIRONMENT,
    NOVA_LIBS,
    NOVA_PORT,
    OLDPWD,
    OUTGOING_REQUESTS_PORT,
    PARAMETERS,
    PWD,
    RELEASE_NAME,
    SERVER_PORT,
    SERVICE_GROUP_ID,
    SERVICE_MAJOR_VERSION,
    SHELL,
    SHLVL,
    SPRING_PROFILES_ACTIVE,
    SSL_CERT,
    TERM,
    TZ,
    USER
}
