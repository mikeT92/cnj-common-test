package edu.hm.cs.fwp.cloud.common.test.adapter.rest;

import edu.hm.cs.fwp.cloud.common.test.internal.config.Config;
import edu.hm.cs.fwp.cloud.common.test.internal.config.ConfigProvider;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;

import java.util.logging.Logger;

/**
 * Common test fixture for {@code RestAssured} based system tests.
 * <p>
 * Performs an OpenID Connect login to the OpenID Connect provider specified in either properties file
 * {@code META-INF/test-config.properties} or in system properties or in environment variables.
 * </p>
 * <p>
 * Tokens obtained during this login can be retrieved via {@code #getToken}Performs an OpenID Connect login to the
 * OpenID Connect provider specified in either properties file {@code META-INF/test-config.properties} or
 * in system properties or in environment variables.
 * </p>
 *
 * @author Michael Theis (michael.theis@hm.edu)
 * @version 1.0
 * @since release 1.0.0
 */
public class RestAssuredSystemTestFixture {

    private static final Logger logger = Logger.getLogger(RestAssuredSystemTestFixture.class.getName());
    private Config config;
    private boolean skipOpenIdConnectLogin;
    private String oidcClientId;
    private String oidcClientSecret;
    private String oidcAccessTokenUri;
    private String oidcUserName;
    private String oidcPassword;
    private String targetRoute;
    private boolean skipReadinessProbe;
    private String readinessProbePath;
    private int initialDelaySeconds;
    private int failureThreshold;
    private int periodSeconds;
    private int timeoutSeconds;
    private String accessToken;
    private String idToken;

    /**
     * Resets this fixture after all tests have been executed
     */
    public void onAfter() {
        RestAssured.reset();
    }

