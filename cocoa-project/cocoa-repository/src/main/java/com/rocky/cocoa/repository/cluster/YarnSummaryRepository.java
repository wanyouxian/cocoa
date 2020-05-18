package com.rocky.cocoa.repository.cluster;

import com.rocky.cocoa.entity.cluster.YarnSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface YarnSummaryRepository extends JpaRepository<YarnSummary, Long> {
    YarnSummary findTop1ByIsTrashFalseAndCreateTimeLessThanEqualOrderByCreateTimeDesc(Date selectTime);

    List<YarnSummary> findByIsTrashFalseAndCreateTimeBetweenOrderByCreateTimeAsc(Date startTime, Date endTime);


}
