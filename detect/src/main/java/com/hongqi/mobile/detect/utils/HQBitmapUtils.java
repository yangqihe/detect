package com.hongqi.mobile.detect.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HQBitmapUtils {

    public static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);

        return bm1;
    }


    public static void saveImageToSysAlbum(Context context, Bitmap bitmap) {
        if (bitmap==null){
            Toast.makeText(context,"保存失败",Toast.LENGTH_SHORT).show();
            return;
        }
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "HaiGouShareCode", "");
        //如果是4.4及以上版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String fileName = System.currentTimeMillis() + ".jpeg";
            File mPhotoFile = new File(fileName);
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(mPhotoFile);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        } else {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
        Toast.makeText(context,"已保存到相册",Toast.LENGTH_SHORT).show();
    }


    //目前 300多K可以处理
    public static int calculateSampleSize(Context context,BitmapFactory.Options options) { //图片过大不显示图片问题   图片过大opencv不能正常检测问题
        int outHeight = options.outHeight;
        int outWidth = options.outWidth;
        int sampleSize = 1;
        int destHeight = 1080;//1000*2;
        int destWidth = 1440;//1000*2;  图片的显示比例
        if (outHeight > destHeight || outWidth > destHeight) {
            if (outHeight > outWidth) {
                sampleSize = outHeight / destHeight;
            } else {
                sampleSize = outWidth / destWidth;
            }
        }
        if (sampleSize < 1) {
            sampleSize = 1;
        }
        //HQLogUtils.e(context,"sampleSize:"+sampleSize);
        return sampleSize;
    }

    public static int calculateSampleSize(Context context,Bitmap bitmap) { //图片过大不显示图片问题   图片过大opencv不能正常检测问题
        int outHeight = bitmap.getHeight();
        int outWidth = bitmap.getWidth();
        int sampleSize = 1;
        int destHeight = 300;
        int destWidth = 300;//
        if (outHeight > destHeight || outWidth > destHeight) {
            if (outHeight > outWidth) {
                sampleSize = outHeight / destHeight;
            } else {
                sampleSize = outWidth / destWidth;
            }
        }
        if (sampleSize < 1) {
            sampleSize = 1;
        }
        //HQLogUtils.e(context,"sampleSize:"+sampleSize);
        return sampleSize;
    }

    public static void saveImage(Bitmap bitmap, File saveFile) {
        try {
            FileOutputStream fos = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static Bitmap zoomBitmap(int width,int height,Bitmap bitmap) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable=true;
        options.inInputShareable=true;
        float scaleWidth = (float) 1.0 / width;
        float scaleHeight = (float) 1.0 / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap scaleBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return scaleBitmap;
    }

}
