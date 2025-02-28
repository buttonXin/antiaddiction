package com.xreal.evapro.toolsapp.local_server;

import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.xreal.evapro.toolsapp.BaseOLFragment;

import java.io.OutputStream;


public class CopyClipToFileFG extends BaseOLFragment {

    private static final String TAG = CopyClipToFileFG.class.getSimpleName();
    private EditText mEditText;

    public static final String fileName = "ol_tools_app_temp.txt";

    public static final String assetsFileName = "ol_tools.apk";


    @Override
    public void initData() {


        mEditText = addEditText("请输入内容");
        final String paste = getClipContent();
        if (!TextUtils.isEmpty(paste)) {
            mEditText.setText(paste);
        }

        addButton("点击将上面内容保存到-下载", v -> {
            final String content = mEditText.getText().toString();

            saveTextToDownload(mActivity, fileName, content);
            toast("保存成功");

            removeFragment();
        });
        addButton("将当前应用保存到-下载", v -> {

            ApkExporter.exportApkToDownloads(mActivity);
            toast("保存成功");
            removeFragment();
        });

    }

    /**
     * 获取剪切板的内容
     */
    public String getClipContent() {
        ClipboardManager manager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
                CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
                String addedTextString = String.valueOf(addedText);
                if (!TextUtils.isEmpty(addedTextString)) {
                    return addedTextString;
                }
            }
        }
        return "";
    }

    /**
     * 保存文件
     */
    public static void saveTextToDownload(Context context, String fileName, String content) {

        ContentResolver resolver = context.getContentResolver();
        Uri collection;

        // 检查 Android 版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 及以上使用 MediaStore API
            collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        } else {
            // Android 9 及以下使用文件路径
            collection = Uri.parse("file:///sdcard/Download");
        }

        deleteExistingFile(resolver, collection, fileName);

        // 创建保存文件的 ContentValues
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName); // 文件名
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain"); // 文件类型
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/"); // 文件相对路径

        Uri fileUri = resolver.insert(collection, values); // 插入到 MediaStore

        if (fileUri == null) {
            Log.e("FileSaveUtil", "Failed to create file URI");
            return;
        }

        // 写入数据
        try (OutputStream outputStream = resolver.openOutputStream(fileUri)) {
            if (outputStream != null) {
                outputStream.write(content.getBytes()); // 写入字符串数据
                outputStream.flush();
                Log.i("FileSaveUtil", "File saved successfully: " + fileUri.toString());
            }
        } catch (Exception e) {
            Log.e("FileSaveUtil", "Error saving file: " + e.getMessage());
        }
    }

    /**
     * 删除已存在的文件
     */
    private static void deleteExistingFile(ContentResolver resolver, Uri collection, String fileName) {
        String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = new String[]{fileName};

        try (Cursor cursor = resolver.query(collection, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                // 获取文件的 URI
                int idIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                if (idIndex != -1) {
                    long id = cursor.getLong(idIndex);
                    Uri fileUri = Uri.withAppendedPath(collection, String.valueOf(id));
                    resolver.delete(fileUri, null, null); // 删除文件
                    Log.i("FileSaveUtil", "Deleted existing file: " + fileName);
                }
            }
        } catch (Exception e) {
            Log.e("FileSaveUtil", "Error deleting existing file: " + e.getMessage());
        }
    }
}
