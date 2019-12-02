package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysMailConfig;
import com.pegasus.kafka.service.alert.MailService;
import com.pegasus.kafka.service.dto.SysMailConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.pegasus.kafka.controller.MailConfigController.PREFIX;

/**
 * The controller for providing the UI used for setting the emails' configuration.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class MailConfigController {
    public static final String PREFIX = "mailconfig";
    private final SysMailConfigService sysMailConfigService;
    private final MailService mailService;

    public MailConfigController(SysMailConfigService sysMailConfigService, MailService mailService) {
        this.sysMailConfigService = sysMailConfigService;
        this.mailService = mailService;
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
        return String.format("%s/list", PREFIX);
    }

    @RequestMapping("totest")
    public String toTest() {
        return String.format("%s/test", PREFIX);
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

    @PostMapping("test")
    @ResponseBody
    public Result<?> test(@RequestParam(required = true, name = "to") String to,
                          @RequestParam(required = true, name = "subject") String subject,
                          @RequestParam(required = true, name = "html") String html) throws Exception {
        mailService.send(to, subject, html);
        return Result.success();
    }

}
