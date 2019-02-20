package jamesswinton.com.zebra.cttdoccapture;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dynamsoft.camerasdk.exception.DcsCameraNotAuthorizedException;
import com.dynamsoft.camerasdk.exception.DcsException;
import com.dynamsoft.camerasdk.exception.DcsValueNotValidException;
import com.dynamsoft.camerasdk.io.DcsJPEGEncodeParameter;
import com.dynamsoft.camerasdk.model.DcsDocument;
import com.dynamsoft.camerasdk.model.DcsImage;
import com.dynamsoft.camerasdk.view.DcsDocumentEditorView;
import com.dynamsoft.camerasdk.view.DcsDocumentEditorViewListener;
import com.dynamsoft.camerasdk.view.DcsVideoView;
import com.dynamsoft.camerasdk.view.DcsVideoViewListener;
import com.dynamsoft.camerasdk.view.DcsView;

import java.io.File;
import java.io.IOException;

import static jamesswinton.com.zebra.cttdoccapture.App.TEMP_IMAGE_DIRECTORY_FILE_PATH;

public class CameraActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "CaptureActivity";

    // Constants


    // Variables
    private DcsView mDcsView;
    private Intent returnDataIntent;
    private String mTempImagePath;
    private File mTempImagesDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Init Dynamsoft SDK (Close Activity if failure)
        if (!initCameraSDK()) {
            setResult(RESULT_CANCELED);
            finish();
        }

        // Init ReturnDataIntent
        returnDataIntent = new Intent();

        // Initialise DCS View
        initDcsView();

        // Listen on Capture (for cancel)
        initCaptureListener();

        // Listen on Editor
        initEditorListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start Preview
        if(mDcsView.getCurrentView() == DcsView.DVE_VIDEOVIEW){
            try {
                mDcsView.getVideoView().preview();
            } catch (DcsCameraNotAuthorizedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop Preview
        mDcsView.getVideoView().stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destroy VideoView
        mDcsView.getVideoView().destroyCamera();
    }

    private boolean initCameraSDK() {
        try {
            DcsView.setLicense(this, getString(R.string.dynamsoft_camera_license));
            return true;
        } catch (DcsValueNotValidException e) {
            Log.e(TAG, "InvalidDynamsoftLicenseException: " + e.getMessage());
            return false;
        }
    }

    private void initDcsView() {
        // Get DCS View
        mDcsView = findViewById(R.id.dcs_view);
        // Start VideoView
        mDcsView.setCurrentView(DcsView.DVE_VIDEOVIEW);
        // Allow Editing Post - Capture
        mDcsView.getVideoView().setNextViewAfterCapture(DcsView.DVE_EDITORVIEW);
    }

    private void initCaptureListener() {
        mDcsView.getVideoView().setListener(new DcsVideoViewListener() {
            @Override
            public boolean onPreCapture(DcsVideoView dcsVideoView) {
                return false;
            }

            @Override
            public void onCaptureFailure(DcsVideoView dcsVideoView, DcsException e) {

            }

            @Override
            public void onPostCapture(DcsVideoView dcsVideoView, DcsImage dcsImage) {

            }

            @Override
            public void onCancelTapped(DcsVideoView dcsVideoView) {
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onCaptureTapped(DcsVideoView dcsVideoView) {

            }

            @Override
            public void onDocumentDetected(DcsVideoView dcsVideoView, DcsDocument dcsDocument) {

            }
        });
    }

    private void initEditorListener() {
        mDcsView.getDocumentEditorView().setListener(new DcsDocumentEditorViewListener() {
            @Override
            public void onCancelTapped(DcsDocumentEditorView dcsDocumentEditorView) {
                // Return Cancelled Result
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onOkTapped(DcsDocumentEditorView dcsDocumentEditorView, DcsException ex) {
                try {
                    // Save File
                    mDcsView.getIO().save(
                            new int[]{ mDcsView.getBuffer().getCurrentIndex() },
                            createTempImageFile(),
                            new DcsJPEGEncodeParameter()
                    );
                    // Finish Activity -> Return Path
                    returnDataIntent.putExtra("image-path", mTempImagePath);
                    setResult(RESULT_OK, returnDataIntent);
                    finish();
                } catch (IOException e) {
                    // Log Exception
                    Log.e(TAG, "IOException: " + e.getMessage());
                    // Return Cancelled Result
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        });
    }

    private String createTempImageFile() throws IOException {
        // Create Temporary File Name
        String temporaryImageFileName = getString(R.string.temporary_image_file_name,
                String.valueOf(System.currentTimeMillis()));
        // Create New Temporary File
        File tempImageFile = File.createTempFile(temporaryImageFileName, ".jpg",
                new File(TEMP_IMAGE_DIRECTORY_FILE_PATH));
        // Set mTempImagePath
        mTempImagePath = tempImageFile.getAbsolutePath();
        // Return Temporary File Path
        return mTempImagePath;

    }
}
