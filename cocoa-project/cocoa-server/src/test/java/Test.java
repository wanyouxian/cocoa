import cn.hutool.core.date.DateUtil;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Rocky
 * Date: 2020-05-14
 * Time: 14:55
 * Description:
 */
public class Test {
    public static void main(String[] args) {
        Date now = new Date();
        System.out.println(DateUtil.date());
        System.out.println(DateUtil.toIntSecond(new Date()));
        System.out.println(now.getTime());
    }
}
