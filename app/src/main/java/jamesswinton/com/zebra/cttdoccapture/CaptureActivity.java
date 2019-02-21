package jamesswinton.com.zebra.cttdoccapture;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

import static jamesswinton.com.zebra.cttdoccapture.App.BASE_DIRECTORY_FILE_PATH;
import static jamesswinton.com.zebra.cttdoccapture.App.PERM_IMAGE_DIRECTORY_FILE_PATH;
import static jamesswinton.com.zebra.cttdoccapture.App.TEMP_IMAGE_DIRECTORY_FILE_PATH;

public class CaptureActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "CaptureActivity";

    // Constants
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 100;

    // Variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        // Request Permissions
        getPermission();

        // Init Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("CTT Document Capture");
            getSupportActionBar().setSubtitle("Capture, process & store documents");
        }

        // Create Directories if required
        initDirectories();

        // Show Capture Fragment
        showCaptureFragment(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings: {
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
        }
        return true;
    }

    private void initDirectories() {
        File baseDirectory = new File(BASE_DIRECTORY_FILE_PATH);
        if (!baseDirectory.exists()) {
            Log.i(TAG, "Base Directory Created: " + baseDirectory.mkdirs());
        }

        File tempImageDirectory = new File(TEMP_IMAGE_DIRECTORY_FILE_PATH);
        if (!tempImageDirectory.exists()) {
            Log.i(TAG, "Temp Image Directory Created: " + tempImageDirectory.mkdirs());
        }

        File permImageDirectory = new File(PERM_IMAGE_DIRECTORY_FILE_PATH);
        if (!permImageDirectory.exists()) {
            Log.i(TAG, "Perm Image Directory Created: " + permImageDirectory.mkdirs());
        }
    }

    private void showCaptureFragment(Bundle savedInstanceState) {
        // Only Display Fragment if required
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new CaptureFragment())
                    .commit();
        }
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    getPermission();
                } else {
                    initDirectories();
                }
            }
        }
    }
}
