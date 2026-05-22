package com.cognizant.PredictiveMaintenanceLite;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "app.simulator.enabled=false"
})
class PredictiveMaintenanceLiteApplicationTests {

    @Test
    void contextLoads() {
        // Smoke test: app context boots cleanly
    }
}
