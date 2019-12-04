package com.pegasus.kafka.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysAdmin;
import com.pegasus.kafka.entity.dto.SysRole;
import com.pegasus.kafka.service.dto.SysAdminService;
import com.pegasus.kafka.service.dto.SysRoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.pegasus.kafka.controller.RoleController.PREFIX;


/**
 * The controller for system administration's role.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class RoleController {
    public static final String PREFIX = "role";

    private final SysAdminService sysAdminService;
    private final SysRoleService sysRoleService;


    public RoleController(SysAdminService sysAdminService, SysRoleService sysRoleService) {
        this.sysAdminService = sysAdminService;
        this.sysRoleService = sysRoleService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd() {
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(Model model,
                         @RequestParam(name = "id", required = true) Long id) {
        model.addAttribute("role", sysRoleService.getById(id));
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<SysRole>> list(@RequestParam(value = "name", required = false) String name,
                                      @RequestParam(value = "page", required = true) Integer pageNum,
                                      @RequestParam(value = "limit", required = true) Integer pageSize) {
        pageNum = Math.min(pageNum, Constants.MAX_PAGE_NUM);
        IPage<SysRole> sysAdmins = sysRoleService.list(pageNum, pageSize, name);
        return Result.ok(sysAdmins);
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "name", required = true) String name,
                         @RequestParam(value = "superAdmin", required = true) boolean superAdmin,
                         @RequestParam(value = "remark", required = true) String remark) {
        if (sysRoleService.add(name, superAdmin, remark) > 0) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id", required = true) Long id,
                          @RequestParam(value = "name", required = true) String name,
                          @RequestParam(value = "superAdmin", required = true) boolean superAdmin,
                          @RequestParam(value = "remark", required = true) String remark) {
        if (sysRoleService.edit(id, name, superAdmin, remark)) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id", required = true) Long id,
                         @RequestParam(value = "name", required = true) String name) {
        if (Constants.SYSTEM_ROLE_NAME.equals(name)) {
            return Result.error("系统内置角色不能删除");
        }

        List<SysAdmin> sysAdminList = sysAdminService.getByRoleId(id);
        if (sysAdminList != null && sysAdminList.size() > 0) {
            return Result.error("该角色有管理正在使用,无法删除");
        }

        if (sysRoleService.removeById(id)) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

}
