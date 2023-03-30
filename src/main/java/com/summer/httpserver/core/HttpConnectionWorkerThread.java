package com.summer.httpserver.core;

import com.summer.http.HttpStatusCode;
import com.summer.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * 执行具体处理的线程
 */
public class HttpConnectionWorkerThread extends Thread {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConnectionWorkerThread.class);
    private Socket socket;
    private String webroot;

    public HttpConnectionWorkerThread(Socket socket, String webroot) {
        this.socket = socket;
        this.webroot = webroot;
    }

    @Override
    public void run() {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            String targetFile = parseTargetFileFromRequest(inputStream);
            LOGGER.info("targetFile=" + targetFile);

            //读取目标文件内容
            String targetFileContent = readTargetFileContent(this.webroot, targetFile);

            //构造响应
            buildResponse(outputStream, targetFileContent);

            LOGGER.info("*** Connection Processing Finished.");
        } catch (IOException e) {
            LOGGER.error("请求处理发生异常", e);
        } finally {
            if (inputStream!= null) {
                try {
                    inputStream.close();
                } catch (IOException e) {}
            }
            if (outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {}
            }
            if (socket!= null) {
                try {
                    socket.close();
                } catch (IOException e) {}
            }
        }
    }

    /**
     * 响应构造
     *
     * @param outputStream
     * @param targetFileContent
     * @throws IOException
     */
    private void buildResponse(OutputStream outputStream, String targetFileContent) throws IOException {
        final String CRLF = "\r\n"; // 13, 10
        if (targetFileContent != null) {
            String response = HttpVersion.HTTP_1_1.LITERAL + " " + HttpStatusCode.OK_200.STATUS_CODE + " "
                    + HttpStatusCode.OK_200.MESSAGE + CRLF + // Status Line  :   HTTTP_VERSION RESPONSE_CODE RESPONSE_MESSAGE
                            "Content-Length: " + targetFileContent.getBytes().length + CRLF + // HEADER
                            CRLF +
                            targetFileContent +
                            CRLF + CRLF;
            outputStream.write(response.getBytes());
        } else {
            //文件不存在返回404
            String response = HttpVersion.HTTP_1_1.LITERAL + " " + HttpStatusCode.SERVER_ERROR_404_NOT_FOUND.STATUS_CODE
                    + " " + HttpStatusCode.SERVER_ERROR_404_NOT_FOUND.MESSAGE
                    + CRLF + // Status Line  :   HTTTP_VERSION RESPONSE_CODE RESPONSE_MESSAGE
                            "Content-Length: " + 0 + CRLF + // HEADER
                            CRLF +
                            "NOT FOUND" +
                            CRLF + CRLF;
            LOGGER.info(response);
            outputStream.write(response.getBytes());
        }
    }

    /**
     * 解析目标文件内容
     *
     * @param webroot
     * @param targetFile
     * @return
     */
    private String readTargetFileContent(String webroot, String targetFile) {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            StringBuilder resultSB = new StringBuilder();

            fileReader = new FileReader(webroot + targetFile);
            bufferedReader = new BufferedReader(fileReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                resultSB.append(line).append("\n");
            }

            return resultSB.toString();
        } catch (FileNotFoundException fileNotFoundException) {
            LOGGER.warn("文件不存在:" + webroot + targetFile);
        } catch (Exception e) {
            LOGGER.error("目标文件读取异常,", e);
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }

                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                LOGGER.error("文件流关闭失败:" + webroot + targetFile, e);
            }
        }

        return null;
    }

    /**
     * 从请求体中解析出目标文件名
     *
     * @param inputStream 输入流
     * @return
     */
    private String parseTargetFileFromRequest(InputStream inputStream) {
        //获取请求体
        String requestStr = "";
        try {
            byte[] buffer = new byte[2048];
            inputStream.read(buffer);
            requestStr = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("requestStr=" + requestStr);
        //解析出目标文件名
        int indexOfFirstSpace = requestStr.indexOf(' ');
        if (indexOfFirstSpace != -1) {
            int indexOfSecondSpace = requestStr.indexOf(' ', indexOfFirstSpace + 1);
            if (indexOfSecondSpace > indexOfFirstSpace) {
                return requestStr.substring(indexOfFirstSpace + 1, indexOfSecondSpace);
            }
        }

        return null;
    }
}
