package jamesswinton.com.zebra.cttdoccapture;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AlertDialog;

import java.io.File;

public class App extends Application {

    // Debugging
    private static final String TAG = "ApplicationClass";

    // Constants
    private static final String BASE_DIRECTORY_NAME = "Camera Document Capture";
    private static final String TEMP_IMAGE_DIRECTORY_NAME = "Temporary Images";
    private static final String PERM_IMAGE_DIRECTORY_NAME = "Processed Images";
    public static final String BASE_DIRECTORY_FILE_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath() + File.separator + BASE_DIRECTORY_NAME;
    public static final String TEMP_IMAGE_DIRECTORY_FILE_PATH = BASE_DIRECTORY_FILE_PATH +
            File.separator + TEMP_IMAGE_DIRECTORY_NAME;
    public static final String PERM_IMAGE_DIRECTORY_FILE_PATH = BASE_DIRECTORY_FILE_PATH +
            File.separator + PERM_IMAGE_DIRECTORY_NAME;

    public static final String TEMP_IMAGE_PATH_ARG = "temp-image-path-arg";
    public static final String FILE_PROVIDER = "jamesswinton.com.zebra.cttsimulscan.fileprovider";

    public static final int CAPTURE_IMAGE = 100;

    // Variables

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void showErrorDialog(Context cx, String message) {
        new AlertDialog.Builder(cx)
                .setTitle("Error!")
                .setIcon(R.drawable.ic_error)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create()
                .show();
    }
}
