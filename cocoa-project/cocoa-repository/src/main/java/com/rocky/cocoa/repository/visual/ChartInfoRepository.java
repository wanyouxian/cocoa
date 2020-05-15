package com.rocky.cocoa.repository.visual;

import com.rocky.cocoa.entity.visual.ChartInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChartInfoRepository extends JpaRepository<ChartInfo, Long> {
  void deleteByIdIn(List<Long> ids);
  void deleteByDashboardId(long dashboardId);
}
