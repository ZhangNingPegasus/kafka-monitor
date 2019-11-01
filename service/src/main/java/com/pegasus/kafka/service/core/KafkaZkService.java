package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.ehcache.EhcacheService;
import com.pegasus.kafka.common.exception.BusinessException;
import com.pegasus.kafka.common.response.ResultCode;
import com.pegasus.kafka.entity.vo.ZooKeeperInfo;
import com.pegasus.kafka.service.property.KafkaMonitorProperty;
import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

@Service
public class KafkaZkService implements InitializingBean, DisposableBean {

    private final String CHARSET_NAME = "gbk";
    private final KafkaMonitorProperty kafkaMonitorProperty;
    private final EhcacheService ehcacheService;
    private CuratorFramework client;

    public KafkaZkService(KafkaMonitorProperty kafkaMonitorProperty, EhcacheService ehcacheService) {
        this.kafkaMonitorProperty = kafkaMonitorProperty;
        this.ehcacheService = ehcacheService;
    }

    public List<ZooKeeperInfo> listZooKeeperCluster() {
        String[] zks = kafkaMonitorProperty.getZookeeper().split(",");
        List<ZooKeeperInfo> result = new ArrayList<>(zks.length);
        for (String zk : zks) {
            ZooKeeperInfo zooKeeperInfo = new ZooKeeperInfo();
            zooKeeperInfo.setHost(zk.split(":")[0]);
            zooKeeperInfo.setPort(zk.split(":")[1].split("/")[0]);
            ZkStatus status = status(zooKeeperInfo.getHost(), zooKeeperInfo.getPort());
            zooKeeperInfo.setMode(status.getMode());
            zooKeeperInfo.setVersion(status.getVersion());
            result.add(zooKeeperInfo);
        }
        return result;
    }

    private ZkStatus status(String host, String port) {
        ZkStatus zkStatus = new ZkStatus();
        Socket sock;
        try {
            String tmp;
            if (port.contains("/")) {
                tmp = port.split("/")[0];
            } else {
                tmp = port;
            }
            sock = new Socket(host, Integer.parseInt(tmp));
        } catch (Exception e) {
            zkStatus.setMode("death");
            zkStatus.setVersion("death");
            return zkStatus;
        }
        BufferedReader reader = null;
        OutputStream outstream = null;
        try {
            outstream = sock.getOutputStream();
            outstream.write("stat".getBytes());
            outstream.flush();
            sock.shutdownOutput();

            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("mode: ")) {
                    zkStatus.setMode(line.replaceAll("Mode: ", "").trim());
                } else if (line.toLowerCase().contains("version")) {
                    zkStatus.setVersion(line.split(":")[1].split("-")[0].trim());
                }
            }
        } catch (Exception ex) {
            zkStatus.setMode("death");
            zkStatus.setVersion("death");
            return zkStatus;
        } finally {
            try {
                sock.close();
                if (reader != null) {
                    reader.close();
                }
                if (outstream != null) {
                    outstream.close();
                }
            } catch (Exception ignored) {
            }
        }
        return zkStatus;
    }

    List<String> getChildren(String path) throws Exception {
        return client.getChildren().forPath(path);
    }

    public String getData(String path) throws Exception {
        return new String(client.getData().forPath(path), CHARSET_NAME);
    }

    public String getData(String path, Stat stat) throws Exception {
        return new String(client.getData().storingStatIn(stat).forPath(path), CHARSET_NAME);
    }

    public boolean exists(String path) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        return stat != null;
    }

    public void remove(String path) throws Exception {
        if (exists(path)) {
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        }
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

    public String execute(String command, String type) throws Exception {
        String result = "";
        String[] len = command.replaceAll(" ", "").split(type);
        if (len.length == 0) {
            return command + " has error";
        } else {
            String cmd = len[1];
            switch (type) {
                case "get":
                    result = getData(cmd);
                    break;
                case "ls":
                    result = getChildren(cmd).toString();
                    break;
                default:
                    result = "Invalid command";
                    break;
            }
        }
        return result;
    }

    @Data
    private static class ZkStatus {
        private String mode;
        private String version;
    }
}