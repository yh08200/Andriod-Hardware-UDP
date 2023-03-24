package com.mj.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class FileShareUtils {
    private static final String TAG = "FileShareUtils";
    //文件输出流
    private static OutputStream outputStream;
    /**
     * 维度。A4尺寸为210×297毫米或8.27×11.69英寸。
     * 在PostScript中，其尺寸四舍五入为595×842点。
     */
    private static final int A4_WIDTH = 2520 / 2; // 210 * 6
    private static final int A4_HEIGHT = 3564 / 2; // 297 * 6

    /**
     * 创建csv文件
     *
     * @param context 上下文
     * @param datas   Lists of csv data
     */
    public static void shareCsvFile(Context context, List<String> datas, String fileName) {
        //应用路径：/storage/emulated/0/Android/data/你的应用包名/files/test
        File csvFile = context.getExternalFilesDir("N270x");
        if (!csvFile.exists()) {
            // 如果你想在已经存在的文件夹(zainar)下建立新的文件夹（database），就可以用此方法。
            // 此方法不能在不存在的文件夹下建立新的文件夹。假如想建立名字是"database"文件夹，那么它的父文件夹必须存在。
            csvFile.mkdir();
        }
        //时间戳.csv
        File file = new File(csvFile, fileName + ".csv");
        try {
            //创建一个文件夹
            file.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "createCsvFile: " + e.getMessage());
        }
        //分享CSV
        startIntent(context, getUriForFile(context, writeDataToFile(file, datas)), fileName);
    }

    /**
     * 写数据至文件夹中，创建file
     *
     * @param file     创建CSV文件对象
     * @param dataList 分享的数组
     * @return
     */
    private static File writeDataToFile(final File file, List<String> dataList) {
        //目录是否存在该文件
        if (file.exists()) {
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "writeDataToFile: " + e.getMessage());
            }
            //文件输出流
            final OutputStream putStream = outputStream;
            try { //写入Utf-8文件头
                //在utf-8编码文件中BOM在文件头部，占用三个字节，用来标示该文件属于utf-8编码，
                //现在已经有很多软件识别bom头，但是还有些不能识别bom头，比如PHP就不能识别bom头，
                //这也是用记事本编辑utf-8编码后执行就会出错的原因了
                putStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
            } catch (IOException e) {
                e.printStackTrace();
            }
            //减去1的标题栏
            String[] headerArray = new String[dataList.size() - 1];
            headerArray = dataList.toArray(headerArray);
            //1、普通形式
            try {
                for (int i = 0; i < headerArray.length; i++) {
                    putStream.write((headerArray[i]+"\n").getBytes());
                }
                putStream.close();//关闭流
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "writeDataToFile: " + e.getMessage());
            }
        } else {
            //无法创建CSV文件
            Log.e(TAG, "创建CSV文件失败");
        }
        return file;
    }
    /**
     * 返回uri
     */
    private static Uri getUriForFile(Context context, File file) {
        //应用包名.fileProvider
        String authority = context.getPackageName().concat(".provider");
        Uri fileUri = FileProvider.getUriForFile(context, authority, file);
        return fileUri;
    }

    /**
     * 返回文件夹
     */
    private static File getFileUrl(Context context) {
        File root = context.getFilesDir();
        File dir = new File(root, "hello/");
        if (!dir.exists()) {
            //创建失败
            if (!dir.mkdir()) {
                Log.e(TAG, "createBitmapPdf: 创建失败");
            }
        }
        return dir;
    }

    /**
     * 分享CSV文件
     */
    @SuppressLint("WrongConstant")
    private static void startIntent(Context context, Uri fileUri, String fileName) {

        Log.e(TAG, "startIntent: " + fileUri.toString());
        Log.e(TAG, "startIntent: " + fileName);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.putExtra(Intent.EXTRA_STREAM, fileUri);
        share.putExtra(Intent.EXTRA_SUBJECT, fileName);
        String title = "Share"+fileName;

        share.setType("application/vnd.ms-excel");
        context.startActivity(Intent.createChooser(share, title));
    }
}
