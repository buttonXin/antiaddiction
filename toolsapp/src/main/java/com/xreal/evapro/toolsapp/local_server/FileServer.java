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
import java.util.function.Consumer;
import java.util.function.Supplier;

import fi.iki.elonen.NanoHTTPD;

public class FileServer extends NanoHTTPD {
    private final boolean isOnlyDownload;
    private Context context;

    // 访问密码的 SHA-256 哈希;为空表示未设置密码,下载无需密码
    private final String passwordHash;
    // 由密码哈希派生的会话令牌;密码设置后,浏览器验证通过便凭此 cookie 免重复输入
    private final String authToken;
    // 网页最上方的提示内容;每次请求实时获取,所以改内容后刷新网页即可看到
    private final Supplier<String> noticeSupplier;
    // 网页端点"发送到手机"时回调,把文字交给界面显示;在服务器线程调用,界面需自行切回主线程
    private final Consumer<String> textReceiver;
    // 最近一次网页端发来的文字;没接回调时也可通过 getLastPushedText() 拿到
    private volatile String lastPushedText = "";

    private static final String AUTH_COOKIE = "fs_auth";
    private static final String AUTH_TOKEN_SALT = "::local_server_session::";

    public FileServer(int port, Context context, boolean isOnlyDownload, String passwordHash,
                      Supplier<String> noticeSupplier, Consumer<String> textReceiver) {
        super(port);
        this.context = context;
        this.isOnlyDownload = isOnlyDownload;
        this.passwordHash = passwordHash;
        this.noticeSupplier = noticeSupplier;
        this.textReceiver = textReceiver;
        this.authToken = (passwordHash == null || passwordHash.isEmpty())
                ? null
                : sha256Hex(passwordHash + AUTH_TOKEN_SALT);
    }

    public FileServer(int port, Context context, boolean isOnlyDownload, String passwordHash,
                      Supplier<String> noticeSupplier) {
        this(port, context, isOnlyDownload, passwordHash, noticeSupplier, null);
    }

    public FileServer(int port, Context context, boolean isOnlyDownload, String passwordHash) {
        this(port, context, isOnlyDownload, passwordHash, null, null);
    }

    public FileServer(int port, Context context, boolean isOnlyDownload) {
        this(port, context, isOnlyDownload, null, null, null);
    }

    public FileServer(int port, Context context) {
        this(port, context, false, null, null, null);
    }

    /**
     * 最近一次网页端"发送到手机"的文字;没有则为空串。
     */
    public String getLastPushedText() {
        return lastPushedText;
    }

