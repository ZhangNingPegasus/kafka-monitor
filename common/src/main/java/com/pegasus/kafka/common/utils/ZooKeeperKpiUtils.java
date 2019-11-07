package com.pegasus.kafka.common.utils;


import lombok.Data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * the utils class for ZooKeeper.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
public class ZooKeeperKpiUtils {

    private static final String zk_avg_latency = "zk_avg_latency";
    private static final String zk_packets_received = "zk_packets_received";
    private static final String zk_packets_sent = "zk_packets_sent";
    private static final String zk_num_alive_connections = "zk_num_alive_connections";
    private static final String zk_outstanding_requests = "zk_outstanding_requests";
    private static final String zk_open_file_descriptor_count = "zk_open_file_descriptor_count";
    private static final String zk_max_file_descriptor_count = "zk_max_file_descriptor_count";

    public static ZooKeeperKpi listKpi(String ip, int port) {
        ZooKeeperKpi result = new ZooKeeperKpi();
        Socket sock;
        try {
            sock = new Socket(ip, port);
        } catch (Exception e) {
            return result;
        }
        BufferedReader reader = null;
        OutputStream outstream = null;
        try {
            outstream = sock.getOutputStream();
            outstream.write("mntr".getBytes());
            outstream.flush();
            sock.shutdownOutput();

            reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] rs = line.split("\\s+");
                try {
                    switch (rs[0]) {
                        case zk_avg_latency:
                            result.setZkAvgLatency(rs[1]);
                            break;
                        case zk_packets_received:
                            result.setZkPacketsReceived(rs[1]);
                            break;
                        case zk_packets_sent:
                            result.setZkPacketsSent(rs[1]);
                            break;
                        case zk_num_alive_connections:
                            result.setZkNumAliveConnections(rs[1]);
                            break;
                        case zk_outstanding_requests:
                            result.setZkOutstandingRequests(rs[1]);
                            break;
                        case zk_open_file_descriptor_count:
                            result.setZkOpenFileDescriptorCount(rs[1]);
                            break;
                        case zk_max_file_descriptor_count:
                            result.setZkMaxFileDescriptorCount(rs[1]);
                            break;
                        default:
                            break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            return result;
        } finally {
            try {
                sock.close();
                if (reader != null) {
                    reader.close();
                }
                if (outstream != null) {
                    outstream.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    @Data
    public static class ZooKeeperKpi {
        private String zkPacketsReceived;// received client packet numbers
        private String zkPacketsSent;// send client packet numbers
        private String zkAvgLatency;// response client request avg time
        private String zkNumAliveConnections;// has connected client numbers
        private String zkOutstandingRequests; //waiting deal with client request numbers in queue.
        private String zkOpenFileDescriptorCount; //server mode,like standalone|cluster[leader,follower].
        private String zkMaxFileDescriptorCount;
    }
}
