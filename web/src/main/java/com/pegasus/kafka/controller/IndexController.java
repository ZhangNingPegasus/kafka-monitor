package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.vo.AdminVo;
import com.pegasus.kafka.service.dto.SysAdminService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * The controller for providing a home page.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
public class IndexController {

    private final SysAdminService sysAdminService;

    public IndexController(SysAdminService sysAdminService) {
        this.sysAdminService = sysAdminService;
    }

    @GetMapping(value = {"/","l"})
    public String toLogin() {
        return "login";
    }

    @GetMapping("index")
    public String toIndex(Model model,
                          AdminVo adminVo) {
        model.addAttribute("admin", adminVo);
        return "index";
    }


    @GetMapping("toinfo")
    public String toInfo(Model model,
                         AdminVo adminVo) {
        model.addAttribute("admin", sysAdminService.getBaseMapper().getById(adminVo.getId()));
        return "info";
    }

    @GetMapping("topassword")
    public String toPassword() {
        return "password";
    }

    @PostMapping("login")
    @ResponseBody
    public Result<?> login(@RequestParam(name = "username", required = true) String username,
                           @RequestParam(name = "password", required = true) String password,
                           @RequestParam(name = "remember", required = true, defaultValue = "false") Boolean remember) {
        try {
            Subject subject = SecurityUtils.getSubject();
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            usernamePasswordToken.setRememberMe(remember);
            subject.login(usernamePasswordToken);
            return Result.ok();
        } catch (Exception e) {
            return Result.error("账号或密码错误");
        }
    }

    @PostMapping("repwd")
    @ResponseBody
    public Result<?> repwd(AdminVo adminVo,
                           @RequestParam(name = "oldPassword", required = true) String oldPassword,
                           @RequestParam(name = "password", required = true) String password) {
        if (sysAdminService.changePassword(adminVo.getId(), oldPassword, password)) {
            return Result.ok();
        } else {
            return Result.error("密码修改失败");
        }
    }

    @PostMapping("reinfo")
    @ResponseBody
    public Result<?> reinfo(AdminVo adminVo,
                            @RequestParam(name = "name", required = true) String name,
                            @RequestParam(name = "gender", required = true) Boolean gender,
                            @RequestParam(name = "phoneNumber", required = true) String phoneNumber,
                            @RequestParam(name = "email", required = true) String email,
                            @RequestParam(name = "remark", required = true) String remark) {
        sysAdminService.updateInfo(adminVo.getId(), name, gender, phoneNumber, email, remark);
        return Result.ok();

    }

    @PostMapping("quit")
    @ResponseBody
    public Result<?> quit() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isAuthenticated()) {
            subject.logout(); //session会销毁, 在SessionListener监听session销毁，清理权限缓存
        }
        return Result.ok();
    }
}
