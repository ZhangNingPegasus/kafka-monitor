package com.pegasus.kafka.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pegasus.kafka.common.constant.Constants;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysAdmin;
import com.pegasus.kafka.entity.vo.AdminInfo;
import com.pegasus.kafka.service.dto.SysAdminService;
import com.pegasus.kafka.service.dto.SysRoleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.pegasus.kafka.controller.AdminController.PREFIX;


/**
 * The controller for system administration.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class AdminController {
    public static final String PREFIX = "admin";

    private final SysAdminService sysAdminService;
    private final SysRoleService sysRoleService;

    public AdminController(SysAdminService sysAdminService, SysRoleService sysRoleService) {
        this.sysAdminService = sysAdminService;
        this.sysRoleService = sysRoleService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @GetMapping("toadd")
    public String toAdd(Model model) {
        model.addAttribute("roles", sysRoleService.listOrderByName());
        return String.format("%s/%s", PREFIX, "add");
    }

    @GetMapping("toedit")
    public String toEdit(Model model,
                         @RequestParam(name = "id", required = true) Long id) {
        model.addAttribute("admin", sysAdminService.getById(id));
        model.addAttribute("roles", sysRoleService.listOrderByName());
        return String.format("%s/%s", PREFIX, "edit");
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<AdminInfo>> list(@RequestParam(value = "name", required = false) String name,
                                        @RequestParam(value = "page", required = true) Integer pageNum,
                                        @RequestParam(value = "limit", required = true) Integer pageSize) {
        pageNum = Math.min(pageNum, Constants.MAX_PAGE_NUM);
        IPage<AdminInfo> sysAdmins = sysAdminService.list(pageNum, pageSize, name);
        return Result.ok(sysAdmins);
    }

    @PostMapping("repwd")
    @ResponseBody
    public Result<?> repwd(@RequestParam(value = "id", required = true) Long id) {
        if (sysAdminService.resetPassword(id)) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(@RequestParam(value = "roleId", required = true) Long roleId,
                         @RequestParam(value = "username", required = true) String username,
                         @RequestParam(value = "password", required = true) String password,
                         @RequestParam(value = "name", required = true) String name,
                         @RequestParam(value = "gender", required = true) Boolean gender,
                         @RequestParam(value = "phoneNumber", required = true) String phoneNumber,
                         @RequestParam(value = "email", required = true) String email,
                         @RequestParam(value = "remark", required = true) String remark) {
        if (sysAdminService.add(roleId, username, password, name, gender, phoneNumber, email, remark) > 0) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(@RequestParam(value = "id", required = true) Long id,
                          @RequestParam(value = "roleId", required = true) Long roleId,
                          @RequestParam(value = "username", required = true) String username,
                          @RequestParam(value = "name", required = true) String name,
                          @RequestParam(value = "gender", required = true) Boolean gender,
                          @RequestParam(value = "phoneNumber", required = true) String phoneNumber,
                          @RequestParam(value = "email", required = true) String email,
                          @RequestParam(value = "remark", required = true) String remark) {
        if (sysAdminService.edit(id, roleId, username, name, gender, phoneNumber, email, remark)) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

    @PostMapping("del")
    @ResponseBody
    public Result<?> del(@RequestParam(value = "id", required = true) Long id,
                         @RequestParam(value = "username", required = true) String username) {
        if (Constants.DEFAULT_ADMIN_USER_NAME.equals(username)) {
            return Result.error("系统内置账户不能删除");
        }

        if (sysAdminService.removeById(id)) {
            return Result.ok();
        } else {
            return Result.error();
        }
    }

}
