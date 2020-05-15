package com.rocky.cocoa.server.service;

import cn.hutool.core.util.ObjectUtil;
import com.rocky.cocoa.entity.visual.ChartInfo;
import com.rocky.cocoa.entity.visual.Dashboard;
import com.rocky.cocoa.repository.visual.ChartInfoRepository;
import com.rocky.cocoa.repository.visual.DashboardRepository;
import com.rocky.cocoa.server.visual.ChartData;
import com.rocky.cocoa.server.visual.ChartDataLoader;
import com.rocky.cocoa.server.visual.ChartSpecific;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashBoardServiceImpl implements DashBoardService {

    @Autowired
    DashboardRepository dashBoardRepository;
    @Autowired
    ChartInfoRepository chartInfoRepository;

    @Autowired
    ChartService chartService;

    @Autowired
    ChartDataLoader chartDataLoader;

    @Override
    public Page<Dashboard> listDashboards(int page, int size, String sort, Sort.Direction direction) {
        Dashboard dashboard = new Dashboard();
        dashboard.setIsTrash(false);
        return dashBoardRepository.findAll(Example.of(dashboard),
                PageRequest.of(page, size,
                        Sort.by(direction == null ? Sort.Direction.DESC : direction, ObjectUtil.isNull(sort) ? "id" : sort)));
    }

    @Override
    public List<Map<String, Object>> listDashboardNames() {
        Dashboard dashboard = new Dashboard();
        dashboard.setIsTrash(false);
        return dashBoardRepository.findAll(Example.of(dashboard)).stream().map(dashboard1 -> {
            Map<String,Object> dash = new HashMap<>();
            dash.put("id",dashboard1.getId());
            dash.put("name",dashboard1.getName());
            return dash;
        }).collect(Collectors.toList());

    }

    @Override
    public void createDashBoard(Dashboard dashBoard) {
        dashBoardRepository.save(dashBoard);
    }

    @Override
    public void deleteDashBoard(long dashBoardId) {
        dashBoardRepository.deleteById(dashBoardId);
        chartInfoRepository.deleteByDashboardId(dashBoardId);
    }

    @Override
    public void updateDashBoard(Dashboard dashBoard) {
        dashBoardRepository.save(dashBoard);
    }

    @Override
    public Object getDashBoard(long dashBoardId) {
        ChartInfo chartInfo = new ChartInfo();
        chartInfo.setDashboardId(dashBoardId);
        chartInfo.setIsTrash(false);
        List<ChartInfo> all = chartInfoRepository.findAll(Example.of(chartInfo));
        List<Map<String,Object>> result = new ArrayList<>();
        all.forEach(chartInfo1 -> {
            ChartSpecific chartSpecific = null;
            try {
                chartSpecific = chartService.convertChartSpecific(chartInfo1.getChartSpecific(), chartInfo1.getChartType());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ChartData chartData = null;
            try {
                chartData = chartDataLoader.load(chartSpecific);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("chartData", chartData);
            assert chartSpecific != null;
            data.put("chartSettings", chartSpecific.getChartSetting());
            data.put("x", chartInfo1.getLocation().getWidgetX());
            data.put("y", chartInfo1.getLocation().getWidgetY());
            data.put("w", chartInfo1.getLocation().getWidgetW());
            data.put("h", chartInfo1.getLocation().getWidgetH());
            data.put("name",chartInfo1.getName());
            data.put("i",chartInfo1.getId());
            result.add(data);
        });

        return result;
    }

}
