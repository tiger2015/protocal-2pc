package com.tiger.distributeprotocol.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 13:24
 * @Description:
 * @Version: 1.0
 **/
public class LogUtil {

    public static Logger getLogger(Class clazz) {
        return LoggerFactory.getLogger(clazz);
    }

}
