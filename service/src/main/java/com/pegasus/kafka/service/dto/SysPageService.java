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
import com.pegasus.kafka.entity.vo.AdminInfo;
import com.pegasus.kafka.entity.vo.PageInfo;
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
    public IPage<PageInfo> list(Integer pageNum,
                                Integer pageSize,
                                @Nullable String name) {
        IPage<PageInfo> page = new Page<>(pageNum, pageSize);
        List<PageInfo> list = this.baseMapper.list(page, name);
        page.setRecords(list);
        return page;
    }

    @TranRead
    public Long getMaxOrderNum(Long parentId) {
        return this.baseMapper.getMaxOrderNum(parentId);
    }


    @TranRead
    public void fillPages(AdminInfo adminInfo) {
        if (adminInfo == null) {
            return;
        }
        adminInfo.setPermissions(getPages(adminInfo));
    }

    @TranRead
    List<PageInfo> getPages(AdminInfo adminInfo) {
        List<SysPage> allPages = list(new QueryWrapper<SysPage>().lambda().eq(SysPage::getIsMenu, true).orderByAsc(SysPage::getParentId).orderByAsc(SysPage::getOrderNum));
        Map<Long, SysPage> allPageMap = toMap(allPages);

        // 非顶级集合
        List<PageInfo> nonRootPageList = new ArrayList<>();
        // 顶级集合
        List<PageInfo> rootPageList = new ArrayList<>();
        Map<Long, PageInfo> permission = sysPermissionService.getPermissionPages(adminInfo.getId());

        for (SysPage sysPage : allPages) {
            if (!adminInfo.getSysRole().getSuperAdmin() && !permission.containsKey(sysPage.getId())) {
                continue;
            }
            if (sysPage.getParentId() == 0) {
                PageInfo rootPageInfo = Common.toVo(sysPage, PageInfo.class);
                PageInfo permissionPageInfo = permission.get(rootPageInfo.getId());
                if (permissionPageInfo != null) {
                    rootPageInfo.setCanInsert(permissionPageInfo.getCanInsert());
                    rootPageInfo.setCanDelete(permissionPageInfo.getCanDelete());
                    rootPageInfo.setCanUpdate(permissionPageInfo.getCanUpdate());
                    rootPageInfo.setCanSelect(permissionPageInfo.getCanSelect());
                }
                rootPageList.add(rootPageInfo);
            } else {
                nonRootPageList.add(Common.toVo(sysPage, PageInfo.class));
            }

            if (sysPage.getIsDefault()) {
                adminInfo.setDefaultPage(sysPage.getUrl());
            }
        }

        for (PageInfo pageInfo : nonRootPageList) {
            SysPage sysPageParent = allPageMap.get(pageInfo.getParentId());
            if (sysPageParent != null) {
                Optional<PageInfo> first = rootPageList.stream().filter(p -> p.getId().equals(sysPageParent.getId())).findFirst();
                if (!first.isPresent()) {
                    PageInfo rootPageInfo = Common.toVo(sysPageParent, PageInfo.class);
                    PageInfo permissionPageInfo = permission.get(rootPageInfo.getId());
                    if (permissionPageInfo != null) {
                        rootPageInfo.setCanInsert(permissionPageInfo.getCanInsert());
                        rootPageInfo.setCanDelete(permissionPageInfo.getCanDelete());
                        rootPageInfo.setCanUpdate(permissionPageInfo.getCanUpdate());
                        rootPageInfo.setCanSelect(permissionPageInfo.getCanSelect());
                    }
                    rootPageList.add(rootPageInfo);
                }
            }
        }


        if (ObjectUtils.isNotNull(rootPageList) || ObjectUtils.isNotNull(nonRootPageList)) {
            Set<Long> map = Sets.newHashSetWithExpectedSize(nonRootPageList.size());
            rootPageList.forEach(rootPage -> getChild(adminInfo, permission, rootPage, nonRootPageList, map));
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

    private void filter(List<PageInfo> pageInfoList) {
        Iterator<PageInfo> iterator = pageInfoList.iterator();
        while (iterator.hasNext()) {
            PageInfo page = iterator.next();
            if (StringUtils.isEmpty(page.getUrl()) && (page.getChildren() == null || page.getChildren().size() < 1)) {
                iterator.remove();
            } else {
                filter(page.getChildren());
            }
        }
    }

    private void getChild(AdminInfo adminInfo,
                          Map<Long, PageInfo> permission,
                          PageInfo parentPage,
                          List<PageInfo> childrenPageList,
                          Set<Long> set) {
        List<PageInfo> childList = Lists.newArrayList();
        childrenPageList.stream().//
                filter(p -> !set.contains(p.getId())). // 判断是否已循环过当前对象
                filter(p -> p.getParentId().equals(parentPage.getId())). // 判断是否父子关系
                filter(p -> set.size() <= childrenPageList.size()).// set集合大小不能超过childrenPageList的大小
                forEach(p -> {
            if (adminInfo.getSysRole().getSuperAdmin() || StringUtils.isEmpty(p.getUrl()) || permission.containsKey(p.getId())) {
                // 放入set, 递归循环时可以跳过这个页面，提高循环效率
                set.add(p.getId());
                // 递归获取当前类目的子类目
                getChild(adminInfo, permission, p, childrenPageList, set);

                if (permission.containsKey(p.getId())) {
                    PageInfo page = permission.get(p.getId());
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
