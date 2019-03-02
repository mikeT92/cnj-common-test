package edu.hm.cs.fwp.cloud.common.test.adapter.rest;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link RestAssuredSystemTestFixture}.
 */
public class RestAssuredSystemTestFixtureTest {

    private RestAssuredSystemTestFixture underTest = new RestAssuredSystemTestFixture();

    @Test
    public void checkIfFixtureWorks() {
        this.underTest.onBefore();
        this.underTest.onAfter();
    }
}
