package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysMailConfig;
import com.pegasus.kafka.service.dto.SysMailConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("mailconfig")
public class MailConfigController {
    private final SysMailConfigService sysMailConfigService;

    public MailConfigController(SysMailConfigService sysMailConfigService) {
        this.sysMailConfigService = sysMailConfigService;
    }

    @RequestMapping("tolist")
    public String toList(Model model) {
        List<SysMailConfig> sysMailConfigList = sysMailConfigService.list();
        if (sysMailConfigList != null && sysMailConfigList.size() > 0) {
            SysMailConfig sysMailConfig = sysMailConfigList.get(0);
            model.addAttribute("config", sysMailConfig);
        } else {
            model.addAttribute("config", new SysMailConfig());
        }
        return "mailconfig/list";
    }

    @PostMapping("save")
    @ResponseBody
    public Result<Integer> save(@RequestParam(required = true, name = "host") String host,
                                @RequestParam(required = true, name = "port") String port,
                                @RequestParam(required = true, name = "username") String username,
                                @RequestParam(required = true, name = "password") String password) {
        int result = sysMailConfigService.save(host, port, username, password);
        return Result.success(result);
    }

}
