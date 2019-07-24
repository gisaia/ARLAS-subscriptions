package io.arlas.subscriptions.rest;

import io.arlas.subscriptions.AbstractTestContext;
import org.hamcrest.Matcher;
import org.junit.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class UserSubscriptionManagerServiceIT extends AbstractTestContext {

    @Test
    public void testGetAllUserSubscriptions() throws Exception {
        // GET all collections
        getAllUserSubscriptions(emptyArray());
    }

    private void getAllUserSubscriptions(Matcher matcher) throws InterruptedException {
        int cpt = 0;
        while (cpt > 0 && cpt < 5) {
            try {
                when().get(arlasSubManagerPath + "subscriptions/")
                        .then().statusCode(200)
                        .body(matcher);
                cpt = -1;
            } catch (Exception e) {
                cpt++;
                Thread.sleep(1000);
            }
        }
    }
}
