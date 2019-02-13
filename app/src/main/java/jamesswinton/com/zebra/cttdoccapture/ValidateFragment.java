package jamesswinton.com.zebra.cttdoccapture;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.dynamsoft.barcode.BarcodeReader;
import com.dynamsoft.barcode.BarcodeReaderException;
import com.dynamsoft.barcode.TextResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jamesswinton.com.zebra.cttdoccapture.databinding.FragmentValidateBinding;

import static jamesswinton.com.zebra.cttdoccapture.App.PERM_IMAGE_DIRECTORY_FILE_PATH;
import static jamesswinton.com.zebra.cttdoccapture.App.TEMP_IMAGE_PATH_ARG;

public class ValidateFragment extends Fragment {

    // Debugging
    private static final String TAG = "ValidateFragment";

    // Constants


    // Variables
    private static String mSelectedBarcode = null;
    private static BarcodeReader mBarcodeReader = null;
    private static List<String> mDecodedBarcodes = new ArrayList<>();

    private FragmentValidateBinding mDataBinding = null;

    public ValidateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create View
        View fragmentView = inflater.inflate(R.layout.fragment_validate, container,
                false);

        // Init DataBinding
        mDataBinding = DataBindingUtil.bind(fragmentView);

        // Return View
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getImagePath() != null) {
            // Load Image
            loadImage(getImagePath());
            // Attempt Decode
            processImage();
            // Set Click Listeners
            mDataBinding.saveImage.setOnClickListener(view -> saveImage());
            mDataBinding.deleteImage.setOnClickListener(view -> deleteImage());
        } else {
            App.showErrorDialog(getContext(), getString(R.string.error_message_no_image_path));
        }
    }

    private void saveImage() {
        // Get Temporary File
        File tempImage = new File(getImagePath());
        // Check File Exists
        if (tempImage.exists()) {
            // Create New File
            File permImage = new File(PERM_IMAGE_DIRECTORY_FILE_PATH
                    + File.separator
                    + mDataBinding.barcodeSpinner.getSelectedItem().toString()
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

    private void processImage() {
        try {
            // Clear Existing Barcodes
            mDecodedBarcodes = new ArrayList<>();
            // Init Barcode Reader
            if (mBarcodeReader == null) {
                mBarcodeReader = new BarcodeReader(getString(R.string.dynamsoft_license));
            }
            // Attempt Decode
            TextResult[] decodeResults = mBarcodeReader.decodeFile(getImagePath(), "");
            // Loop results
            if (decodeResults != null && decodeResults.length > 0) {
                // Add Results to Array
                for (TextResult barcode : decodeResults) {
                    mDecodedBarcodes.add(barcode.barcodeText);
                }
                // Enable Save Button
                mDataBinding.saveImage.setEnabled(true);
            } else {
                // Show Error
                App.showErrorDialog(getContext(), getString(R.string.error_message_no_image_decode));
                // Disable Save
                mDataBinding.saveImage.setEnabled(false);
                // Set Selected Barcode to Generic String
                mSelectedBarcode = getString(R.string.error_message_no_image_decode);
                // Add Generic string to List
                mDecodedBarcodes.add(mSelectedBarcode);
            }
            // Init Spinner Adapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    getContext() == null ? App.mContext : getContext(),
                    android.R.layout.simple_spinner_item, mDecodedBarcodes);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Set Adapter on Spinner
            mDataBinding.barcodeSpinner.setAdapter(adapter);
        } catch (BarcodeReaderException e) {
            Log.e(TAG, "BarcodeReaderException: " + e.getMessage());
            App.showErrorDialog(getContext(),
                    getString(R.string.error_message_barcode_reader_exception,
                            e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            App.showErrorDialog(getContext(),
                    getString(R.string.error_message_generic_exception,
                            e.getMessage()));
        }
    }

    private void loadImage(String imagePath) {
        // Get Non-null Context
        Context cx = (getContext() == null ? App.mContext : getContext());
        // Create Circular Progress Drawable
        CircularProgressDrawable loadingImage = new CircularProgressDrawable(cx);
        loadingImage.setStrokeWidth(5f);
        loadingImage.setCenterRadius(30f);
        loadingImage.start();
        // Load Image
        GlideApp.with(this)
                .load(imagePath)
                .placeholder(loadingImage)
                .into(mDataBinding.imagePreview);
    }

    private String getImagePath() {
        return getArguments() == null ? null : getArguments().getString(TEMP_IMAGE_PATH_ARG);
    }

}
