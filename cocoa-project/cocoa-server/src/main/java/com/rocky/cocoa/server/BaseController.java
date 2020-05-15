package com.rocky.cocoa.server;

import com.rocky.cocoa.core.exception.ErrorCodes;
import com.rocky.cocoa.core.exception.CocoaException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class BaseController {
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler
    @ResponseBody
    public Map<String, Object> exceptionHandle(Exception ex, HttpServletResponse response) {
        //检测exception是否为cocoaexception
        ex.printStackTrace();
        if (CocoaException.class.isAssignableFrom(ex.getClass())) {
            CocoaException ng = (CocoaException) ex;
            //返回
            return getResultMap(ng.getErrorCode(), ng.getErrorMessage(), null);
        } else {
            //返回
            return getError(ErrorCodes.SYSTEM_EXCEPTION, ex.getMessage());
        }
    }


    protected Map<String, Object> getResult(Object o) {
        return getResultMap(null, o, null);
    }

    protected Map<String, Object> getPagedResult(List dataList,
                                                 int pageIndex,
                                                 int offset,
                                                 int count,
                                                 boolean hasMore) {
        Map<String, Object> map = this.getResult(dataList);
        map.put("pageIndex", pageIndex);
        map.put("start", offset);
        map.put("stop", offset + count);
        map.put("hasMore", hasMore);
        return map;

    }


    protected Map<String, Object> getError(int errCode, String errMsg) {
        return getResultMap(errCode, errMsg, null);
    }

    protected Map<String, Object> getResultMap(Integer code, Object data, Map<String, Object> extraMap) {
        String currentTime = sdf.format(new Date());
        HashMap<String, Object> result = new HashMap<>();
        result.put("currentTime", currentTime);
        if (code == null || code.equals(ErrorCodes.SYSTEM_SUCCESS)) {
            result.put("code", ErrorCodes.SYSTEM_SUCCESS);
            result.put("data", data);
        } else {
            result.put("code", code);
            result.put("msg", data);
        }

        if (extraMap != null && !extraMap.isEmpty()) {
            result.putAll(extraMap);
        }
        return result;
    }

}
