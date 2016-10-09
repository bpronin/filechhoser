package com.bopr.filechhoser_project;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.bopr.filechooser.FileChooserFragment;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OPEN_FILE = 100;
    private EditText text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (EditText) findViewById(R.id.text_file);
    }

//    public void onButtonClick(View view) {
//        Intent intent = new FileChooserIntent(this);
//        intent.putExtra(FileChooserIntent.EXTRA_FILTER, "");
//        intent.putExtra(FileChooserIntent.EXTRA_ALLOW_CREATE_FILE, false);
//        intent.putExtra(FileChooserIntent.EXTRA_ALLOW_FILE_OPERATIONS, false);
//        intent.putExtra(FileChooserIntent.EXTRA_FILE_NAME, text.getText().toString());
//        startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case REQUEST_CODE_OPEN_FILE:
//                if (resultCode == Activity.RESULT_OK) {
//                    text.setText(data.getStringExtra(FileChooserIntent.EXTRA_FILE_NAME));
//                }
//                break;
//        }
//    }

    public void onButtonClick(View view) {
        FileChooserFragment fragment = FileChooserFragment.newInstance(true, text.getText().toString(), true, null);
        fragment.setOnDismissListener(new FileChooserFragment.OnDismissListener() {

            @Override
            public void onDismiss(String selectedFileName, boolean cancelled) {
                text.setText(selectedFileName);
            }
        });

        fragment.show(getSupportFragmentManager(), "file-chooser");
    }

}
