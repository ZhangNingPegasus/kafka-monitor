package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.echarts.TreeInfo;
import com.pegasus.kafka.entity.vo.KafkaBrokerVo;
import com.pegasus.kafka.entity.vo.ZooKeeperVo;
import com.pegasus.kafka.service.core.KafkaService;
import com.pegasus.kafka.service.core.KafkaZkService;
import com.pegasus.kafka.service.kafka.KafkaBrokerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

import static com.pegasus.kafka.controller.ClusterController.PREFIX;

/**
 * The controller for providing the ability of cluster.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class ClusterController {
    public static final String PREFIX = "cluster";
    private final KafkaBrokerService kafkaClusterService;
    private final KafkaZkService kafkaZkService;
    private final KafkaService kafkaService;

    public ClusterController(KafkaBrokerService kafkaClusterService,
                             KafkaZkService kafkaZkService,
                             KafkaService kafkaService) {
        this.kafkaClusterService = kafkaClusterService;
        this.kafkaZkService = kafkaZkService;
        this.kafkaService = kafkaService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("tosetting")
    public String tosetting(Model model) throws Exception {

        List<KafkaBrokerVo> kafkaBrokerVos = kafkaService.listBrokerInfos();

        StringBuilder brokers = new StringBuilder();
        for (KafkaBrokerVo kafkaBrokerVo : kafkaBrokerVos) {
            brokers.append(String.format("%s:%s,", kafkaBrokerVo.getHost(), kafkaBrokerVo.getPort()));
        }
        String kafkaUrl = "";
        if (brokers.length() > 0) {
            kafkaUrl = brokers.substring(0, brokers.length() - 1);
        }


        String springboot = String.format("spring:\n" +
                "  kafka:\n" +
                "    #kafka集群地址，多个用逗号隔开\n" +
                "    bootstrap-servers: %s\n" +
                "    \n" +
                "    listener:\n" +
                "      #当topic不存在时，是否保存\n" +
                "      missing-topics-fatal: false\n" +
                "    \n" +
                "    #--------------------------------------------------------------发送端--------------------------------------------------------------\n" +
                "    producer:\n" +
                "      #ID在发出请求时传递给服务器;用于服务器端日志记录。\n" +
                "      client-id: your_producer_client_id\n" +
                "      #如果该值大于零时，表示启用重试失败的发送次数\n" +
                "      retries: 3\n" +
                "    \n" +
                "      #Consumer每次调用poll()时取到的records的最大数\n" +
                "      batch-size: 1024\n" +
                "    \n" +
                "      #0: 消息发送出去，不管kafka有无落到磁盘，都直接就认为消息发送成功;\n" +
                "      #1: 只要Leader分区接收到消息且写入磁盘，就认为成功;\n" +
                "      #all: Leader成功，且ISR列表里跟Leader保持同步的那些Follower都要把消息同步过去，才认为消息写入成功了\n" +
                "      acks: all\n" +
                "    \n" +
                "      #消息压缩算法, 支持gzip、snappy、lz4三种 \n" +
                "      compression-type: lz4\n" +
                "    \n" +
                "      key-serializer: org.apache.kafka.common.serialization.StringSerializer\n" +
                "      value-serializer: org.apache.kafka.common.serialization.StringSerializer\n" +
                "    \n" +
                "    #--------------------------------------------------------------消费端--------------------------------------------------------------\n" +
                "    consumer:\n" +
                "      #消费组名称，名称相同的视为同一个消费组。同一个消费组内属于单播模式，不同消费组之间属于广播模式 \n" +
                "      group-id: your_consumer_group_name\n" +
                "      #ID在发出请求时传递给服务器;用于服务器端日志记录。\n" +
                "      client-id: your_consumer_client_id\n" +
                "    \n" +
                "      #earliest: 从上次消费的位置接着消费，如果没有，则从0开始消费;\n" +
                "      #latest: 从上次消费的位置接着消费，如果没有，则从这个时间节点后发送的消息开始消费;\n" +
                "      #none: 从上次消费的位置接着消费，如果没有，则抛出异常\n" +
                "      auto-offset-reset: earliest\n" +
                "    \n" +
                "      #如果为true，则消费者的偏移量将在后台定期提交，默认值为true\n" +
                "      enable-auto-commit: true\n" +
                "    \n" +
                "      #如果'enable.auto.commit'为true，则消费者偏移自动提交给Kafka的频率（以毫秒为单位），默认值为5000。\n" +
                "      auto-commit-interval: 1000\n" +
                "    \n" +
                "      #事务隔离级别: \n" +
                "      #read_uncommitted: 读取所有消息;\n" +
                "      #read_committed: 读取没有开启事务的消息，也可读取开启事务并已成功提交的消息\n" +
                "      isolation-level: read_committed\n" +
                "    \n" +
                "      #Consumer每次调用poll()时取到的records的最大数\n" +
                "      max-poll-records: 2048\n" +
                "    \n" +
                "      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer\n" +
                "      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer", kafkaUrl);

        String spring = String.format("#--------------------------------------------------------------消费端--------------------------------------------------------------\n" +
                "#kafka集群地址，多个用逗号隔开\n" +
                "spring.kafka.consumer.bootstrap-servers=%s;\n" +
                "\n" +
                "#如果'enable.auto.commit'为true，则消费者偏移自动提交给Kafka的频率（以毫秒为单位），默认值为5000。\n" +
                "spring.kafka.consumer.auto-commit-interval=1000;\n" +
                "\n" +
                "#earliest: 从上次消费的位置接着消费，如果没有，则从0开始消费;\n" +
                "#latest: 从上次消费的位置接着消费，如果没有，则从这个时间节点后发送的消息开始消费;\n" +
                "#none: 从上次消费的位置接着消费，如果没有，则抛出异常\n" +
                "spring.kafka.consumer.auto-offset-reset=earliest;\n" +
                "\n" +
                "#ID在发出请求时传递给服务器;用于服务器端日志记录。\n" +
                "spring.kafka.consumer.client-id=your_consumer_client_id;\n" +
                "\n" +
                "#如果为true，则消费者的偏移量将在后台定期提交，默认值为true\n" +
                "spring.kafka.consumer.enable-auto-commit=true;\n" +
                "\n" +
                "#如果没有足够的数据立即满足“fetch.min.bytes”给出的要求，服务器在回答获取请求之前将阻塞的最长时间（以毫秒为单位）,默认值为500\n" +
                "spring.kafka.consumer.fetch-max-wait;\n" +
                "\n" +
                "#服务器应以字节为单位返回获取请求的最小数据量，默认值为1，对应的kafka的参数为fetch.min.bytes。\n" +
                "spring.kafka.consumer.fetch-min-size=1;\n" +
                "\n" +
                "#消费组名称，名称相同的视为同一个消费组。同一个消费组内属于单播模式，不同消费组之间属于广播模式 \n" +
                "spring.kafka.consumer.group-id=your_consumer_group_name;\n" +
                "\n" +
                "#心跳与消费者协调员之间的预期时间（以毫秒为单位），默认值为3000\n" +
                "spring.kafka.consumer.heartbeat-interval=3000;\n" +
                "\n" +
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer\n" +
                "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer\n" +
                "\n" +
                "#一次调用poll()操作时返回的最大记录数，默认值为500\n" +
                "spring.kafka.consumer.max-poll-records=1024;\n" +
                "\n" +
                "#--------------------------------------------------------------生产端--------------------------------------------------------------\n" +
                "#0: 消息发送出去，不管kafka有无落到磁盘，都直接就认为消息发送成功;\n" +
                "#1: 只要Leader分区接收到消息且写入磁盘，就认为成功;\n" +
                "#all: Leader成功，且ISR列表里跟Leader保持同步的那些Follower都要把消息同步过去，才认为消息写入成功了\n" +
                "spring.kafka.producer.acks=all\n" +
                "\n" +
                "#每当多个消息被发送到同一分区时，生产者将尝试将消息一起批量处理，有助于提升性能，以字节为单位，默认值为16384\n" +
                "spring.kafka.producer.batch-size=16384\n" +
                "\n" +
                "#kafka集群地址，多个用逗号隔开\n" +
                "spring.kafka.producer.bootstrap-servers=%s\n" +
                "\n" +
                "#生产者可用于缓冲等待发送到服务器的记录的内存总字节数，默认值为33554432\n" +
                "spring.kafka.producer.buffer-memory=33554432\n" +
                "\n" +
                "#ID在发出请求时传递给服务器，用于服务器端日志记录\n" +
                "spring.kafka.producer.client-id=your_producer_client_id\n" +
                "\n" +
                "#消息压缩算法, 支持gzip、snappy、lz4三种 \n" +
                "spring.kafka.producer.compression-type=lz4\n" +
                "\n" +
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer\n" +
                "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer\n" +
                "\n" +
                "#如果该值大于零时，表示启用重试失败的发送次数\n" +
                "spring.kafka.producer.retries=3\n" +
                "\n" +
                "#--------------------------------------------------------------监听端--------------------------------------------------------------\n" +
                "#侦听器的AckMode,当enable.auto.commit的值设置为false时，该值c才会生效:\n" +
                "#RECORD: 每处理一条commit一次\n" +
                "#BATCH(默认): 每次poll的时候批量提交一次，频率取决于每次poll的调用频率\n" +
                "#TIME: 每次间隔ackTime的时间去commit\n" +
                "#COUNT: 累积达到ackCount次的ack去commit\n" +
                "#COUNT_TIME: ackTime或ackCount哪个条件先满足，就commit\n" +
                "#MANUAL: listener负责ack，但是背后也是批量上去\n" +
                "#MANUAL_IMMEDIATE: listner负责ack，每调用一次，就立即commit\n" +
                "spring.kafka.listener.ack-mode=BATCH;\n" +
                "\n" +
                "#在侦听器容器中运行的线程数\n" +
                "spring.kafka.listener.concurrency=3;\n" +
                "\n" +
                "#轮询消费者时使用的超时（以毫秒为单位）\n" +
                "spring.kafka.listener.poll-timeout=60000;\n" +
                "\n" +
                "#当ackMode为'COUNT'或'COUNT_TIME'时，偏移提交之间的记录数\n" +
                "spring.kafka.listener.ack-count=1;\n" +
                "\n" +
                "#当ackMode为'TIME'或'COUNT_TIME'时，偏移提交之间的时间（以毫秒为单位）\n" +
                "spring.kafka.listener.ack-time=1000;\n", kafkaUrl, kafkaUrl);

        String java = String.format("package com.xxx.xxx.xxx;\n" +
                "\n" +
                "import org.apache.kafka.clients.CommonClientConfigs;\n" +
                "import org.apache.kafka.clients.consumer.ConsumerConfig;\n" +
                "import org.apache.kafka.clients.consumer.ConsumerRecord;\n" +
                "import org.apache.kafka.clients.consumer.ConsumerRecords;\n" +
                "import org.apache.kafka.clients.consumer.KafkaConsumer;\n" +
                "import org.apache.kafka.clients.producer.KafkaProducer;\n" +
                "import org.apache.kafka.clients.producer.ProducerConfig;\n" +
                "import org.apache.kafka.clients.producer.ProducerRecord;\n" +
                "\n" +
                "import java.time.Duration;\n" +
                "import java.util.Collections;\n" +
                "import java.util.Properties;\n" +
                "import java.util.concurrent.ExecutionException;\n" +
                "\n" +
                "public class Producer {\n" +
                "    public static void main(String[] args) throws InterruptedException, ExecutionException {\n" +
                "        //--------------------------------------------------------------发送端--------------------------------------------------------------\n" +
                "        Properties properties1 = new Properties();\n" +
                "        //kafka集群地址，多个用逗号隔开\n" +
                "        properties1.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, \"%s\");\n" +
                "\n" +
                "        //0: 消息发送出去，不管kafka有无落到磁盘，都直接就认为消息发送成功;\n" +
                "        //1: 只要Leader分区接收到消息且写入磁盘，就认为成功;\n" +
                "        //all: Leader成功，且ISR列表里跟Leader保持同步的那些Follower都要把消息同步过去，才认为消息写入成功了\n" +
                "        properties1.put(ProducerConfig.ACKS_CONFIG, \"all\");\n" +
                "\n" +
                "        //消息压缩算法, 支持gzip、snappy、lz4三种 \n" +
                "        properties1.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, \"lz4\");\n" +
                "\n" +
                "        //给定一个事务id, 从而开启kafka消息发生端的事务, 事务id自定义,如果不需要，屏蔽即可\n" +
                "        //properties.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, \"your_transactional_id\");\n" +
                "\n" +
                "        //ID在发出请求时传递给服务器;用于服务器端日志记录。\n" +
                "        props.put(ProducerConfig.CLIENT_ID_CONFIG, \"your_producer_client_id\");\n" +
                "\n" +
                "        //key的序列化类 \n" +
                "        properties1.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getCanonicalName());\n" +
                "\n" +
                "        //value的序列化类 \n" +
                "        properties1.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getCanonicalName());\n" +
                "\n" +
                "        org.apache.kafka.clients.producer.Producer<String, String> producer = new KafkaProducer<>(properties1);\n" +
                "\n" +
                "        //初始化事务\n" +
                "        //producer.initTransactions();\n" +
                "        //开启事务\n" +
                "        //producer.beginTransaction();\n" +
                "\n" +
                "        producer.send(new ProducerRecord<>(\"your_topic_name\", \"your_record_key\", \"your_record_value\"), (metadata, exception) -> {\n" +
                "            if (exception == null) {\n" +
                "                System.out.println(String.format(\"topic : %%s,  partition : %%s,   offset  :  %%s\",\n" +
                "                        metadata.topic(),\n" +
                "                        metadata.partition(),\n" +
                "                        metadata.offset()));\n" +
                "            } else {\n" +
                "                exception.printStackTrace();\n" +
                "            }\n" +
                "        }).get();\n" +
                "\n" +
                "        //提交事务\n" +
                "        //producer.commitTransaction();\n" +
                "        //回滚事务\n" +
                "        //producer.abortTransaction();\n" +
                "        producer.close();\n" +
                "\n" +
                "\n" +
                "        //--------------------------------------------------------------消费端--------------------------------------------------------------\n" +
                "        Properties properties2 = new Properties();\n" +
                "        properties2.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, \"%s\");\n" +
                "\n" +
                "        //消费组名称，名称相同的视为同一个消费组。同一个消费组内属于单播模式，不同消费组之间属于广播模式 \n" +
                "        properties2.setProperty(ConsumerConfig.GROUP_ID_CONFIG, \"your_consumer_group_name\");\n" +
                "\n" +
                "        //ID在发出请求时传递给服务器;用于服务器端日志记录。\n" +
                "        props.put(ConsumerConfig.CLIENT_ID_CONFIG, \"your_consumer_client_id\");\n" +
                "\n" +
                "        //如果为true，则消费者的偏移量将在后台定期提交，默认值为true\n" +
                "        properties2.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, \"true\");\n" +
                "\n" +
                "        //Consumer每次调用poll()时取到的records的最大数\n" +
                "        properties2.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, \"1024\");\n" +
                "\n" +
                "        //当Consumer由于某种原因不能发Heartbeat到coordinator时,且时间超过session.timeout.ms时,就会认为该consumer已退出,它所订阅的分区会分配到同一group内的其它的consumer上\n" +
                "        properties2.setProperty(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, String.valueOf(1000 * 60));\n" +
                "\n" +
                "        //表示最大的poll数据间隔，如果超过这个间隔没有发起pool请求，但heartbeat仍旧在发，就认为该consumer处于 livelock状态。就会将该consumer退出consumer group.\n" +
                "        //所以为了不使Consumer被退出，Consumer应该不停的发起poll(timeout)操作\n" +
                "        properties2.setProperty(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, String.valueOf(1000 * 60));\n" +
                "\n" +
                "        //如果'enable.auto.commit'为true，则消费者偏移自动提交给Kafka的频率（以毫秒为单位），默认值为5000。\n" +
                "        properties2.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, String.valueOf(1000));\n" +
                "\n" +
                "        //事务隔离级别: \n" +
                "        //read_uncommitted: 读取所有消息;\n" +
                "        //read_committed: 读取没有开启事务的消息，也可读取开启事务并已成功提交的消息\n" +
                "        properties2.setProperty(ConsumerConfig.ISOLATION_LEVEL_CONFIG, \"read_committed\");\n" +
                "\n" +
                "        //earliest: 从上次消费的位置接着消费，如果没有，则从0开始消费;\n" +
                "        //latest: 从上次消费的位置接着消费，如果没有，则从这个时间节点后发送的消息开始消费;\n" +
                "        //none: 从上次消费的位置接着消费，如果没有，则抛出异常\n" +
                "        properties2.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, \"earliest\");\n" +
                "\n" +
                "        //key的反序列化类 \n" +
                "        properties2.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getCanonicalName());\n" +
                "        //value的反序列化类 \n" +
                "        properties2.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getCanonicalName());\n" +
                "\n" +
                "        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties2);\n" +
                "        consumer.subscribe(Collections.singletonList(\"your_topic_name\"));\n" +
                "\n" +
                "        while (true) {\n" +
                "            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));\n" +
                "            for (ConsumerRecord<String, String> record : records) {\n" +
                "                System.out.printf(\"topic = %%s, partition = %%d, offset = %%d, key = %%s, value =%%s%%n\",\n" +
                "                        record.topic(),\n" +
                "                        record.partition(),\n" +
                "                        record.offset(),\n" +
                "                        record.key(),\n" +
                "                        record.value());\n" +
                "                //当ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG设置为false的时候，需要使用下面代码进行手动提交\n" +
                "                //consumer.commitAsync();\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}", kafkaUrl, kafkaUrl);

        model.addAttribute("strSpringboot", springboot);
        model.addAttribute("strSpring", spring);
        model.addAttribute("strJava", java);
        return String.format("%s/setting", PREFIX);
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<KafkaBrokerVo>> list() throws Exception {
        return Result.ok(kafkaClusterService.listAllBrokers());
    }

    @PostMapping("listZk")
    @ResponseBody
    public Result<List<ZooKeeperVo>> listZk() {
        return Result.ok(kafkaZkService.listZooKeeperCluster());
    }

    @PostMapping("getChartData")
    @ResponseBody
    public Result<TreeInfo> getChartData() throws Exception {
        TreeInfo root = new TreeInfo("Kafka集群");

        List<KafkaBrokerVo> allBrokers = kafkaClusterService.listAllBrokers();

        List<TreeInfo> children = new ArrayList<>(allBrokers.size());
        for (KafkaBrokerVo broker : allBrokers) {
            TreeInfo treeInfo = new TreeInfo(String.format("[%s] : %s", broker.getName(), broker.getHost()));
            treeInfo.setStyle(TreeInfo.Style.success());
            children.add(treeInfo);
        }
        if (children.size() > 0) {
            root.setChildren(children);
        }
        return Result.ok(root);
    }
}