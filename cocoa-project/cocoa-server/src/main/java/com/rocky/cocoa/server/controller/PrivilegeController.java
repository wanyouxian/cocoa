package com.rocky.cocoa.server.controller;

import com.rocky.cocoa.entity.privilege.ResourcePrivilege;
import com.rocky.cocoa.server.BaseController;
import com.rocky.cocoa.server.jwt.ContextUtil;
import com.rocky.cocoa.server.service.PrivilegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/cocoa/v1/ranger")
@CrossOrigin
public class PrivilegeController extends BaseController {

    @Autowired
    PrivilegeService privilegeService;

    @GetMapping(value = "privileges")
    public Object getRangerPrivileges(
            @RequestParam(name = "pageIndex", required = true, defaultValue = "1") int pageIndex,
            @RequestParam(name = "pageSize", required = true, defaultValue = "50") int pageSize) {

        Page<ResourcePrivilege> resourcePrivileges =
                privilegeService.listResourcePrivileges(ContextUtil.getCurrentUser().getTeam(), pageIndex-1, pageSize, null, null);
        Map<String,Object> pages = new HashMap<>();
        pages.put("pages",resourcePrivileges.getContent());
        pages.put("pageIndex",pageIndex);
        pages.put("pageSize",pageSize);
        pages.put("pageCount",resourcePrivileges.getTotalPages());
        return getResult(pages);
    }

    @PostMapping(value = "privilege")
    public Object addRangerPrivilege(@RequestBody ResourcePrivilege resourcePrivilege) {
        privilegeService.addPrivilege(resourcePrivilege);
        return getResult(true);
    }

    @PutMapping(value = "privilege")
    public Object updateRangerPrivilege(@RequestBody ResourcePrivilege resourcePrivilege) {
        privilegeService.updatePrivilege(resourcePrivilege);
        return getResult(true);
    }

    @DeleteMapping(value = "privilege")
    public Object deleteRangerPrivilege(@RequestParam long id) {
        privilegeService.delPrivilege(id);
        return getResult(true);
    }

}
