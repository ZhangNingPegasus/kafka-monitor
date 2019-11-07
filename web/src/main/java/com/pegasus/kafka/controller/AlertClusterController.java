package com.pegasus.kafka.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysAlertCluster;
import com.pegasus.kafka.service.dto.SysAlertClusterService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The controller for providing the ability of alert for cluster.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping("alertcluster")
public class AlertClusterController {
    private final SysAlertClusterService sysAlertClusterService;

    public AlertClusterController(SysAlertClusterService sysAlertClusterService) {
        this.sysAlertClusterService = sysAlertClusterService;
    }

    @RequestMapping("tolist")
    public String toList() {
        return "alertcluster/list";
    }

    @RequestMapping("toadd")
    public String toAdd(Model model) {
        model.addAttribute("type", SysAlertCluster.Type.values());
        return "alertcluster/add";
    }

    @RequestMapping("toedit/{id}")
    public String toEdit(Model model,
                         @PathVariable(required = true, value = "id") String id) {
        SysAlertCluster sysAlertCluster = sysAlertClusterService.getById(id);
        model.addAttribute("item", sysAlertCluster);
        model.addAttribute("type", SysAlertCluster.Type.values());
        return "alertcluster/edit";
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<SysAlertCluster>> list(@RequestParam(value = "page", required = true) Integer pageNum,
                                              @RequestParam(value = "limit", required = true) Integer pageSize) {
        QueryWrapper<SysAlertCluster> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(SysAlertCluster::getCreateTime);
        return Result.success(this.sysAlertClusterService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }


    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "type", required = true) Integer type,
                         @RequestParam(value = "server", required = true) String server,
                         @RequestParam(value = "email", required = true) String email
    ) {
        sysAlertClusterService.save(type, server, email);
        return Result.success();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id", required = true) Long id,
                          @RequestParam(value = "type", required = true) Integer type,
                          @RequestParam(value = "server", required = true) String server,
                          @RequestParam(value = "email", required = true) String email
    ) {
        sysAlertClusterService.update(id, type, server, email);
        return Result.success();
    }


    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id", required = true) Long id) {
        sysAlertClusterService.removeById(id);
        return Result.success();
    }

}
