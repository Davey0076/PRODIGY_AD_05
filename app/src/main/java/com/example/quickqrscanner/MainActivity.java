package com.example.quickqrscanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView textViewResult;
    private ExecutorService cameraExecutor;
    private String scannedResult;  // Variable to store the last scanned result

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        textViewResult = findViewById(R.id.textView_result);

        cameraExecutor = Executors.newSingleThreadExecutor();

        startCamera();

        // Make the TextView clickable
        textViewResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scannedResult != null && !scannedResult.isEmpty()) {
                    // Check if the result is a valid URL
                    if (scannedResult.startsWith("http://") || scannedResult.startsWith("https://")) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(scannedResult));
                        startActivity(browserIntent);
                    } else {
                        // Handle other types of results (e.g., display a message)
                        Toast.makeText(MainActivity.this, "Scanned Data: " + scannedResult, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No result to show", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new QRCodeAnalyzer());

        Camera camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis);
    }

    private class QRCodeAnalyzer implements ImageAnalysis.Analyzer {
        @Override
        public void analyze(@NonNull ImageProxy image) {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            if (planes == null || planes.length == 0) {
                image.close();
                return;
            }

            ByteBuffer buffer = planes[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            int width = image.getWidth();
            int height = image.getHeight();

            LuminanceSource source = new ImageProxyLuminanceSource(bytes, width, height);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                Result result = new MultiFormatReader().decode(bitmap);
                scannedResult = result.getText();  // Store the result in the variable
                runOnUiThread(() -> textViewResult.setText(scannedResult));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                image.close();
            }
        }
    }

    private static class ImageProxyLuminanceSource extends LuminanceSource {
        private final byte[] yuvData;
        private final int dataWidth;
        private final int dataHeight;

        protected ImageProxyLuminanceSource(byte[] yuvData, int width, int height) {
            super(width, height);
            this.yuvData = yuvData;
            this.dataWidth = width;
            this.dataHeight = height;
        }

        @Override
        public byte[] getRow(int y, byte[] row) {
            if (y < 0 || y >= getHeight()) {
                throw new IllegalArgumentException("Requested row is outside the image: " + y);
            }
            if (row == null || row.length < getWidth()) {
                row = new byte[getWidth()];
            }
            System.arraycopy(yuvData, y * dataWidth, row, 0, getWidth());
            return row;
        }

        @Override
        public byte[] getMatrix() {
            byte[] matrix = new byte[getWidth() * getHeight()];
            for (int y = 0; y < getHeight(); y++) {
                System.arraycopy(yuvData, y * dataWidth, matrix, y * getWidth(), getWidth());
            }
            return matrix;
        }

        @Override
        public boolean isCropSupported() {
            return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
