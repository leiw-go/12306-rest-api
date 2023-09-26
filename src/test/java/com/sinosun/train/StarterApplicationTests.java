package com.sinosun.train;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StarterApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Value("${sleepTime}")
    private String SLEEP_TIME;

    @Test
    public void printValue() {
        System.out.println("SLEEP TIME: " + SLEEP_TIME);
    }

}

