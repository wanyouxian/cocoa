package com.rocky.cocoa.server.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.rocky.cocoa.entity.system.PrivilegeType;
import com.rocky.cocoa.entity.system.SystemPrivilege;
import com.rocky.cocoa.entity.system.Team;
import com.rocky.cocoa.entity.system.User;
import com.rocky.cocoa.repository.system.SystemPrivilegeRepository;
import com.rocky.cocoa.repository.system.TeamRepository;
import com.rocky.cocoa.repository.system.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SystemServiceImpl implements SystemService {

    @Resource
    UserRepository userRepository;
    @Resource
    TeamRepository teamRepository;
    @Resource
    SystemPrivilegeRepository systemPrivilegeRepository;
    @Autowired
    RangerService rangerService;

    @PostConstruct
    public void init() {
        //创建超级用户和超级组
        Team admin = teamRepository.findOneByName("Admin");
        if (admin == null) {
            Team team = new Team();
            team.setName("Admin");
            team.setCreateTime(new Date());
            team.setIsTrash(false);
            team.setAdmin("cocoa");
            teamRepository.save(team);
        }
        User cocoa = userRepository.findOneByName("cocoa");
        if (cocoa == null) {
            User user = new User();
            user.setIsTrash(false);
            user.setMail("cocoa@cocoa.com");
            user.setName("cocoa");
            user.setPhone("13888888888");
            user.setPwd(SecureUtil.md5("cocoa@123"));
            user.setTeam("Admin");
            user.setCreateTime(new Date());
            userRepository.save(user);
        }
        //为超级用户添加权限
        SystemPrivilege privilege = systemPrivilegeRepository.findOneByTeamAndPrivilegeType("Admin", PrivilegeType.SYSTEM);
        if (privilege == null) {
            SystemPrivilege systemPrivilege = new SystemPrivilege();
            systemPrivilege.setIsTrash(false);
            systemPrivilege.setTeam("Admin");
            systemPrivilege.setPrivilegeType(PrivilegeType.SYSTEM);
            systemPrivilege.setCreateTime(new Date());
            systemPrivilegeRepository.save(systemPrivilege);
        }

    }

    @Override
    public Page<User> listUsers(int page, int size, String sort, Direction direction) {
        User user = new User();
        user.setIsTrash(false);

        return userRepository.findAll(Example.of(user), PageRequest.of(page, size,
                Sort.by(direction == null ? Direction.DESC : direction, ObjectUtil.isNull(sort) ? "id" : sort)));

    }

    @Override
    public void addUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void deleteUser(long id) {
        User user = userRepository.getOne(id);
        user.setIsTrash(true);
        userRepository.save(user);
    }

    @Override
    public User findUserById(long id) {
        return userRepository.getOne(id);
    }

    @Override
    public User findUserByName(String name) {
        return userRepository.findOneByName(name);
    }

    @Override
    public Page<Team> listTeams(int page, int size, String sort, Direction direction) {
        Team team = new Team();
        team.setIsTrash(false);

        return teamRepository.findAll(Example.of(team), PageRequest.of(page, size,
                Sort.by(direction == null ? Direction.DESC : direction, ObjectUtil.isNull(sort) ? "id" : sort)));

    }

    @Override
    public List<String> listTeamNames() {
        Team team = new Team();
        team.setIsTrash(false);
        return teamRepository.findAll(Example.of(team)).stream().map(Team::getName).collect(Collectors.toList());
    }

    @Override
    public void addTeam(Team team) {
        //todo add user to ranger and os
//        if (!team.getName().equals("hadoop")) {
//            rangerService.addRangerUser(team.getName(), team.getName(), team.getName(), SecureUtil.md5(team.getName()),
//                    Arrays.asList("ROLE_USER"));
//        }
        teamRepository.save(team);
    }

    @Override
    public void updateTeam(Team team) {
        teamRepository.save(team);
    }

    @Override
    public void deleteTeam(long id) {
        Team one = teamRepository.getOne(id);
        //todo
        rangerService.removeRangerUser(one.getName());
        teamRepository.delete(one);
    }

    @Override
    public Team findTeamById(long id) {
        return teamRepository.getOne(id);
    }

    @Override
    public Team findTeamByName(String name) {
        return teamRepository.findOneByName(name);
    }

    @Override
    public SystemPrivilege findSystemPrivilege(String team, PrivilegeType privilegeType) {
        return systemPrivilegeRepository.findOneByTeamAndPrivilegeType(team, privilegeType);
    }

    @Override
    public void addSystemPrivilege(SystemPrivilege systemPrivilege) {
        systemPrivilegeRepository.save(systemPrivilege);
    }

    @Override
    public void delSystemPrivilege(long id) {
        systemPrivilegeRepository.deleteById(id);
    }

    @Override
    public Page<SystemPrivilege> listSystemPrivileges(int page, int size, String sort,
                                                      Direction direction) {
        SystemPrivilege systemPrivilege = new SystemPrivilege();
        systemPrivilege.setIsTrash(false);

        return systemPrivilegeRepository.findAll(Example.of(systemPrivilege), PageRequest.of(page, size,
                Sort.by(direction == null ? Direction.DESC : direction, ObjectUtil.isNull(sort) ? "id" : sort)));
    }

    @Override
    public void updateSystemPrivilege(SystemPrivilege systemPrivilege) {
        System.err.println(systemPrivilege);
        Optional<SystemPrivilege> oldOptional = systemPrivilegeRepository.findById(systemPrivilege.getId());
        SystemPrivilege oldSystemPrivilege = oldOptional.get();
        BeanUtil.copyProperties(systemPrivilege, oldSystemPrivilege);
        systemPrivilegeRepository.save(oldSystemPrivilege);
    }
}
