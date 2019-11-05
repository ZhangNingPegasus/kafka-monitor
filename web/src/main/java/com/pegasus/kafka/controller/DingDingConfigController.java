package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysDingDingConfig;
import com.pegasus.kafka.service.dto.SysDingDingConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("dingdingconfig")
public class DingDingConfigController {
    private final SysDingDingConfigService sysDingDingConfigService;

    public DingDingConfigController(SysDingDingConfigService sysDingDingConfigService) {
        this.sysDingDingConfigService = sysDingDingConfigService;
    }

    @RequestMapping("tolist")
    public String toList(Model model) {
        List<SysDingDingConfig> sysDingDingConfigList = sysDingDingConfigService.list();
        if (sysDingDingConfigList != null && sysDingDingConfigList.size() > 0) {
            SysDingDingConfig sysDingDingConfig = sysDingDingConfigList.get(0);
            model.addAttribute("config", sysDingDingConfig);
        } else {
            model.addAttribute("config", new SysDingDingConfig());
        }
        return "dingdingconfig/list";
    }

    @PostMapping("save")
    @ResponseBody
    public Result<Integer> save(@RequestParam(required = true, name = "accesstoken") String accesstoken,
                                @RequestParam(required = true, name = "secret") String secret) {
        int result = sysDingDingConfigService.save(accesstoken, secret);
        return Result.success(result);
    }

}
