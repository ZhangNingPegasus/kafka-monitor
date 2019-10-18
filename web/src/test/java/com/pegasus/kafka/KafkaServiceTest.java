package com.pegasus.kafka;

import com.pegasus.kafka.service.core.KafkaService;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KafkaServiceTest {

    @Autowired
    private KafkaService kafkaService;

    @Test
    public void test() {
        kafkaService.listTopicParitions("AAA");
    }

}
