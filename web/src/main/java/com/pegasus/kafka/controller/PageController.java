package com.pegasus.kafka.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pegasus.kafka.common.response.Result;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysPage;
import com.pegasus.kafka.entity.vo.PageInfo;
import com.pegasus.kafka.service.dto.SysPageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.pegasus.kafka.controller.PageController.PREFIX;


/**
 * The controller for monitor's pages.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Controller
@RequestMapping(PREFIX)
public class PageController {
    public static final String PREFIX = "page";

    private final SysPageService sysPageService;

    public PageController(SysPageService sysPageService) {
        this.sysPageService = sysPageService;
    }

    @GetMapping("tolist")
    public String toList() {
        return String.format("%s/%s", PREFIX, "list");
    }

    @RequestMapping("toadd")
    public String toAdd(Model model) {
        model.addAttribute("pages", sysPageService.list(new QueryWrapper<SysPage>().lambda().eq(SysPage::getUrl, "").orderByAsc(SysPage::getName)));
        return "page/add";
    }

    @RequestMapping("toedit")
    public String toEdit(Model model,
                         @RequestParam(value = "id", required = true) Long id) {
        model.addAttribute("page", sysPageService.getById(id));
        model.addAttribute("pages", sysPageService.list(new QueryWrapper<SysPage>().lambda().eq(SysPage::getUrl, "").orderByAsc(SysPage::getName)));
        return "page/edit";
    }

    @PostMapping("list")
    @ResponseBody
    public Result<List<PageInfo>> list(@RequestParam(value = "page", required = true) Integer pageNum,
                                       @RequestParam(value = "limit", required = true) Integer pageSize,
                                       @RequestParam(value = "name", required = false) String name) {
        return Result.ok(sysPageService.list(pageNum, pageSize, name));
    }

    @PostMapping("add")
    @ResponseBody
    public Result<?> add(SysPage sysPage) {
        if (sysPage.getIsDefault() == null) {
            sysPage.setIsDefault(false);
        }
        if (sysPage.getIsMenu() == null) {
            sysPage.setIsMenu(false);
        }
        Long orderNum = sysPageService.getMaxOrderNum(sysPage.getParentId());
        sysPage.setOrderNum(orderNum + 1);
        sysPageService.save(sysPage);
        return Result.ok();
    }

    @PostMapping("edit")
    @ResponseBody
    public Result<?> edit(SysPage sysPage) {
        SysPage dbSysPage = sysPageService.getById(sysPage.getId());
        if (dbSysPage != null) {
            if (sysPage.getIsDefault() == null) {
                sysPage.setIsDefault(false);
            }
            if (sysPage.getIsMenu() == null) {
                sysPage.setIsMenu(false);
            }
            sysPageService.updateById(sysPage);
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
        sysPageService.removeByIds(idsList);
        return Result.ok();
    }

}
