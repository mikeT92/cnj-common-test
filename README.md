# cnj-common-test

Provides common test classes to run system tests on REST APIs using RestAssured.

## Build this application 

__Note:__ You must setup a FWP IDE environment in order to build the application successfully. Please contact AT for instructions.  

1. Clone the git repo to your local machine.
1. Change to the repos root folder
1. Run command file __pre-commit-stage.cmd__ to build the application from scratch, run all tests etc. 

``` 
pre-commit-stage.cmd
```

## Exported Test Classes

### RestAssuredSystemTestFixture

Provides a text fixture for system test classes running on REST APIs which performs an OpenID Connect login obtaining an 
access token and an ID token from the specified OpenID Connect provider.

__Prerequisites:__
* expects a system property __test.target.route__ or an environment variable __TEST_TARGET_ROUTE__to be set to the base URL of the REST endpoint to test.
* expects a properties configuration file __META-INF/test-config.properties__ providing the following properties:

| Property Name | Type | Mandatory? | Description |
| --- | --- | --- | --- |
| test.oidc.skip | bool |  | true, if OpenID Connect authentication should be skipped and test.oidc.* properties are not specified (default: false)
| test.oidc.client.clientId | string | x | OpenID client ID; must match the unique identifier of a registered client on an OpenID Connect provider
| test.oidc.client.clientSecret | string | x | OpenID client credentials; must match the credentials of a registered client on an OpenID Connect provider
| test.oidc.client.accessTokenUri | string | x | Target URI of the token endpoint provided by an OpenID Connect provider
| test.oidc.client.user | string | x | test user name
| test.oidc.client.password | string | x | test user credentials
| test.target.route | string | x | target route URL to the application under test (just scheme + hostname + port without path)
| test.target.readinessProbe.skip | bool |  | true, if application should not be checked for readiness; otherwise false (default: false)
| test.target.readinessProbe.path | string |   | path of the readiness probe endpoint (default: /api/v1/probes/readiness)
| test.target.readinessProbe.initialDelaySeconds | int |    | number of seconds to wait before checking readiness probe (default: 10)
| test.target.readinessProbe.failureThreshold | int |    | number of retries before an application is assumed to be unhealthy (default: 3)
| test.target.readinessProbe.periodSeconds | int |   | number of seconds to wait between retries (default: 10)
| test.target.readinessProbe.timeoutSeconds | int |    | number of seconds a readiness check may last (default: 1)

* OR expects system properties with mentioned property names
* OR expects environment variable with matching names: property names are matched to environment names by converting everything to uppercase and replacing dots with underscores (i.e. test.oidc.client.clientId becomes TEST_OIDC_CLIENT_CLIENTID).

### HOW-TO use test fixture RestAssuredSystemTestFixture

#### Step 0: Setup your OpenID Connect Provider (like KeyCloak)

* Make sure your test client has been added as a client to your OpenID Connect provider. 
You should own a client ID and a client credential representing your test client. 
Otherwise your test client will not be able to login!

* Make sure you added a test user with appropriate groups and roles to your OpenID connect provider. 
You should own the user name and credential of your test user.
Otherwise your test client will not be able to login or your test will fail due to missing roles.

#### Step 1: Add required Maven dependencies

Add the following dependency to your POM file:

``` 
<!-- common test support -->
<dependency>
    <groupId>edu.hm.cs.fwp.cloud.common</groupId>
    <artifactId>cnj-common-test</artifactId>
    <version>${REPLACE_WITH_CURRENT_VERSION}</version>
    <scope>test</scope>
</dependency>
```

#### Step 2: Add test fixture configuration to your test sources

Add a properties file named __test-config.properties__ to your __/src/test/resources/META-INF__ folder:

``` 
test.oidc.client.clientId=<your-test-client-id>
test.oidc.client.clientSecret=<your-test-client-credential>
test.oidc.client.accessTokenUri=<your-openid-connect-provider-token-endpoint>
test.oidc.client.user=<your-test-user-name>
test.oidc.client.password=<your-test-user-password>
```

#### Step 3: Add test fixture to your system test class

Add a static member to your test class holding a reference to the test fixture. 
Make sure you call __RestAssuredSystemTestFixture#onBefore()__ before running all tests and 
__RestAssuredSystemTestFixture#onAfter()__ after running all tests.

Here's an example of a proper RestAssuredSystemTestFixture setup: 

```java 
public class HelloResourceSystemTest {

    private static final RestAssuredSystemTestFixture fixture = new RestAssuredSystemTestFixture();

    @BeforeAll
    public static void onBeforeAll() {
        fixture.onBefore();
    }

    @AfterAll
    public static void onAfterAll() {
        fixture.onAfter();
    }
    
    /* ... */
}
```

#### Step 4: Pass tokens provided by the test fixture with all your requests

Don't forget to pass at least the access token obtained during __RestAssuredSystemTestFixture#onBefore()__ with all your
requests by invoking __auth().preemptive().oauth2(fixture.getAccessToken())__ on RestAssured:

```java 
@Test
public void getWelcomeMessageWithTokenMustReturn200() {
    given().auth().preemptive().oauth2(fixture.getAccessToken())
            .get("api/v1/hello")
            .then().assertThat()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("text", response -> equalTo("Dear \"cnj-tester\", welcome to a cloud native java application protected by OpenID Connect"));
}
```


