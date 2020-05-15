package com.rocky.cocoa.server.plugin;

import com.rocky.cocoa.entity.plugin.PackageOutParam;
import com.rocky.cocoa.entity.plugin.PackageParam;
import lombok.Data;

import java.util.List;

@Data
public class PackageMetaInfo {
    private String name;
    private String version;
    private String jobType;
    private String language;
    private List<PackageParam> pkgParams;
    private List<PackageOutParam> outParams;
}
