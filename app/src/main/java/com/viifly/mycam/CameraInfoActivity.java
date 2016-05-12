package com.viifly.mycam;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CameraInfoActivity extends AppCompatActivity {
    private TextView countTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_info);

        countTextView = (TextView)findViewById(R.id.textView2);

        int n = Camera.getNumberOfCameras();
        countTextView.setText("Camera count: " + n);

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        //List<String> textLines = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < n; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            sb.append("Camera# ").append(i).append("\n");
            sb.append(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK ?
                    "FACING_BACK" : "FACING_FRONT").append("\n");
            sb.append("orientation: ").append(cameraInfo.orientation).append("\n");
        }
        TextView tv3 = (TextView)findViewById(R.id.textView3);
        tv3.setText(sb.toString());

    }

    @Override
    protected void onStart(){
        super.onStart();

    }
}
