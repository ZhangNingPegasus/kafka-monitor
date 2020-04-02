package com.pegasus.kafka.service.dto;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pegasus.kafka.common.annotation.TranRead;
import com.pegasus.kafka.common.utils.Common;
import com.pegasus.kafka.entity.dto.SysPage;
import com.pegasus.kafka.entity.vo.AdminVo;
import com.pegasus.kafka.entity.vo.PageVo;
import com.pegasus.kafka.mapper.SysPageMapper;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * The service for table 'sys_page'.
 * <p>
 * *****************************************************************
 * Name               Action            Time          Description  *
 * Ning.Zhang       Initialize         11/7/2019      Initialize   *
 * *****************************************************************
 */
@Service
public class SysPageService extends ServiceImpl<SysPageMapper, SysPage> {

    private final SysPermissionService sysPermissionService;

    public SysPageService(SysPermissionService sysPermissionService) {
        this.sysPermissionService = sysPermissionService;
    }

    @TranRead
    public IPage<PageVo> list(Integer pageNum,
                              Integer pageSize,
                              @Nullable String name) {
        IPage<PageVo> page = new Page<>(pageNum, pageSize);
        List<PageVo> list = this.baseMapper.list(page, name);
        page.setRecords(list);
        return page;
    }

    @TranRead
    public Long getMaxOrderNum(Long parentId) {
        return this.baseMapper.getMaxOrderNum(parentId);
    }


    @TranRead
    public void fillPages(AdminVo adminVo) {
        if (adminVo == null) {
            return;
        }
        adminVo.setPermissions(getPages(adminVo));
        adminVo.getPermissions().sort((o1, o2) -> (int) (o1.getOrderNum() - o2.getOrderNum()));
    }

    @TranRead
    List<PageVo> getPages(AdminVo adminVo) {
        List<SysPage> allPages = list(new QueryWrapper<SysPage>().lambda().eq(SysPage::getIsMenu, true).orderByAsc(SysPage::getParentId).orderByAsc(SysPage::getOrderNum));
        Map<Long, SysPage> allPageMap = toMap(allPages);

        // 非顶级集合
        List<PageVo> nonRootPageList = new ArrayList<>();
        // 顶级集合
        List<PageVo> rootPageList = new ArrayList<>();
        Map<Long, PageVo> permission = sysPermissionService.getPermissionPages(adminVo.getId());

        for (SysPage sysPage : allPages) {
            if (!adminVo.getSysRole().getSuperAdmin() && !permission.containsKey(sysPage.getId())) {
                continue;
            }
            if (sysPage.getParentId() == 0) {
                PageVo rootPageVo = Common.toVo(sysPage, PageVo.class);
                PageVo permissionPageVo = permission.get(rootPageVo.getId());
                if (permissionPageVo != null) {
                    rootPageVo.setCanInsert(permissionPageVo.getCanInsert());
                    rootPageVo.setCanDelete(permissionPageVo.getCanDelete());
                    rootPageVo.setCanUpdate(permissionPageVo.getCanUpdate());
                    rootPageVo.setCanSelect(permissionPageVo.getCanSelect());
                }
                rootPageList.add(rootPageVo);
            } else {
                nonRootPageList.add(Common.toVo(sysPage, PageVo.class));
            }

            if (sysPage.getIsDefault()) {
                adminVo.setDefaultPage(sysPage.getUrl());
            }
        }

        for (PageVo pageVo : nonRootPageList) {
            SysPage sysPageParent = allPageMap.get(pageVo.getParentId());
            if (sysPageParent != null) {
                Optional<PageVo> first = rootPageList.stream().filter(p -> p.getId().equals(sysPageParent.getId())).findFirst();
                if (!first.isPresent()) {
                    PageVo rootPageVo = Common.toVo(sysPageParent, PageVo.class);
                    PageVo permissionPageVo = permission.get(rootPageVo.getId());
                    if (permissionPageVo != null) {
                        rootPageVo.setCanInsert(permissionPageVo.getCanInsert());
                        rootPageVo.setCanDelete(permissionPageVo.getCanDelete());
                        rootPageVo.setCanUpdate(permissionPageVo.getCanUpdate());
                        rootPageVo.setCanSelect(permissionPageVo.getCanSelect());
                    }
                    rootPageList.add(rootPageVo);
                }
            }
        }


        if (ObjectUtils.isNotNull(rootPageList) || ObjectUtils.isNotNull(nonRootPageList)) {
            Set<Long> map = Sets.newHashSetWithExpectedSize(nonRootPageList.size());
            rootPageList.forEach(rootPage -> getChild(adminVo, permission, rootPage, nonRootPageList, map));
            filter(rootPageList);
            return rootPageList;
        }
        return null;
    }

    private Map<Long, SysPage> toMap(List<SysPage> sysPageList) {
        Map<Long, SysPage> result = new HashMap<>((int) (sysPageList.size() / 0.75));

        for (SysPage sysPage : sysPageList) {
            result.put(sysPage.getId(), sysPage);
        }
        return result;
    }

    private void filter(List<PageVo> pageVoList) {
        Iterator<PageVo> iterator = pageVoList.iterator();
        while (iterator.hasNext()) {
            PageVo page = iterator.next();
            if (StringUtils.isEmpty(page.getUrl()) && (page.getChildren() == null || page.getChildren().size() < 1)) {
                iterator.remove();
            } else {
                filter(page.getChildren());
            }
        }
    }

    private void getChild(AdminVo adminVo,
                          Map<Long, PageVo> permission,
                          PageVo parentPage,
                          List<PageVo> childrenPageList,
                          Set<Long> set) {
        List<PageVo> childList = Lists.newArrayList();
        childrenPageList.stream().//
                filter(p -> !set.contains(p.getId())). // 判断是否已循环过当前对象
                filter(p -> p.getParentId().equals(parentPage.getId())). // 判断是否父子关系
                filter(p -> set.size() <= childrenPageList.size()).// set集合大小不能超过childrenPageList的大小
                forEach(p -> {
            if (adminVo.getSysRole().getSuperAdmin() || StringUtils.isEmpty(p.getUrl()) || permission.containsKey(p.getId())) {
                // 放入set, 递归循环时可以跳过这个页面，提高循环效率
                set.add(p.getId());
                // 递归获取当前类目的子类目
                getChild(adminVo, permission, p, childrenPageList, set);

                if (permission.containsKey(p.getId())) {
                    PageVo page = permission.get(p.getId());
                    p.setCanInsert(page.getCanInsert());
                    p.setCanDelete(page.getCanDelete());
                    p.setCanUpdate(page.getCanUpdate());
                    p.setCanSelect(page.getCanSelect());
                }
                childList.add(p);
            }
        });
        parentPage.setChildren(childList);
    }
}