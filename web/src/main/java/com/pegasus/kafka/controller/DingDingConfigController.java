package com.pegasus.kafka.controller;

import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.entity.dto.SysDingDingConfig;
import com.pegasus.kafka.entity.po.DingDingMessage;
import com.pegasus.kafka.service.alert.DingDingService;
import com.pegasus.kafka.service.dto.SysDingDingConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.pegasus.kafka.controller.DingDingConfigController.PREFIX;

/**
 * The controller for providing a UI for setting the dingding's configuration.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class DingDingConfigController {
    public static final String PREFIX = "dingdingconfig";
    private final SysDingDingConfigService sysDingDingConfigService;
    private final DingDingService dingDingService;

    public DingDingConfigController(SysDingDingConfigService sysDingDingConfigService, DingDingService dingDingService) {
        this.sysDingDingConfigService = sysDingDingConfigService;
        this.dingDingService = dingDingService;
    }

    @GetMapping("tolist")
    public String toList(Model model) {
        List<SysDingDingConfig> sysDingDingConfigList = sysDingDingConfigService.list();
        if (sysDingDingConfigList != null && sysDingDingConfigList.size() > 0) {
            SysDingDingConfig sysDingDingConfig = sysDingDingConfigList.get(0);
            model.addAttribute("config", sysDingDingConfig);
        } else {
            model.addAttribute("config", new SysDingDingConfig());
        }
        return String.format("%s/list", PREFIX);
    }

    @GetMapping("totest")
    public String toTest() {
        return String.format("%s/test", PREFIX);
    }

    @PostMapping("save")
    @ResponseBody
    public Result<Integer> save(@RequestParam(required = true, name = "accesstoken") String accesstoken,
                                @RequestParam(required = true, name = "secret") String secret) {
        int result = sysDingDingConfigService.save(accesstoken, secret);
        return Result.ok(result);
    }

    @PostMapping("test")
    @ResponseBody
    public Result<?> test(@RequestParam(required = true, name = "content", defaultValue = "") String content,
                          @RequestParam(required = true, name = "atMobiles", defaultValue = "") String atMobiles,
                          @RequestParam(required = true, name = "isAtAll", defaultValue = "false") Boolean isAtAll) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DingDingMessage message = new DingDingMessage();
        message.setMsgtype("text");
        message.setText(new DingDingMessage.Text("告警主机：" + InetAddress.getLocalHost().getHostName() + "\n" +
                "主机地址：" + InetAddress.getLocalHost().getHostAddress() + "\n" +
                "告警等级：警告\n" +
                "当前状态：OK\n" +
                "问题详情：" + content + "\n" +
                "告警时间：" + sdf.format(new Date()) + "\n"));
        message.setAt(new DingDingMessage.At(Arrays.asList(atMobiles.split(",")), isAtAll));

        dingDingService.send(message);
        return Result.ok();
    }

}
