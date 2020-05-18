package com.rocky.cocoa.repository.cluster;

import com.rocky.cocoa.entity.cluster.HdfsSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface HdfsSummaryRepository extends JpaRepository<HdfsSummary, Long> {
    HdfsSummary findTop1ByIsTrashFalseAndCreateTimeLessThanEqualOrderByCreateTimeDesc(Date selectTime);

    List<HdfsSummary> findByIsTrashFalseAndCreateTimeBetweenOrderByCreateTimeAsc(Date startTime, Date endTime);

}
