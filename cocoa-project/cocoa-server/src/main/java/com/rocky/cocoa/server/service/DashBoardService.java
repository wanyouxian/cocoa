package com.rocky.cocoa.server.service;


import com.rocky.cocoa.entity.visual.Dashboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

public interface DashBoardService {

    Page<Dashboard> listDashboards(int page, int size, String sort, Sort.Direction direction);

    List<Map<String, Object>> listDashboardNames();

    public void createDashBoard(Dashboard dashBoard);

    public void deleteDashBoard(long dashBoardId);

    public void updateDashBoard(Dashboard dashBoard);

    public Object getDashBoard(long dashBoardId);
}
