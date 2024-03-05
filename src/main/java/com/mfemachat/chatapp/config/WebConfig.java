package com.mfemachat.chatapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class WebConfig {
    private String tokenSecret;

    /**
     * @return the tokenSecret
     */
    public String getTokenSecret() {
        return tokenSecret;
    }

    /**
     * @param tokenSecret the tokenSecret to set
     */
    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    private long tokenExpirationMsec;

    /**
     * @return the tokenExpirationMsec
     */
    public long getTokenExpirationMsec() {
        return tokenExpirationMsec;
    }

    /**
     * @param tokenExpirationMsec the tokenExpirationMsec to set
     */
    public void setTokenExpirationMsec(long tokenExpirationMsec) {
        this.tokenExpirationMsec = tokenExpirationMsec;
    }

    private String cookieName;

    /**
     * @return the cookieName
     */
    public String getCookieName() {
        return cookieName;
    }

    /**
     * @param cookieName the cookieName to set
     */
    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }
}
