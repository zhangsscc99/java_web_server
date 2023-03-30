package com.summer.httpserver.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 监听请求的线程
 */
public class ServerListenerThread extends Thread {

    private final static Logger LOGGER = LoggerFactory.getLogger(ServerListenerThread.class);

    /**
     * port
     */
    private int port;

    /**
     * web root
     */
    private String webroot;

    /**
     * server socket
     */
    private ServerSocket serverSocket;

    public ServerListenerThread(int port, String webroot) throws IOException {
        this.port = port;
        this.webroot = webroot;
        this.serverSocket = new ServerSocket(this.port);
    }

    @Override
    public void run() {
        try {
            //循环，一直监听请求
            while ( serverSocket.isBound() && !serverSocket.isClosed()) {
                //监听请求
                Socket socket = serverSocket.accept();

                LOGGER.info("***Connection accepted: " + socket.getInetAddress());

                //开启新的线程执行具体的处理
                HttpConnectionWorkerThread workerThread = new HttpConnectionWorkerThread(socket, webroot);
                workerThread.start();
            }
        } catch (IOException e) {
            LOGGER.error("Problem with setting socket", e);
        } finally {
            if (serverSocket!=null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    LOGGER.error("serverSocket close exception,", e);
                }
            }
        }
    }
}
