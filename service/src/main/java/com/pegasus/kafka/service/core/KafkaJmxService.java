package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.utils.JMXFactoryUtils;
import com.pegasus.kafka.entity.vo.BrokerVo;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaJmxService {
    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

    public String getData(BrokerVo brokerInfo, String name, String attribute) throws Exception {
        String result;
        JMXConnector connector = null;
        try {
            JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX_URL, brokerInfo.getHost() + ":" + brokerInfo.getJmxPort()));
            connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
            MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
            result = mbeanConnection.getAttribute(ObjectName.getInstance(name), attribute).toString();
        } finally {
            if (connector != null) {
                connector.close();
            }
        }
        return result;
    }
}
