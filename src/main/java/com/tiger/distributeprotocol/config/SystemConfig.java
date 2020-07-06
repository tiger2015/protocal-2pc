package com.tiger.distributeprotocol.config;

import com.tiger.distributeprotocol.common.LogUtil;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Auther: Zeng Hu
 * @Date: 2020/7/5 22:46
 * @Description:
 * @Version: 1.0
 **/
public class SystemConfig {
    private static final Logger LOG = LogUtil.getLogger(SystemConfig.class);
    private static final String CONFIG_FILE = "server.properties";
    private static final String FILE_PATH = "./config/" + CONFIG_FILE;
    public static final Map<Long, NodeAddress> nodes = new HashMap<>();
    public static long sesstionTimeout = 3000;
    public static long id = 1;

    public static void loadConfig() {
        Properties properties = new Properties();
        InputStreamReader reader = null;
        String userDir = System.getProperty("user.dir");
        File file = new File(userDir, FILE_PATH);
        if (file.exists()) {
            try {
                reader = new InputStreamReader(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                LOG.error("config file {} don't exits", file.toString());
            }
        } else {
            InputStream stream = SystemConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (Objects.nonNull(stream))
                reader = new InputStreamReader(stream);
        }
        if (Objects.isNull(reader)) {
            LOG.error("config file don't exits");
            System.exit(0);
        }
        try {
            String pattern = "server.\\d+=*";
            Pattern compile = Pattern.compile(pattern);
            properties.load(reader);
            Enumeration<?> enumeration = properties.propertyNames();
            while (enumeration.hasMoreElements()) {
                String element = enumeration.nextElement().toString();
                if(element.equals("server.id")){
                    id = Long.parseLong(properties.getProperty(element));
                }
                if (element.equals("session.timeout")) {
                    sesstionTimeout = Long.parseLong(properties.getProperty(element));
                }
                if (compile.matcher(element).find()){
                   // LOG.info("{}", properties.getProperty(element));
                    NodeAddress nodeAddress = parseNodeAddress(element, properties.getProperty(element));
                    nodes.put(nodeAddress.id, nodeAddress);
                }
            }
        } catch (IOException e) {
            LOG.error("load config fail", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static NodeAddress parseNodeAddress(String key, String value) {
        long id = Long.parseLong(key.substring(key.lastIndexOf('.') + 1));
        String ip = value.substring(0, value.indexOf(':'));
        int port = Integer.parseInt(value.substring(value.indexOf(":") + 1));
        return new NodeAddress(id, ip, port);
    }

    public static class NodeAddress {
        public final long id;
        public final String ip;
        public final int port;

        NodeAddress(long id, String ip, int port) {
            this.id = id;
            this.ip = ip;
            this.port = port;
        }
    }

}
