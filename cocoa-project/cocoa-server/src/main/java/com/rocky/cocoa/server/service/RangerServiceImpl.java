package com.rocky.cocoa.server.service;

import com.rocky.cocoa.core.client.ranger.RangerClient;
import com.rocky.cocoa.core.client.ranger.config.RangerAuthConfig;
import com.rocky.cocoa.core.client.ranger.config.RangerClientConfig;
import com.rocky.cocoa.core.client.ranger.model.Policy;
import com.rocky.cocoa.core.client.ranger.model.PolicyItem;
import com.rocky.cocoa.core.client.ranger.model.PolicyItemAccess;
import com.rocky.cocoa.core.client.ranger.model.PolicyResource;
import com.rocky.cocoa.entity.privilege.HdfsResource;
import com.rocky.cocoa.entity.privilege.HiveResource;
import com.rocky.cocoa.entity.privilege.ResourcePrivilege;
import com.rocky.cocoa.entity.privilege.ResourceType;

import com.rocky.cocoa.core.client.ranger.model.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Service public class RangerServiceImpl implements RangerService {

    @Value("${custom.ranger.url}") private String rangerUrl;
    @Value("${custom.ranger.user}") private String rangerUser;
    @Value("${custom.ranger.pwd}") private String rangerPwd;
    @Value("${custom.ranger.service.hdfs}") private String rangerHdfs;
    @Value("${custom.ranger.service.hive}") private String rangerHive;
    @Getter private RangerClient rangerClient;

    @PostConstruct public void init() throws IOException {

        RangerClientConfig rangerClientConfig =
                RangerClientConfig.builder().connectTimeoutMillis(1000).readTimeoutMillis(1000)
                        .authConfig(RangerAuthConfig.builder().username(rangerUser).password(rangerPwd).build())
                        .logLevel(feign.Logger.Level.BASIC)
                        .url(rangerUrl).build();

        rangerClient = new RangerClient(rangerClientConfig);
    }

    @Override public void addRangerUser(String name, String firstName, String lastName, String password,
                                        List<String> roles) {
        User user =
                User.builder().name(name).firstName(firstName).lastName(lastName).password(password).isVisible(1).status(1)
                        .userSource(0).userRoleList(roles).build();
        rangerClient.getUsers().createUser(user);
    }

    @Override public void removeRangerUser(String name) {
        User user = rangerClient.getUsers().getUserByName(name);
        if (user != null) {
            rangerClient.getUsers().deleteUser(user.getId(), true);
        }
    }

    @Override public void addPolicy(ResourcePrivilege resourcePrivilege) {
        Map<String, PolicyResource> policyResourceMap = new HashMap<>();
        List<PolicyItemAccess> policyItemAccessList = new ArrayList<>();
        Set<String> users = new HashSet<>();
        users.add(resourcePrivilege.getTeam());
        Policy policy = null;
        if (resourcePrivilege.getResourceType().equals(ResourceType.HDFS)) {
            HdfsResource hdfsResource = (HdfsResource) resourcePrivilege.getResource();
            PolicyResource policyResource =
                    PolicyResource.builder().values(Arrays.asList(hdfsResource.getPath())).isRecursive(hdfsResource.getRecursive()).build();
            policyResourceMap.put("path", policyResource);
            hdfsResource.getPolicys().forEach(type -> {
                policyItemAccessList.add(PolicyItemAccess.builder().type(type).isAllowed(true).build());
            });
            PolicyItem policyItem =
                    PolicyItem.builder().delegateAdmin(true).users(users).accesses(policyItemAccessList).build();
            policy = Policy.builder().service(rangerHdfs).name(resourcePrivilege.getName()).isEnabled(true).policyType(0)
                    .resources(policyResourceMap).policyItems(Arrays.asList(policyItem)).build();

        } else if (resourcePrivilege.getResourceType().equals(ResourceType.HIVE)) {
            HiveResource hiveResource = (HiveResource) resourcePrivilege.getResource();
            PolicyResource dbResource =
                    PolicyResource.builder().values(Arrays.asList(hiveResource.getDatabase())).build();
            policyResourceMap.put("database", dbResource);
            PolicyResource tableResource =
                    PolicyResource.builder().values(Arrays.asList(hiveResource.getTable())).build();
            policyResourceMap.put("table", tableResource);
            PolicyResource columnResource =
                    PolicyResource.builder().values(Arrays.asList(hiveResource.getColumns())).build();
            policyResourceMap.put("column", columnResource);
            hiveResource.getPolicys().forEach(type -> {
                policyItemAccessList.add(PolicyItemAccess.builder().type(type).isAllowed(true).build());
            });
            PolicyItem policyItem =
                    PolicyItem.builder().delegateAdmin(true).users(users).accesses(policyItemAccessList).build();
            policy = Policy.builder().service(rangerHive).name(resourcePrivilege.getName()).isEnabled(true).policyType(0)
                    .resources(policyResourceMap).policyItems(Arrays.asList(policyItem)).build();
        } else {
            return;
        }


        rangerClient.getPolicies().createPolicy(policy);

    }

    @Override public void removePolicy(String name) {
        Policy policy = rangerClient.getPolicies().getPolicyByName(rangerHdfs, name);
        if (policy != null) {
            rangerClient.getPolicies().deletePolicy(policy.getId());
        }
    }
}
