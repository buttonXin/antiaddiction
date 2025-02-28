package com.xreal.evapro.toolsapp.local_server;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class FileServer extends NanoHTTPD {
    private final boolean isOnlyDownload;
    private Context context;

    public FileServer(int port, Context context, boolean isOnlyDownload) {
        super(port);
        this.context = context;
        this.isOnlyDownload = isOnlyDownload;
    }

    public FileServer(int port, Context context) {
        this(port, context, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        File rootDirectory;

        // 获取文件目录

        rootDirectory = Environment.getExternalStorageDirectory(); // Android 10 或更低版本

        final File downloadFile = new File(rootDirectory, "Download");
        File targetFile;
        if (isOnlyDownload) {
            targetFile = new File(downloadFile, uri);
        } else {
            targetFile = new File(rootDirectory, uri);
        }


        if (!targetFile.exists()) {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "404 Not Found");
        }

        if (targetFile.isDirectory()) {
            // 如果是目录，返回文件列表
            StringBuilder response = new StringBuilder("<html><body>");
            response.append("<h1>File Browser</h1>");
            response.append("<ul>");
            for (File file : targetFile.listFiles()) {
                String fileName = file.getName();
                String link = uri.endsWith("/") ? uri + fileName : uri + "/" + fileName;
                response.append("<li><a href=\"").append(link).append("\">").append(fileName).append("</a></li>");
            }
            response.append("</ul>");
            response.append("</body></html>");
            return newFixedLengthResponse(Response.Status.OK, "text/html", response.toString());
        } else {
            // 如果是文件，返回文件内容
            try {
                return newChunkedResponse(Response.Status.OK, "application/octet-stream", new FileInputStream(targetFile));
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "500 Internal Server Error");
            }
        }
    }
}

