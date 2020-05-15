package com.rocky.cocoa.server.var;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ParamReference {
    //${JOB.jobname.IN.paramname}
    //${JOB.jobname.OUT.paramname}
    //${PRO.projectname.VAR.varname}

    private static final String SPLIT_PATTERN =
            "\\$\\{JOB\\.|\\.OUT\\.|\\.IN\\.|\\$\\{PRO\\.|\\.VAR\\.";
    public static String PARAM_OUTPUT_REF_PATTERN = "^\\$\\{JOB..+.OUT..+\\}$";
    public static String PARAM_INPUT_REF_PATTERN = "^\\$\\{JOB..+.IN..+\\}$";
    public static String PARAM_PRO_REF_PATTERN = "^\\$\\{PRO..+.VAR..+\\}$";
    private String projectName;
    private String jobName;
    private String varName;
    private boolean input;
    private boolean output;
    private boolean byProject;

    public static ParamReference parseFrom(String str) {
        //判断str符合那种正则

        //根据我们定义的规范 获取相关信息

        if (str.matches(PARAM_OUTPUT_REF_PATTERN)) {
            String substring = str.substring(0, str.length() - 1);
            String[] split = substring.split(SPLIT_PATTERN, -1);
            ParamReference reference = new ParamReference();
            reference.setOutput(true);
            reference.setJobName(split[1]);
            reference.setVarName(split[2]);
            return reference;
        } else if (str.matches(PARAM_INPUT_REF_PATTERN)) {
            String substring = str.substring(0, str.length() - 1);
            String[] split = substring.split(SPLIT_PATTERN, -1);
            ParamReference reference = new ParamReference();
            reference.setInput(true);
            reference.setJobName(split[1]);
            reference.setVarName(split[2]);
            return reference;
        } else if (str.matches(PARAM_PRO_REF_PATTERN)) {
            String substring = str.substring(0, str.length() - 1);
            String[] split = substring.split(SPLIT_PATTERN, -1);
            ParamReference reference = new ParamReference();
            reference.setByProject(true);
            reference.setProjectName(split[1]);
            reference.setVarName(split[2]);
            return reference;
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(ParamReference.parseFrom("${PRO.project1.VAR.var1}"));
        System.out.println(ParamReference.parseFrom("${JOB.job1.IN.input1}"));
        System.out.println(ParamReference.parseFrom("${JOB.job2.OUT.output1}"));
    }

}
