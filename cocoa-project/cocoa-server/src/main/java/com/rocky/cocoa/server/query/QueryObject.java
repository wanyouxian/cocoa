package com.rocky.cocoa.server.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryObject {
    private String sql;
    private int pageSize = DataCacheUtil.PageSize;
    private String currentUser;
    private EngineType engineType;
}
