package jamesswinton.com.zebra.cttdoccapture;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.dynamsoft.camerasdk.exception.DcsCameraNotAuthorizedException;
import com.dynamsoft.camerasdk.exception.DcsException;
import com.dynamsoft.camerasdk.exception.DcsValueNotValidException;
import com.dynamsoft.camerasdk.io.DcsJPEGEncodeParameter;
import com.dynamsoft.camerasdk.io.ISave;
import com.dynamsoft.camerasdk.model.DcsDocument;
import com.dynamsoft.camerasdk.model.DcsImage;
import com.dynamsoft.camerasdk.view.DcsDocumentEditorView;
import com.dynamsoft.camerasdk.view.DcsDocumentEditorViewListener;
import com.dynamsoft.camerasdk.view.DcsVideoView;
import com.dynamsoft.camerasdk.view.DcsVideoViewListener;
import com.dynamsoft.camerasdk.view.DcsView;
import com.dynamsoft.camerasdk.view.DcsViewListener;

import java.io.File;
import java.io.IOException;
import java.util.zip.Inflater;

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

    private AlertDialog mSaveProgressDialog;
    private ProgressBar mSaveProgressBar;
    private SharedPreferences mPreferenceManager;

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

        // Init Preference Manager
        mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(this);

        //
        initSaveProgressDialog();

        // Initialise DCS View
        initDcsView();

        //
        initViewChangeListener();

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

    private void initSaveProgressDialog() {
        if (mSaveProgressDialog == null) {
            mSaveProgressDialog = new AlertDialog.Builder(this)
                    .setTitle("Saving Image")
                    .setView(R.layout.dialog_save_progress)
                    .setCancelable(false)
                    .create();
        }
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
        // Remove Cancel Button
        mDcsView.getVideoView().setShowCancelToolItem(false);
        // Allow Editing Post - Capture
        mDcsView.getVideoView().setNextViewAfterCapture(DcsView.DVE_EDITORVIEW);
        // Allow Canceling without opening Gallery
        mDcsView.getVideoView().setNextViewAfterCancel(DcsView.DVE_VIDEOVIEW);
        // Set Flash Mode
        setTorchMode();
    }

    private void setTorchMode() {
        // Get String
        int torchMode = Integer.parseInt(mPreferenceManager.getString("torch_mode", "0"));
        // Always On
        if (torchMode == 0) {
            mDcsView.getVideoView().setFlashMode(DcsVideoView.DFME_TORCH);
            // Always Off
        } else if (torchMode == 1) {
            mDcsView.getVideoView().setFlashMode(DcsVideoView.DFME_OFF);
            // Auto
        } else if (torchMode == 2) {
            mDcsView.getVideoView().setFlashMode(DcsVideoView.DFME_AUTO);
            // On At Capture
        } else if (torchMode == 3) {
            mDcsView.getVideoView().setFlashMode(DcsVideoView.DFME_ON);
        }
    }

    private void initCaptureListener() {
        mDcsView.getVideoView().setListener(new DcsVideoViewListener() {
            @Override
            public boolean onPreCapture(DcsVideoView dcsVideoView) {
                Log.i(TAG, "Preparing to capture image...");
                return true;
            }

            @Override
            public void onCaptureFailure(DcsVideoView dcsVideoView, DcsException e) {
                Log.i(TAG, "Error! Could not capture image");
            }

            @Override
            public void onPostCapture(DcsVideoView dcsVideoView, DcsImage dcsImage) {
                Log.i(TAG, "Image Captured");
            }

            @Override
            public void onCancelTapped(DcsVideoView dcsVideoView) {
                setResult(RESULT_CANCELED);
                finish();
            }

            @Override
            public void onCaptureTapped(DcsVideoView dcsVideoView) {
                Log.i(TAG, "Capture Image Button Tapped...");
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
                    mDcsView.getIO().saveAsync(
                            new int[]{mDcsView.getBuffer().getCurrentIndex()},
                            createTempImageFile(),
                            new DcsJPEGEncodeParameter(),
                            new ISave() {
                                @Override
                                public boolean onSaveProgress(int i) {
                                    mSaveProgressDialog.show();
                                    return true;
                                }

                                @Override
                                public void onSaveSuccess(Object o) {
                                    // Remove Dialog
                                    if (mSaveProgressDialog.isShowing()) {
                                        mSaveProgressDialog.dismiss();
                                    }
                                    // Finish Activity -> Return Path
                                    returnDataIntent.putExtra("image-path", mTempImagePath);
                                    setResult(RESULT_OK, returnDataIntent);
                                    finish();
                                }

                                @Override
                                public void onSaveFailure(Object o, DcsException e) {

                                }
                            }
                    );
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

    private void initViewChangeListener() {
        mDcsView.setListener((dcsView, i, i1) -> {
            if (i1 == DcsView.DVE_EDITORVIEW){
                // Set View to prevent Gallery Showing
                mDcsView.getDocumentEditorView().setNextViewAfterCancel(DcsView.DVE_EDITORVIEW);
                mDcsView.getDocumentEditorView().setNextViewAfterOK(DcsView.DVE_EDITORVIEW);

                // Set Prefs
                mDcsView.getDocumentEditorView().adjustBrightness(
                        mPreferenceManager.getInt("brightness", 30));
                mDcsView.getDocumentEditorView().adjustContrast(
                        mPreferenceManager.getInt("contrast", 70));

                // Set Filter Mode
                setFilterMode();
            }
        });
    }

    private void setFilterMode() {
        // Get String
        int filterMode = Integer.parseInt(mPreferenceManager.getString("filter", "0"));
        // Colour
        if (filterMode == 1) {
            mDcsView.getDocumentEditorView().toColor();
        // Grey
        } else if (filterMode == 2) {
            mDcsView.getDocumentEditorView().toGrey();
        // B & W
        } else if (filterMode == 3) {
            mDcsView.getDocumentEditorView().toBlackWhite();
        }
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
