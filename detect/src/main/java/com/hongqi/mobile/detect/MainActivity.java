package com.hongqi.mobile.detect;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.hongqi.mobile.detect.base.Logger;
import com.hongqi.mobile.detect.tflite.Classifier;
import com.hongqi.mobile.detect.tflite.TFLiteObjectDetectionAPIModel;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private Classifier detector;
    private static final Logger LOGGER = new Logger();

    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDetect();
    }

    private void initDetect() {

        int cropSize = TF_OD_API_INPUT_SIZE;
        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }
    }

    private void detect(Bitmap croppedBitmap){
        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
        for (final Classifier.Recognition result : results) {
            final RectF location = result.getLocation();
            LOGGER.e("########################"+result.getTitle());
            if (location != null && result.getConfidence() >= MINIMUM_CONFIDENCE_TF_OD_API) {
                LOGGER.e("########################location#####"+location);
            }
        }
    }
}
