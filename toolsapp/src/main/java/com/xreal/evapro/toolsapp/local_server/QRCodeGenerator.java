package com.xreal.evapro.toolsapp.local_server;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {

    public static Bitmap generateQRCode(String content, int width, int height) {
        try {
            // 1. 设置二维码参数
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8"); // 设置字符集
            hints.put(EncodeHintType.ERROR_CORRECTION, "H"); // 设置容错率
            hints.put(EncodeHintType.MARGIN, 1); // 设置边距

            // 2. 创建 MultiFormatWriter 对象
            MultiFormatWriter writer = new MultiFormatWriter();

            // 3. 生成位矩阵 (BitMatrix)
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            // 4. 将位矩阵转换为 Bitmap
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = Color.BLACK; // 黑色色块
                    } else {
                        pixels[y * width + x] = Color.WHITE; // 白色色块
                    }
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}