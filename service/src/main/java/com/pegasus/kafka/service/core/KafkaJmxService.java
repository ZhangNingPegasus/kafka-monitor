package com.pegasus.kafka.service.core;

import com.pegasus.kafka.common.utils.JMXFactoryUtils;
import com.pegasus.kafka.entity.vo.KafkaBrokerVo;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The service for kafka's JMX.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class KafkaJmxService {
    private static final String JMX_URL;
    private static final Integer TIME_OUT = 10;

    static {
        JMX_URL = "service:jmx:rmi:///jndi/rmi://%s/jmxrmi";
    }

    public String getData(KafkaBrokerVo brokerVo, String name, String attribute) throws Exception {
        String result;
        JMXConnector connector = null;
        try {
            JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX_URL, String.format("%s:%s", brokerVo.getHost(), brokerVo.getJmxPort())));
            connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, TIME_OUT, TimeUnit.SECONDS);
            MBeanServerConnection mbeanConnection = connector.getMBeanServerConnection();
            result = mbeanConnection.getAttribute(ObjectName.getInstance(name), attribute).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (connector != null) {
                connector.close();
            }
        }
        return result;
    }

    public String[] getData(KafkaBrokerVo brokerVo, String[] names, String[] attributes) throws Exception {
        List<String> results = new ArrayList<>(names.length);
        JMXConnector connector = null;
        try {
            JMXServiceURL jmxSeriverUrl = new JMXServiceURL(String.format(JMX_URL, String.format("%s:%s", brokerVo.getHost(), brokerVo.getJmxPort())));
            connector = JMXFactoryUtils.connectWithTimeout(jmxSeriverUrl, TIME_OUT, TimeUnit.SECONDS);
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
