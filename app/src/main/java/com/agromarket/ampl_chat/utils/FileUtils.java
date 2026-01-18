package com.agromarket.ampl_chat.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    /**
     * Convert Uri to File for uploading via Multipart
     */
    public static File getFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            String fileName = getFileName(context, uri);
            File file = new File(context.getCacheDir(), fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get file name from Uri
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;

        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        if (result == null) {
            result = "image_" + System.currentTimeMillis() + ".jpg";
        }

        return result;
    }

    /**
     * Get MIME type from Uri
     */
    public static String getMimeType(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            mimeType = "image/*";
        }
        return mimeType;
    }

    /**
     * Check if file size is within limit (in MB)
     */
    public static boolean isFileSizeValid(Context context, Uri uri, int maxSizeMB) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return false;

            long fileSize = inputStream.available();
            inputStream.close();

            long maxSizeBytes = maxSizeMB * 1024 * 1024;
            return fileSize <= maxSizeBytes;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}