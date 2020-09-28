package com.hongqi.mobile.detect.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.widget.Toast;

import com.hongqi.mobile.detect.base.Logger;
import com.hongqi.mobile.detect.tflite.Classifier;
import com.hongqi.mobile.detect.tflite.TFLiteObjectDetectionAPIModel;
import com.hongqi.mobile.detect.utils.ImageUtils;

import java.io.IOException;
import java.util.List;

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
}
