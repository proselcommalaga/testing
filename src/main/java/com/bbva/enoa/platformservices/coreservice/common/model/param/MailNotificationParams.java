package com.bbva.enoa.platformservices.coreservice.common.model.param;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class MailNotificationParams
{
    private String productName;
    private Integer productId;
    private String environment;
    private String ivUser;
    private String userMailAddress;
    private String filesystemPath;
    private String filename;
    private String filesystemName;

    private MailNotificationParams()
    {

    }

    public static class MailNotificationParamsBuilder
    {
        private String productName;
        private Integer productId;
        private String environment;
        private String ivUser;
        private String userMailAddress;
        private String filesystemPath;
        private String filename;
        private String filesystemName;

        public MailNotificationParamsBuilder productName(String productName)
        {
            this.productName = productName;
            return this;
        }

        public MailNotificationParamsBuilder productId(Integer productId)
        {
            this.productId = productId;
            return this;
        }

        public MailNotificationParamsBuilder environment(String environment)
        {
            this.environment = environment;
            return this;
        }

        public MailNotificationParamsBuilder ivUser(String ivUser)
        {
            this.ivUser = ivUser;
            return this;
        }

        public MailNotificationParamsBuilder userMailAddress(String userMailAddress)
        {
            this.userMailAddress = userMailAddress;
            return this;
        }

        public MailNotificationParamsBuilder filesystemPath(String filesystemPath)
        {
            this.filesystemPath = filesystemPath;
            return this;
        }

        public MailNotificationParamsBuilder filename(String fileName)
        {
            this.filename = fileName;
            return this;
        }

        public MailNotificationParamsBuilder filesystemName(String filesystemName)
        {
            this.filesystemName = filesystemName;
            return this;
        }

        public MailNotificationParams build()
        {
            MailNotificationParams params = new MailNotificationParams();
            params.productName = this.productName;
            params.productId = this.productId;
            params.environment = this.environment;
            params.ivUser = this.ivUser;
            params.userMailAddress = this.userMailAddress;
            params.filesystemPath = this.filesystemPath;
            params.filename = this.filename;
            params.filesystemName = this.filesystemName;
            return params;
        }
    }
}