    /**
     * Sets up this fixture before all tests are run.
     * <p>
     * A typical setup consists of the following steps:
     * </p>
     * <ul>
     * <li>Retrieve configuration from properties file {@code META-INF/test-config.properties} or
     * from system properties or from environment variables.</li>
     * <li>Retrieve target route to REST endpoint to be tested from system property {@code target.route}
     * or environment variable {@code TARGET_ROUTE}.</li>
     * <li>Performs a login to the specified OpenID Connect provider obtaining an access accessToken and an ID accessToken.</li>
     * <li>Waits to the specified target service to become ready by checking its readiness probe
     * at URI {@code api/v1/probes/readiness}.</li>
     * </ul>
     */
    public void onBefore() {
        ensureConfiguration();
        LogConfig augmentedLogConfig = RestAssured.config.getLogConfig();
        augmentedLogConfig.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.config = RestAssured.config().sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation()).logConfig(augmentedLogConfig);
        RestAssured.baseURI = targetRoute;
        ensureTokens();
        ensureApplicationReadiness();
    }

    /**
     * Returns the access token obtained during login.
     */
    public String getToken() {
        return this.accessToken;
    }

    /**
     * Returns the access token obtained during login.
     */
    public String getAccessToken() {
        return this.accessToken;
    }

    /**
     * Returns the ID token obtained during login.
     */
    public String getIdToken() {
        return this.idToken;
    }

    /**
     * Reads some configuration from a config datasource.
     */
    private void ensureConfiguration() {
        if (this.config == null) {
            this.config = ConfigProvider.getConfig();
        }
        targetRoute = this.config.getOptionalValue("target.route", String.class).orElse(this.config.getValue("test.target.route", String.class));

        skipReadinessProbe = this.config.getOptionalValue("test.target.readinessProbe.skip", Boolean.class).orElse(false);
        logger.info(String.format("skipReadinessProbe=%s", skipReadinessProbe));
        readinessProbePath = this.config.getOptionalValue("test.target.readinessProbe.path", String.class).orElse("api/v1/probes/readiness");
        initialDelaySeconds = this.config.getOptionalValue("test.target.readinessProbe.initialDelaySeconds", Integer.class).orElse(10);
        failureThreshold = this.config.getOptionalValue("test.target.readinessProbe.failureThreshold", Integer.class).orElse(3);
        periodSeconds = this.config.getOptionalValue("test.target.readinessProbe.periodSeconds", Integer.class).orElse(10);
        timeoutSeconds = this.config.getOptionalValue("test.target.readinessProbe.timeoutSeconds", Integer.class).orElse(1);

        skipOpenIdConnectLogin = this.config.getOptionalValue("test.oidc.skip", Boolean.class).orElse(false);
        logger.info(String.format("skipOpenIdConnectLogin=%s", skipOpenIdConnectLogin));
        if (!skipOpenIdConnectLogin) {
            oidcClientId = this.config.getValue("test.oidc.client.clientId", String.class);
            oidcClientSecret = this.config.getValue("test.oidc.client.clientSecret", String.class);
            oidcAccessTokenUri = this.config.getValue("test.oidc.client.accessTokenUri", String.class);
            oidcUserName = this.config.getValue("test.oidc.client.user", String.class);
            oidcPassword = this.config.getValue("test.oidc.client.password", String.class);
        }
    }

    /**
     * Waits until the application is actually ready to accept requests.
     */
    private void ensureApplicationReadiness() {
        if (!skipReadinessProbe) {
            logger.info(String.format("waiting for application on readiness probe at [%s/%s] to become ready", RestAssured.baseURI, readinessProbePath));
            if (initialDelaySeconds > 0) {
                logger.info(String.format("sleeping [%d] seconds before checking readiness probe at [%s/%s] for the first time", initialDelaySeconds, RestAssured.baseURI, readinessProbePath));
                try {
                    Thread.sleep(initialDelaySeconds * 1000);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException("Thread has been interrupted", ex);
                }
            }
            boolean succeeded = false;
            while (!succeeded && failureThreshold > 0) {
                try {
                    RestAssured.given()
                            .get(readinessProbePath)
                            .then()
                            .assertThat()
                            .statusCode(200)/*
                        .body("outcome", response -> Matchers.equalTo("UP"))*/;
                    succeeded = true;
                    logger.info(String.format("readiness probe at [%s/%s] reported UP", RestAssured.baseURI, readinessProbePath));
                } catch (AssertionError | Exception ex) {
                    // explicitly ignore any exceptions
                    logger.info("checking readiness probe failed (assuming application is still booting)");
                }
                if (!succeeded) {
                    if (--failureThreshold > 0) {
                        logger.info(String.format("sleeping [%d] seconds before checking readiness probe at [%s/%s] again", periodSeconds, RestAssured.baseURI, readinessProbePath));
                        try {
                            Thread.sleep(periodSeconds * 1000);
                        } catch (InterruptedException ex) {
                            throw new IllegalStateException("Thread has been interrupted", ex);
                        }
                    }
                }
            }
            if (!succeeded) {
                throw new IllegalStateException("readiness check failed!");
            }
        } else {
            logger.warning(String.format("assuming application at [%s] to be ready without checking readiness probe", RestAssured.baseURI));
        }
    }

    private void ensureTokens() {
        if (this.accessToken == null && !this.skipOpenIdConnectLogin) {
            ExtractableResponse response = RestAssured.given()
                    .param("scope", "openid")
                    .param("grant_type", "password")
                    .param("username", oidcUserName)
                    .param("password", oidcPassword)
                    .param("client_id", oidcClientId)
                    .param("client_secret", oidcClientSecret)
                    .when().post(oidcAccessTokenUri).then().contentType
                            (ContentType.JSON).extract();
            this.accessToken = response.jsonPath().getString("access_token");
            this.idToken = response.jsonPath().getString("id_token");
            if (this.accessToken == null) {
                throw new IllegalStateException("expected authentication provider to return access token but got none");
            } else {
                logger.info(String.format("got access token: \"%s\"", this.accessToken));
            }
            if (this.idToken == null) {
                logger.warning("expected authentication provider to return ID token but got none");
            } else {
                logger.info(String.format("got ID token: \"%s\"", this.idToken));
            }
        }
    }
}
