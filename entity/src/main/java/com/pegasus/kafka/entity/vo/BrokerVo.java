package com.pegasus.kafka.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

@JsonSerialize
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class BrokerVo implements Serializable {
    private String name;
    private String host;
    private String port;
    private String jmxPort;
    private String endpoints;
    private String version;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(String jmxPort) {
        this.jmxPort = jmxPort;
    }

    public String getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "BrokerInfo{" +
                "name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", jmxPort='" + jmxPort + '\'' +
                ", endpoints='" + endpoints + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
