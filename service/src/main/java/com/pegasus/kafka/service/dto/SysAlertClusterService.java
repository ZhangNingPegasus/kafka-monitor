package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pegasus.kafka.common.annotation.TranSave;
import com.pegasus.kafka.entity.dto.SysAlertCluster;
import com.pegasus.kafka.mapper.SysAlertClusterMapper;
import org.springframework.stereotype.Service;

@Service
public class SysAlertClusterService extends ServiceImpl<SysAlertClusterMapper, SysAlertCluster> {

    @TranSave
    public boolean save(Integer type, String server, String email) {
        SysAlertCluster sysAlertCluster = new SysAlertCluster();
        sysAlertCluster.setType(type);
        sysAlertCluster.setServer(server);
        sysAlertCluster.setEmail(email);
        return this.save(sysAlertCluster);
    }

    @TranSave
    public boolean update(Long id, Integer type, String server, String email) {
        UpdateWrapper<SysAlertCluster> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(SysAlertCluster::getId, id)
                .set(SysAlertCluster::getType, type)
                .set(SysAlertCluster::getServer, server)
                .set(SysAlertCluster::getEmail, email);
        return this.update(updateWrapper);
    }
}
