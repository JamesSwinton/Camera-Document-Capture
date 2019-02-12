package jamesswinton.com.zebra.cttdoccapture;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import jamesswinton.com.zebra.cttdoccapture.databinding.ActivityCaptureBinding;

public class CaptureActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "CaptureActivity";

    // Constants


    // Variables
    private ActivityCaptureBinding mDataBinding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init DataBinding
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_capture);

        // Init Toolbar
        setSupportActionBar(mDataBinding.toolbar);
        getSupportActionBar().setTitle("CTT Document Capture");
        getSupportActionBar().setSubtitle("Capture, process & store documents");
    }
}
