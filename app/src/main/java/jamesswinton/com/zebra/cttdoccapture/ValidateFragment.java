package jamesswinton.com.zebra.cttdoccapture;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ValidateFragment extends Fragment {

    // Debugging
    private static final String TAG = "ValidateFragment";

    // Constants


    // Variables


    public ValidateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Create View
        View fragmentView = inflater.inflate(R.layout.fragment_validate, container,
                false);



        // Return View
        return fragmentView;
    }

}
