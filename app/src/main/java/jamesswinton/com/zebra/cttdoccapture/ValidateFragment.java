package jamesswinton.com.zebra.cttdoccapture;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dynamsoft.barcode.BarcodeReader;
import com.dynamsoft.barcode.BarcodeReaderException;
import com.dynamsoft.barcode.TextResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static jamesswinton.com.zebra.cttdoccapture.App.PERM_IMAGE_DIRECTORY_FILE_PATH;
import static jamesswinton.com.zebra.cttdoccapture.App.TEMP_IMAGE_PATH_ARG;

public class ValidateFragment extends Fragment {

    // Debugging
    private static final String TAG = "ValidateFragment";

    // Constants


    // Variables
    private ImageView mImagePreview;
    private Spinner mBarcodeSpinner;
    private AlertDialog mDecodeProgressDialog;
    private FloatingActionButton mSaveButton, mDeleteButton;

    private static Exception mException = null;
    private static String mSelectedBarcode = null;
    private static boolean mBarcodeDecoded = false;
    private static BarcodeReader mBarcodeReader = null;
    private static SharedPreferences mPreferenceManager = null;
    private static List<String> mDecodedBarcodes = new ArrayList<>();

    public ValidateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create View
        View fragmentView = inflater.inflate(R.layout.fragment_validate, container,
                false);

        // Get UI Elements
        mSaveButton = fragmentView.findViewById(R.id.save_image);
        mDeleteButton = fragmentView.findViewById(R.id.delete_image);
        mImagePreview = fragmentView.findViewById(R.id.image_preview);
        mBarcodeSpinner = fragmentView.findViewById(R.id.barcode_spinner);

        // Set Click Listeners
        mSaveButton.setOnClickListener(view -> saveImage());
        mDeleteButton.setOnClickListener(view -> deleteImage());

        // Return View
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getImagePath() != null) {
            // Get Preference Manager
            mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(getContext());
            // Load Image
            loadImage(getImagePath());
            // Attempt Decode
            new DecodeBarcodeAsync().execute();
        } else {
            App.showErrorDialog(getContext(), getString(R.string.error_message_no_image_path));
        }
    }

    private void saveImage() {
        // Get Temporary File
        File tempImage = new File(getImagePath());
        // Check File Exists
        if (tempImage.exists()) {
            // Get File Name (Barcode if selected, otherwise current time)
            String fileName = mBarcodeDecoded ?
                    mBarcodeSpinner.getSelectedItem().toString() :
                    String.valueOf(System.currentTimeMillis());
            // Create New File
            File permImage = new File(PERM_IMAGE_DIRECTORY_FILE_PATH
                    + File.separator
                    + fileName
                    + ".jpg");
            // Rename tempImage to permImage
            if (tempImage.renameTo(permImage)) {
                Toast.makeText(getContext(), "File Created: " + permImage.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
                getActivity().getSupportFragmentManager().popBackStack();
            } else {
                App.showErrorDialog(getContext(),
                        getString(R.string.error_message_cant_create_perm_image));
            }
        } else {
            App.showErrorDialog(getContext(),
                    getString(R.string.error_message_no_image_path));
        }
    }

    private void deleteImage() {
        // Get Temporary file
        File tempImage = new File(getImagePath());
        // Check File Exits
        if (tempImage.exists()) {
            String tempImageAbsolutePath = tempImage.getAbsolutePath();
            if (tempImage.delete()) {
                Toast.makeText(getContext(), "File Deleted: " + tempImageAbsolutePath,
                        Toast.LENGTH_LONG).show();
                getActivity().getSupportFragmentManager().popBackStack();
            } else {
                App.showErrorDialog(getContext(),
                        getString(R.string.error_message_cant_delete_temp_image));
            }
        } else {
            App.showErrorDialog(getContext(),
                    getString(R.string.error_message_no_image_path));
        }
    }

    private void loadImage(String imagePath) {
        // Load Image
        Glide.with(this)
                .load(imagePath)
                .into(mImagePreview);
    }

    private String getImagePath() {
        return getArguments() == null ? null : getArguments().getString(TEMP_IMAGE_PATH_ARG);
    }

    private class DecodeBarcodeAsync extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Display Indeterminate dialog
            if (mDecodeProgressDialog == null) {
                mDecodeProgressDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Decoding Image")
                        .setView(R.layout.dialog_progress)
                        .setCancelable(false)
                        .create();
            }

            // Display Dialog
            mDecodeProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // Clear Existing Barcodes
                mDecodedBarcodes = new ArrayList<>();
                // Init Barcode Reader
                if (mBarcodeReader == null) {
                    mBarcodeReader = new BarcodeReader(
                            mPreferenceManager.getString("barcode_reader_license",
                                    getString(R.string.dynamsoft_scanner_license)));
                }
                // Attempt Decode
                TextResult[] decodeResults = mBarcodeReader.decodeFile(getImagePath(), "");
                // Loop results
                if (decodeResults != null && decodeResults.length > 0) {
                    // Add Results to Array
                    for (TextResult barcode : decodeResults) {
                        mDecodedBarcodes.add(barcode.barcodeText);
                    }
                    // Notify onPostExecute
                    return true;
                } else {
                    // Set Selected Barcode to Generic String
                    mSelectedBarcode = getString(R.string.error_message_no_image_decode);
                    // Add Generic string to List
                    mDecodedBarcodes.add(mSelectedBarcode);
                    // Notify onPostExecute
                    return false;
                }

            } catch (Exception e) {
                // Log Error
                Log.e(TAG, "BarcodeReaderException: " + e.getMessage());
                // Save Exception
                mException = e;
                // Return Null
                return null;
            }
        }

        @Override
        protected void onPostExecute(Boolean decodeSuccessful) {
            super.onPostExecute(decodeSuccessful);

            // Dismiss Dialog
            mDecodeProgressDialog.dismiss();

            // Handle Exception
            if (decodeSuccessful == null) {
                App.showErrorDialog(getContext(),
                        getString(R.string.error_message_barcode_reader_exception,
                                mException.getMessage()));
                return;
            }

            // Update Holder Variable
            mBarcodeDecoded = decodeSuccessful;

            // Init Spinner Adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext(),
                    android.R.layout.simple_spinner_item, mDecodedBarcodes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mBarcodeSpinner.setAdapter(adapter);
        }
    }

}
