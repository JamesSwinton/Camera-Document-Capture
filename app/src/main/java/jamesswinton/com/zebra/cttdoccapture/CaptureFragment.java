package jamesswinton.com.zebra.cttdoccapture;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import static android.app.Activity.RESULT_OK;
import static jamesswinton.com.zebra.cttdoccapture.App.CAPTURE_IMAGE;
import static jamesswinton.com.zebra.cttdoccapture.App.TEMP_IMAGE_PATH_ARG;

public class CaptureFragment extends Fragment {

    // Debugging
    private static final String TAG = "CaptureFragment";

    // Constants


    // Variables
    private String mTempImagePath = null;
    private FrameLayout mBaseLayout = null;

    // Required empty public constructor
    public CaptureFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Create View
        View fragmentView = inflater.inflate(R.layout.fragment_capture, container, false);

        // Get Base Layout
        mBaseLayout = fragmentView.findViewById(R.id.base_layout);

        // Init CaptureImage FAB Listener
        mBaseLayout.setOnClickListener(view -> captureNewImage());

        // Return View
        return fragmentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_IMAGE:
                    // Check we have a valid Image Path
                    if (mTempImagePath != null) {
                        // Create ValidateFragment
                        ValidateFragment validateFragment = new ValidateFragment();
                        // Build Argument Bundle
                        Bundle args = new Bundle();
                        args.putString(TEMP_IMAGE_PATH_ARG, mTempImagePath);
                        // Set Fragment Arguments
                        validateFragment.setArguments(args);
                        // Create Transaction
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_container, validateFragment)
                                    .addToBackStack(null)
                                    .commit();
                        } else {
                            App.showErrorDialog(getContext(),
                                    getString(R.string.error_message_no_valid_parent));
                        }
                    } else {
                        App.showErrorDialog(getContext(),
                                getString(R.string.error_message_no_image_path));
                    }
                    break;
            }
        }
    }

    private void captureNewImage() {
        // Create Camera Intent
        Intent captureNewImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Make sure we're attached to an Activity
        if (hasParentActivity(captureNewImage)) {
            // Create Temporary Image File
            File temporaryImageFile = createTempImageFile();
            // Check File created successfully
            if (temporaryImageFile != null) {
                // Store Temporary Image Path
                mTempImagePath = temporaryImageFile.getAbsolutePath();
                // Get URI from File
                Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getActivity()),
                        FILE_PROVIDER, temporaryImageFile);
                // Add URI to Intent
                captureNewImage.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                // Fire Intent
                startActivityForResult(captureNewImage, CAPTURE_IMAGE);
            } else {
                // Show Error
                App.showErrorDialog(getContext(),
                        getString(R.string.error_message_failed_create_temp_image));
            }
        }
    }

    private boolean hasParentActivity(Intent intent) {
        return getActivity() != null && intent.resolveActivity(getActivity().getPackageManager()) != null;
    }

    private File createTempImageFile() {
        // Create Temporary File Name
        String temporaryImageFileName = getString(R.string.temporary_image_file_name,
                String.valueOf(System.currentTimeMillis()));
        try {
            // Return New Temporary File
            return File.createTempFile(temporaryImageFileName, ".jpg",
                    new File(TEMP_IMAGE_DIRECTORY_FILE_PATH));
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
            return null;
        }
    }

}
