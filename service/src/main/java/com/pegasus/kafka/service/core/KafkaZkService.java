package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.service.property.KafkaMonitorProperty;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class KafkaZkService implements InitializingBean, DisposableBean {

    private final KafkaMonitorProperty kafkaMonitorProperty;
    private CuratorFramework client;

    public KafkaZkService(KafkaMonitorProperty kafkaMonitorProperty) {
        this.kafkaMonitorProperty = kafkaMonitorProperty;
    }

    public List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    public String getData(String path) throws Exception {
        return new String(client.getData().forPath(path), "gbk");
    }

    public String getData(String path, Stat stat) throws Exception {
        return new String(client.getData().storingStatIn(stat).forPath(path), "gbk");
    }


    @Override
    public void afterPropertiesSet() {
        if (StringUtils.isEmpty(kafkaMonitorProperty.getZookeeper())) {
            throw new BusinessException(ResultCode.ZOOKEEPER_CONFIG_IS_NULL);
        }
        this.client = CuratorFrameworkFactory.builder().connectString(kafkaMonitorProperty.getZookeeper())
                // 连接超时时间
                .sessionTimeoutMs(3000)
                // 会话超时时间
                .connectionTimeoutMs(1000)
                // 刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过五次
                .retryPolicy(new ExponentialBackoffRetry(1000, 5))
                .build();
        client.start();
    }

    @Override
    public void destroy() {
        if (this.client != null) {
            CloseableUtils.closeQuietly(this.client);
        }
    }

}
