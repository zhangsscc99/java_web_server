package com.summer.httpserver;

import com.summer.httpserver.config.Configuration;
import com.summer.httpserver.config.ConfigurationManager;
import com.summer.httpserver.core.ServerListenerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * http server主类
 */
public class HttpServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    public static void main(String[] args) {
        LOGGER.info("Server starting...");

        ConfigurationManager.getInstance().loadConfigurationFile("C:\\Users\\zhang\\Desktop\\SummerHttpServer\\src\\main\\resources\\http.json");
        Configuration conf = ConfigurationManager.getInstance().getCurrentConfiguration();

        LOGGER.info("conf[port:" + conf.getPort() + ",webroot:" + conf.getWebroot() + "]");

        try {
            ServerListenerThread serverListenerThread = new ServerListenerThread(conf.getPort(), conf.getWebroot());
            serverListenerThread.start();
        } catch (IOException e) {
            LOGGER.error("系统异常,", e);
        }
    }
}
