package com.pegasus.kafka.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysPermission;
import com.pegasus.kafka.entity.dto.SysRole;
import com.pegasus.kafka.entity.vo.PermissionVo;
import com.pegasus.kafka.service.dto.SysPageService;
import com.pegasus.kafka.service.dto.SysPermissionService;
import com.pegasus.kafka.service.dto.SysRoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.pegasus.kafka.controller.PermissionController.PREFIX;


/**
 * The controller for role's permission.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class PermissionController {
    public static final String PREFIX = "permission";

    private final SysPermissionService sysPermissionService;
    private final SysRoleService sysRoleService;
    private final SysPageService sysPageService;

    public PermissionController(SysPermissionService sysPermissionService, SysRoleService sysRoleService, SysPageService sysPageService) {
        this.sysPermissionService = sysPermissionService;
        this.sysRoleService = sysRoleService;
        this.sysPageService = sysPageService;
    }

    @GetMapping("tolist")
    public String toList(Model model) {
        model.addAttribute("roles", sysRoleService.list(new QueryWrapper<SysRole>().lambda().eq(SysRole::getSuperAdmin, false).orderByAsc(SysRole::getCreateTime)));
        model.addAttribute("pages", sysPageService.list().stream().filter(p -> !StringUtils.isEmpty(p.getUrl())).collect(Collectors.toList()));
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(Model model) {
        model.addAttribute("roles", sysRoleService.list(new QueryWrapper<SysRole>().lambda().eq(SysRole::getSuperAdmin, false).orderByAsc(SysRole::getCreateTime)));
        model.addAttribute("pages", sysPageService.list().stream().filter(p -> !StringUtils.isEmpty(p.getUrl())).collect(Collectors.toList()));
        return String.format("%s/%s", PREFIX, "add");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<PermissionVo>> list(@RequestParam(value = "page", required = true) Integer pageNum,
                                           @RequestParam(value = "limit", required = true) Integer pageSize,
                                           @RequestParam(value = "sysRoleId", required = false) Long sysRoleId,
                                           @RequestParam(value = "sysPageId", required = false) Long sysPageId) {
        return Result.ok(sysPermissionService.list(pageNum, pageSize, sysRoleId, sysPageId));
    }


    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "sysRoleId", required = true) Long sysRoleId,
                         @RequestParam(value = "sysPageId", required = true) Long sysPageId,
                         @RequestParam(value = "insert", required = true) Boolean insert,
                         @RequestParam(value = "delete", required = true) Boolean delete,
                         @RequestParam(value = "update", required = true) Boolean update,
                         @RequestParam(value = "select", required = true) Boolean select) {
        SysPermission authPermission = new SysPermission();
        authPermission.setSysRoleId(sysRoleId);
        authPermission.setSysPageId(sysPageId);
        authPermission.setCanInsert(insert);
        authPermission.setCanDelete(delete);
        authPermission.setCanUpdate(update);
        authPermission.setCanSelect(select);
        sysPermissionService.save(authPermission);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id", required = true) Long id,
                          @RequestParam(value = "type", required = true) String type,
                          @RequestParam(value = "hasPermission", required = true) Boolean hasPermission) {
        SysPermission dbAdminPermission = sysPermissionService.getById(id);
        if (dbAdminPermission != null) {
            switch (type.toLowerCase()) {
                case "insert":
                    dbAdminPermission.setCanInsert(hasPermission);
                    break;
                case "delete":
                    dbAdminPermission.setCanDelete(hasPermission);
                    break;
                case "update":
                    dbAdminPermission.setCanUpdate(hasPermission);
                    break;
                case "select":
                    dbAdminPermission.setCanSelect(hasPermission);
                    break;
            }
            sysPermissionService.updateById(dbAdminPermission);
        }
        return Result.ok();
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "ids", required = true) String ids) {
        String[] idsArray = ids.split(",");
        List<Long> idsList = new ArrayList<>(idsArray.length);
        for (String id : idsArray) {
            if (id != null && !StringUtils.isEmpty(id.trim())) {
                idsList.add(Common.toLong(id));
            }
        }
        sysPermissionService.removeByIds(idsList);
        return Result.ok();
    }
}
