package com.viifly.mycam;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class CameraActivity extends AppCompatActivity {
    private final static String TAG = CameraActivity.class.getSimpleName();

    private Camera mCamera;
    private CameraPreview mPreview;
    private MediaRecorder mMediaRecorder;
    private boolean isRecording = false;

    private Button captureButton;
    private Button recordButton;

    private String recordText;
    private String stopText;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Log.d(TAG, "onCreate()");

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, null);
        FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        Resources res = getResources();
        this.recordText = res.getString(R.string.record);
        this.stopText =  res.getString(R.string.stop);

        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );

        recordButton = (Button)findViewById(R.id.button_record_video);
        recordButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (isRecording) {
                            // stop recording and release camera
                            mMediaRecorder.stop();  // stop the recording
                            releaseMediaRecorder(); // release the MediaRecorder object
                            mCamera.lock();         // take camera access back from MediaRecorder

                            // inform the user that recording has stopped
                            setRecordButtonText(recordText);
                            isRecording = false;
                            showToastText("Recording stopped");
                            captureButton.setEnabled(true);
                        } else {
                            // initialize video camera
                            if (prepareVideoRecorder()) {
                                // Camera is available and unlocked, MediaRecorder is prepared,
                                // now you can start recording
                                mMediaRecorder.start();

                                // inform the user that recording has started
                                setRecordButtonText(stopText);
                                isRecording = true;
                                showToastText("Recording started");
                                captureButton.setEnabled(false);
                            } else {
                                // prepare didn't work, release the camera
                                releaseMediaRecorder();
                                // inform user
                            }
                        }
                    }
                }
        );
    }

    private void setRecordButtonText(String text) {
        this.recordButton.setText(text);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        // The activity is about to become visible.

        // Create an instance of Camera
        mCamera = getCameraInstance();
        this.mPreview.setCamera(mCamera);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
        // The activity has become visible (it is now "resumed").
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private void showToastText(String message) {
        Toast msg = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        msg.show();
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                showToastText("Error creating media file");
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            showToastText("Picture Path: " + pictureFile.toString());
            notifyCaptureImgPath(pictureFile.toString());
            mPreview.triggerPreview();
        }
    };

    private void notifyCaptureImgPath(String imgPath) {
        int notificationId = 101;

        Intent resultIntent = new Intent(this, ImageViewActivity.class);
        // Sets the Activity to start in a new, empty task
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        resultIntent.putExtra(ImageViewActivity.INTENT_PARAM_IMG_PATH, imgPath);

        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_action_notify)
                .setContentTitle("MyCam Image captured")
                .setContentText("Image Path: " + imgPath)
                .setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(notificationId, builder.build());
    }


    private boolean prepareVideoRecorder() {
        //mCamera = getCameraInstance();

        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        // Step 3: Set output format and encoding (for versions prior to API Level 8)
        /*
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        */

        // Step 4: Set output file
        mMediaRecorder.setOutputFile(Utils.getOutputMediaFile(Utils.MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        isRecording = false;
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        // The activity is about to be destroyed.
    }

}
