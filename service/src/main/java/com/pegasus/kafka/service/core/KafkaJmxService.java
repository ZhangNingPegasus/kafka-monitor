package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.utils.JMXFactoryUtils;
import com.pegasus.kafka.entity.vo.BrokerVo;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class KafkaJmxService {
    private static final String JMX_URL = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";

    public String getData(BrokerVo brokerVo, String name, String attribute) throws Exception {
        String result;
        JMXConnector connector = null;
        try {
            JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX_URL, brokerVo.getHost() + ":" + brokerVo.getJmxPort()));
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

    public String[] getData(BrokerVo brokerVo, String[] names, String[] attributes) throws Exception {
        List<String> results = new ArrayList<>(names.length);
        JMXConnector connector = null;
        try {
            JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX_URL, brokerVo.getHost() + ":" + brokerVo.getJmxPort()));
            connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, 30, TimeUnit.SECONDS);
            MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
            for (int i = 0; i < names.length; i++) {
                results.add(mbeanConnection.getAttribute(ObjectName.getInstance(names[i]), attributes[i]).toString());
            }
        } finally {
            if (connector != null) {
                connector.close();
            }
        }
        return results.toArray(new String[]{});
    }
}