    // 页面公共头部：字体放大，方便其他设备上的用户阅读
    private static final String PAGE_HEAD =
            "<head><meta charset=\"UTF-8\">" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                    "<title>快捷分享</title>" +
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
                    ".notice{font-size:22px;background:#fff8e1;border:1px solid #ffe082;border-radius:6px;padding:10px 12px;margin:0 0 14px;white-space:pre-wrap;word-break:break-word;}" +
                    // 输入框与"发送到手机"并排:输入框自适应占满剩余宽度,按钮保持自身宽度
                    ".push-row{display:flex;align-items:stretch;gap:10px;max-width:640px;}" +
                    ".push-row textarea{flex:1 1 auto;min-width:0;}" +
                    ".push-row input[type=submit]{flex:0 0 auto;width:auto;max-width:none;}" +
                    // 说明文字与"复制"按钮同一行
                    ".notice-head{display:flex;align-items:center;gap:10px;max-width:640px;}" +
                    ".notice-head .hint{margin:0;}" +
                    ".copy-btn{font-size:16px;padding:4px 14px;cursor:pointer;}" +
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
            // 只把文字送到手机显示,不落文件
            if ("/push".equals(uri)) {
                return handlePush(session);
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
        response.append("<h1>快捷分享</h1>");
        // 动态提示紧跟在标题下面
        appendNotice(response);
        // 上传表单只在首页(/)显示；子目录仅浏览/下载
        if ("/".equals(uri)) {
            response.append("<form method=\"POST\" enctype=\"multipart/form-data\">");
            // 输入框与"发送到手机"同处一行,一眼能看出这个按钮作用于输入框里的文字
            response.append("<hr>");
            response.append("<p class=\"hint\">输入文字后点右边的\"发送到手机\"，文字会直接显示在手机上，不保存文件</p>");
            response.append("<div class=\"row push-row\">");
            response.append("<textarea name=\"text\" rows=\"2\" placeholder=\"在此输入文字\"></textarea>");
            response.append("<input type=\"submit\" formaction=\"/push\" value=\"发送到手机\">");
            response.append("</div>");
            response.append("<hr>");
            response.append("<p class=\"hint\">选择文件后点下面的按钮，文件与文字会存到手机 Download 目录</p>");
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

    /**
     * 网页最上方的提示:设置后放在页面最前面,密码验证之前也能看到。
     */
    private void appendNotice(StringBuilder response) {
        String notice = noticeSupplier == null ? null : noticeSupplier.get();
        if (notice == null || notice.trim().isEmpty()) {
            return;
        }
        // 提示框上方加一行说明,避免用户不知道这块内容从哪来;说明后面跟一个复制按钮
        response.append("<div class=\"notice-head\">")
                .append("<p class=\"hint\">发起端分享文案:</p>")
                .append("<button type=\"button\" id=\"noticeCopyBtn\" class=\"copy-btn\">复制</button>")
                .append("</div>")
                .append("<div class=\"notice\" id=\"noticeBox\">").append(escapeHtml(notice)).append("</div>");
        appendCopyScript(response);
    }

    /**
     * 复制按钮的脚本:点一下把上面那块文案放进剪贴板。
     * <p>
     * 页面是通过 http + 局域网 IP 打开的，不属于安全上下文，navigator.clipboard 不可用，
     * 所以先试新 API，失败再退回 execCommand 的老办法(临时 textarea + 选中 + copy)。
     * 文案内容从 DOM 里读，不拼进脚本，避免转义问题。
     */
    private void appendCopyScript(StringBuilder response) {
        response.append("<script>")
                .append("(function(){")
                .append("var b=document.getElementById('noticeCopyBtn'),n=document.getElementById('noticeBox');")
                .append("if(!b||!n){return;}")
                .append("function fallback(t){")
                .append("var a=document.createElement('textarea');")
                .append("a.value=t;a.setAttribute('readonly','');")
                .append("a.style.position='fixed';a.style.top='-1000px';")
                .append("document.body.appendChild(a);a.select();a.setSelectionRange(0,a.value.length);")
                .append("var ok=false;try{ok=document.execCommand('copy');}catch(e){ok=false;}")
                .append("document.body.removeChild(a);return ok;}")
                .append("function done(ok){var o=b.textContent;b.textContent=ok?'已复制':'复制失败';")
                .append("setTimeout(function(){b.textContent=o;},1500);}")
                .append("b.addEventListener('click',function(){")
                .append("var t=n.textContent;")
                .append("if(navigator.clipboard&&window.isSecureContext){")
                .append("navigator.clipboard.writeText(t).then(function(){done(true);},function(){done(fallback(t));});")
                .append("}else{done(fallback(t));}});")
                .append("})();")
                .append("</script>");
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
            parseBodyAsUtf8(session, new HashMap<>());
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
        response.append("<h1>快捷分享</h1>");
        appendNotice(response);
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
        StringBuilder response = new StringBuilder("<html>" + PAGE_HEAD + "<body>");
        response.append("<h1>快捷分享</h1>");
        appendNotice(response);
        response.append("<p class=\"hint\">该文件需要先输入访问密码才能下载。</p>")
                .append("<a href=\"/\">返回首页输入密码</a></body></html>");
        return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "text/html; charset=UTF-8", response.toString());
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
     * 解析 POST body，并强制按 UTF-8 解码。
     * <p>
     * 浏览器/curl 上传 multipart 时通常不带 charset 参数，NanoHTTPD 会退回用
     * US-ASCII 解码 multipart 的头部与字段值，导致中文等非 ASCII 内容变成乱码。
     * 这里在解析前补上 charset=UTF-8，让它按 UTF-8 解码，保留原始内容。
     */
    private void parseBodyAsUtf8(IHTTPSession session, Map<String, String> files)
            throws IOException, ResponseException {
        Map<String, String> headers = session.getHeaders();
        String contentType = headers.get("content-type");
        if (contentType != null && !contentType.toLowerCase(Locale.US).contains("charset=")) {
            headers.put("content-type", contentType + "; charset=UTF-8");
        }
        session.parseBody(files);
    }

    /**
     * 处理"发送到手机"：只把网页端输入的文字交给界面显示，不保存文件。
     */
    private Response handlePush(IHTTPSession session) {
        try {
            parseBodyAsUtf8(session, new HashMap<>());
        } catch (IOException | ResponseException e) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html; charset=UTF-8",
                    buildResultHtml("发送失败", "解析请求出错: " + escapeHtml(e.getMessage()), "/"));
        }

        String text = session.getParms().get("text");
        if (text == null || text.trim().isEmpty()) {
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html; charset=UTF-8",
                    buildResultHtml("发送失败", "内容为空，请先在输入框里写点东西", "/"));
        }

        lastPushedText = text;
        if (textReceiver != null) {
            textReceiver.accept(text);
        }
        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=UTF-8",
                buildResultHtml("已发送到手机", "<div class=\"notice\">" + escapeHtml(text) + "</div>", "/"));
    }

    /**
     * 处理 POST 上传：把上传的文件保存到 Download 目录。
     */
    private Response handleUpload(IHTTPSession session, File downloadDir) {
        Map<String, String> files = new HashMap<>();
        try {
            parseBodyAsUtf8(session, files);
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

