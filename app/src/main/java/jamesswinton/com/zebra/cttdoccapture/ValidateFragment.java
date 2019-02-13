package jamesswinton.com.zebra.cttdoccapture;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dynamsoft.barcode.BarcodeReader;

import jamesswinton.com.zebra.cttdoccapture.databinding.FragmentValidateBinding;

public class ValidateFragment extends Fragment {

    // Debugging
    private static final String TAG = "ValidateFragment";

    // Constants


    // Variables
    private static BarcodeReader mBarcodeReader = null;

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

}
