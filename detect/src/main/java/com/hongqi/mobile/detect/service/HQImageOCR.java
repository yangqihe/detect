package com.hongqi.mobile.detect.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.google.zxing.pdf417.encoder.BarcodeMatrix;
import com.google.zxing.qrcode.QRCodeReader;
import com.hongqi.mobile.detect.base.Logger;
import com.hongqi.mobile.detect.tflite.Classifier;
import com.hongqi.mobile.detect.tflite.TFLiteObjectDetectionAPIModel;
import com.hongqi.mobile.detect.utils.ImageUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class HQImageOCR {

    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static Classifier detector;
    private static final Logger LOGGER = new Logger();
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static Bitmap croppedBitmap = null;
    private static Matrix frameToCropTransform;
    private static final boolean MAINTAIN_ASPECT = false;
    private static Context context;

    public static void init(Context context_){
        context=context_;
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            context.getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            croppedBitmap = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Bitmap.Config.ARGB_8888);

        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast = Toast.makeText(context, "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public static Bitmap scalBitmap(Bitmap bitmap){
        if (bitmap==null)return null;
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        bitmap.getWidth(), bitmap.getHeight(),
                        TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE,
                        0, MAINTAIN_ASPECT);

        Matrix cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(bitmap, frameToCropTransform, null);
        return croppedBitmap;
    }

    public static String identify(Bitmap croppedBitmap){
        if (croppedBitmap==null)return "bitmap不能为空";
        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
        String resultString = "";
        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            resultString += result.getTitle()+" "+result.getConfidence()+" \n";
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                LOGGER.e("########################location#####"+location);
            }
        }
        return resultString;
    }

    public static String parseQRcode(Bitmap bmp){
        //bmp=comp(bmp);//bitmap压缩  如果不压缩的话在低配置的手机上解码很慢

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        QRCodeReader reader = new QRCodeReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);//优化精度
        hints.put(DecodeHintType.CHARACTER_SET,"utf-8");//解码设置编码方式为：utf-8
        try {
            Result result = reader.decode(new BinaryBitmap(
                    new HybridBinarizer(new RGBLuminanceSource(width, height, pixels))), hints);
            return result.getText();
        } catch (NotFoundException e) {
            Log.i("ansen",""+e.toString());
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String parseBarcode(Bitmap bmp){
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);//优化精度
        hints.put(DecodeHintType.CHARACTER_SET,"utf-8");//解码设置编码方式为：utf-8

        Collection<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.UPC_A);
        formats.add(BarcodeFormat.EAN_13);
        formats.add(BarcodeFormat.EAN_8);
        formats.add(BarcodeFormat.CODABAR);
        formats.add(BarcodeFormat.CODE_39);
        formats.add(BarcodeFormat.CODE_93);
        formats.add(BarcodeFormat.CODE_128);
        formats.add(BarcodeFormat.ITF);
        formats.add(BarcodeFormat.RSS_14);
        formats.add(BarcodeFormat.RSS_EXPANDED);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        reader.setHints(hints);
        try {
            Result result = reader.decodeWithState(binaryBitmap);
            return result.getText();
        } catch (NotFoundException e) {
            Log.e("parseBarcode:",""+e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
