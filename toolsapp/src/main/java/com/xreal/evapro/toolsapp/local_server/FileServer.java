package com.xreal.evapro.toolsapp.local_server;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class FileServer extends NanoHTTPD {
    private final boolean isOnlyDownload;
    private Context context;

    // 访问密码的 SHA-256 哈希;为空表示未设置密码,下载无需密码
    private final String passwordHash;
    // 由密码哈希派生的会话令牌;密码设置后,浏览器验证通过便凭此 cookie 免重复输入
    private final String authToken;

    private static final String AUTH_COOKIE = "fs_auth";
    private static final String AUTH_TOKEN_SALT = "::local_server_session::";

    public FileServer(int port, Context context, boolean isOnlyDownload, String passwordHash) {
        super(port);
        this.context = context;
        this.isOnlyDownload = isOnlyDownload;
        this.passwordHash = passwordHash;
        this.authToken = (passwordHash == null || passwordHash.isEmpty())
                ? null
                : sha256Hex(passwordHash + AUTH_TOKEN_SALT);
    }

    public FileServer(int port, Context context, boolean isOnlyDownload) {
        this(port, context, isOnlyDownload, null);
    }

    public FileServer(int port, Context context) {
        this(port, context, false, null);
    }

    // 页面公共头部：字体放大，方便其他设备上的用户阅读
    private static final String PAGE_HEAD =
            "<head><meta charset=\"UTF-8\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<style>" +
                    "html{-webkit-text-size-adjust:100%;text-size-adjust:100%;}" +
                    "body{font-size:22px;font-family:-apple-system,'PingFang SC','Microsoft YaHei',sans-serif;}" +
                    "textarea{font-size:14px;width:100%;max-width:640px;min-height:40px;box-sizing:border-box;}" +
                    "input[type=file]{font-size:16px;width:100%;max-width:640px;display:block;box-sizing:border-box;padding:8px 0;}" +
                    "input[type=submit]{font-size:16px;width:100%;max-width:640px;display:block;box-sizing:border-box;padding:8px 14px;}" +
                    "input[type=password]{font-size:16px;width:100%;max-width:640px;display:block;box-sizing:border-box;padding:8px 0;}" +
                    "a{font-size:22px;}" +
                    "ul{line-height:1.6;}" +
                    ".hint{font-size:20px;color:#555;margin:0 0 10px;}" +
                    ".row{margin:10px 0;}" +
                    "</style></head>";

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        File rootDirectory;

        // 获取文件目录

        rootDirectory = Environment.getExternalStorageDirectory(); // Android 10 或更低版本

        final File downloadFile = new File(rootDirectory, "Download");

        // 上传：文件统一保存到 Download 目录(不受密码保护)
        if (Method.POST.equals(session.getMethod())) {
            if ("/login".equals(uri)) {
                return handleLogin(session);
            }
            return handleUpload(session, downloadFile);
        }

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
            // 目录页:若设置了密码且未登录,先显示密码输入,隐藏下面的下载列表
            return renderDirectoryPage(session, uri, targetFile);
        } else {
            // 如果是文件,返回文件内容;设置了密码时必须已登录
            if (isPasswordRequired() && !isAuthenticated(session)) {
                return buildAuthRequiredPage();
            }
            try {
                return newChunkedResponse(Response.Status.OK, "application/octet-stream", new FileInputStream(targetFile));
            } catch (IOException e) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "500 Internal Server Error");
            }
        }
    }

    private boolean isPasswordRequired() {
        return passwordHash != null && !passwordHash.isEmpty();
    }

    private boolean isAuthenticated(IHTTPSession session) {
        if (authToken == null) {
            return true; // 未设置密码,始终视为已登录
        }
        return authToken.equals(session.getCookies().read(AUTH_COOKIE));
    }

    /**
     * 渲染目录页:首页显示上传表单 + 下载说明;设置了密码且未登录时,用密码输入框替代下载列表。
     */
    private Response renderDirectoryPage(IHTTPSession session, String uri, File targetFile) {
        StringBuilder response = new StringBuilder("<html>" + PAGE_HEAD + "<body>");
        response.append("<h1>File Browser</h1>");
        // 上传表单只在首页(/)显示；子目录仅浏览/下载
        if ("/".equals(uri)) {
            response.append("<form method=\"POST\" enctype=\"multipart/form-data\">");
            response.append("<p class=\"hint\">说明: 选择文件、输入文字后，点击上传到 Download 目录, 即可在\"发起端\"收到</p>");
            response.append("<div class=\"row\"><textarea name=\"text\" rows=\"2\" cols=\"40\" placeholder=\"在此输入文字，将保存为时间戳命名的 .txt文件\"></textarea></div>");
            response.append("<div class=\"row\"><input type=\"file\" name=\"file\" multiple></div>");
            response.append("<div class=\"row\"><input type=\"submit\" value=\"上传到 Download 目录\"></div>");
            response.append("</form>");
            // 分割线 + 下载说明
            response.append("<hr>");
            response.append("<hr>");
            response.append("<p class=\"hint\">选择下面的文件夹内容即可下载\"发起端\"的文件</p>");
        }

        if (isPasswordRequired() && !isAuthenticated(session)) {
            // 设置了密码且未登录:在下载说明下面加一行密码输入,通过后才显示可下载内容
            appendLoginForm(response, uri, null);
        } else {
            appendFileList(response, uri, targetFile);
        }
        response.append("</body></html>");
        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", response.toString());
    }

    private void appendLoginForm(StringBuilder response, String uri, String error) {
        if (error != null) {
            response.append("<p class=\"hint\" style=\"color:#c00\">").append(escapeHtml(error)).append("</p>");
        }
        response.append("<form method=\"POST\" action=\"/login\">");
        response.append("<div class=\"row\"><input type=\"password\" name=\"password\" placeholder=\"请输入访问密码\"></div>");
        response.append("<input type=\"hidden\" name=\"next\" value=\"").append(escapeHtml(uri)).append("\">");
        response.append("<div class=\"row\"><input type=\"submit\" value=\"输入密码\"></div>");
        response.append("</form>");
        response.append("<p class=\"hint\">输入正确密码后，才能显示下面的可下载内容</p>");
    }

    private void appendFileList(StringBuilder response, String uri, File targetFile) {
        File[] files = targetFile.listFiles();
        response.append("<ul>");
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String link = uri.endsWith("/") ? uri + fileName : uri + "/" + fileName;
                response.append("<li><a href=\"").append(link).append("\">").append(fileName).append("</a></li>");
            }
        }
        response.append("</ul>");
    }

    /**
     * 处理密码校验:正确则种下会话 cookie(关闭浏览器即失效)并跳回原页面;错误则重渲染原页面并提示。
     */
    private Response handleLogin(IHTTPSession session) {
        try {
            session.parseBody(new HashMap<>());
        } catch (IOException | ResponseException e) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html; charset=UTF-8",
                    buildResultHtml("登录失败", "解析请求出错: " + escapeHtml(e.getMessage()), "/"));
        }

        String next = session.getParms().get("next");
        if (next == null || !next.startsWith("/") || next.contains("..")) {
            next = "/";
        }

        // 未设置密码:无需登录,直接放行
        if (!isPasswordRequired()) {
            return redirect(next);
        }

        String input = session.getParms().get("password");
        if (input != null && sha256Hex(input).equals(passwordHash)) {
            Response resp = redirect(next);
            // 会话 cookie:不写过期时间,关闭浏览器即失效
            resp.addHeader("Set-Cookie", AUTH_COOKIE + "=" + authToken + "; Path=/");
            return resp;
        }

        // 密码错误:重渲染该目录页并提示
        File rootDirectory = Environment.getExternalStorageDirectory();
        File downloadFile = new File(rootDirectory, "Download");
        File targetFile = isOnlyDownload
                ? new File(downloadFile, next)
                : new File(rootDirectory, next);
        StringBuilder response = new StringBuilder("<html>" + PAGE_HEAD + "<body>");
        response.append("<h1>File Browser</h1>");
        if ("/".equals(next)) {
            response.append("<p class=\"hint\">选择下面的文件夹内容即可下载\"发起端\"的文件</p>");
        }
        appendLoginForm(response, next, "密码错误，请重试");
        response.append("</body></html>");
        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8", response.toString());
    }

    private Response redirect(String location) {
        Response resp = newFixedLengthResponse(Response.Status.REDIRECT, "text/html; charset=UTF-8",
                "<html><body><p>正在跳转...</p></body></html>");
        resp.addHeader("Location", location);
        return resp;
    }

    private Response buildAuthRequiredPage() {
        String html = "<html>" + PAGE_HEAD + "<body><h1>需要访问密码</h1>" +
                "<p class=\"hint\">该文件需要先输入访问密码才能下载。</p>" +
                "<a href=\"/\">返回首页输入密码</a></body></html>";
        return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/html; charset=UTF-8", html);
    }

    /**
     * SHA-256 十六进制哈希,用于保存密码与校验登录。
     */
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 一定存在,兜底返回原文
            return input;
        }
    }

    /**
     * 处理 POST 上传：把上传的文件保存到 Download 目录。
     */
    private Response handleUpload(IHTTPSession session, File downloadDir) {
        Map<String, String> files = new HashMap<>();
        try {
            // 浏览器/curl 上传 multipart 时通常不带 charset 参数，NanoHTTPD 会退回用
            // US-ASCII 解码 multipart 头部，导致非 ASCII 文件名被替换成 �。
            // 这里在解析前补上 charset=UTF-8，让 NanoHTTPD 按 UTF-8 解码头部，保留原始文件名。
            Map<String, String> headers = session.getHeaders();
            String contentType = headers.get("content-type");
            if (contentType != null && !contentType.toLowerCase(Locale.US).contains("charset=")) {
                headers.put("content-type", contentType + "; charset=UTF-8");
            }
            session.parseBody(files);
        } catch (IOException | ResponseException e) {
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html; charset=UTF-8",
                    buildResultHtml("上传失败", "解析上传内容出错: " + e.getMessage(), session.getUri()));
        }

        StringBuilder savedNames = new StringBuilder();

        // 文本输入：内容原封不动，保存为时间戳命名的 txt（yyyy-MM-dd-HH-mm-ss）
        String text = session.getParms().get("text");
        if (text != null && !text.isEmpty()) {
            String txtName = new SimpleDateFormat("文本-yyyy-MM-dd-HH-mm-ss", Locale.US).format(new Date()) + ".txt";
            try {
                writeTextFile(new File(downloadDir, txtName), text);
                savedNames.append(escapeHtml(txtName)).append("<br>");
            } catch (IOException e) {
                savedNames.append(escapeHtml(txtName)).append(" (保存失败: ").append(escapeHtml(e.getMessage())).append(")<br>");
            }
        }

        for (Map.Entry<String, String> entry : files.entrySet()) {
            String field = entry.getKey();
            String tempPath = entry.getValue();
            // 跳过原始 POST/PUT 数据等非文件字段
            if ("postData".equals(field) || "content".equals(field)) {
                continue;
            }
            String originalName = session.getParms().get(field);
            String fileName = sanitizeFileName(originalName);
            if (fileName == null) {
                continue;
            }
            try {
                copyFile(new File(tempPath), new File(downloadDir, fileName));
                savedNames.append(escapeHtml(fileName)).append("<br>");
            } catch (IOException e) {
                savedNames.append(escapeHtml(fileName)).append(" (保存失败: ").append(escapeHtml(e.getMessage())).append(")<br>");
            }
        }

        if (savedNames.length() == 0) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html; charset=UTF-8",
                    buildResultHtml("上传失败", "未接收到有效文件或文本", session.getUri()));
        }
        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8",
                buildResultHtml("上传成功", savedNames.toString(), session.getUri()));
    }

    /**
     * 净化上传文件名：只保留最后一段路径，拒绝空名 / "." / ".."，防止路径穿越。
     */
    private String sanitizeFileName(String name) {
        if (name == null) {
            return null;
        }
        String base = name;
        int slash = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
        if (slash >= 0) {
            base = base.substring(slash + 1);
        }
        base = base.trim();
        if (base.isEmpty() || ".".equals(base) || "..".equals(base)) {
            return null;
        }
        return base;
    }

    /**
     * 流式拷贝文件。不用 renameTo：临时文件在应用缓存，目标是外置存储，跨文件系统 renameTo 不可靠。
     */
    private void copyFile(File src, File dest) throws IOException {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
        }
    }

    /**
     * 把文本原封不动地以 UTF-8 写入文件。
     */
    private void writeTextFile(File dest, String content) throws IOException {
        try (FileOutputStream out = new FileOutputStream(dest)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String buildResultHtml(String title, String content, String uri) {
        return "<html>" + PAGE_HEAD + "<body><h1>" + title + "</h1><p>" + content + "</p>" +
                "<a href=\"" + uri + "\">返回目录</a></body></html>";
    }

    /**
     * 转义 HTML 特殊字符，防止文件名等动态内容破坏页面结构。
     */
    private String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}

