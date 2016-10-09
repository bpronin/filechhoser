package com.bopr.filechooser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import static com.bopr.filechooser.FileChooserIntent.*;

public class FileChooserActivity extends AppCompatActivity {

    private FileChooserFragment fragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_chooser);

        fragment = FileChooserFragment.newInstance(
                getIntent().getBooleanExtra(EXTRA_ALLOW_FILE_OPERATIONS, false),
                getIntent().getStringExtra(EXTRA_FILE_NAME),
                true,
                getIntent().getStringArrayExtra(EXTRA_FILTER)
        );

        fragment.setOnDismissListener(new FileChooserFragment.OnDismissListener() {

            @Override
            public void onDismiss(String selectedFileName, boolean cancelled) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_FILE_NAME, selectedFileName);
                setResult(cancelled ? Activity.RESULT_CANCELED : Activity.RESULT_OK, intent);
                finish();
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            fragment.dismiss();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}
