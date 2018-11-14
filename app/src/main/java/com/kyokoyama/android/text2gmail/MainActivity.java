package com.kyokoyama.android.text2gmail;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.kyokoyama.android.text2gmail.fragment.EmailConfigFragment;
import com.kyokoyama.android.text2gmail.fragment.MessageLogFragment;
import com.kyokoyama.android.text2gmail.fragment.SettingsFragment;
import com.kyokoyama.android.text2gmail.util.Constants;
import com.kyokoyama.android.text2gmail.util.Util;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final int RC_CONTACT_STARTUP_PERMISSION_GRANTED = 50;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        if (!Util.checkPermission(this, Constants.PERMISSIONS_CONTACTS)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog dialog = builder.setTitle("Contacts Permission")
                        .setMessage(getResources().getString(R.string.contacts_permission_dialog_message))
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setPositiveButton("OK", (dialogInterface, i) -> ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS}, RC_CONTACT_STARTUP_PERMISSION_GRANTED))
                        .create();
                dialog.setOnShowListener(dialogInterface -> {
                    Button buttonNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    Button buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    buttonNegative.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
                    buttonPositive.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
                });
                dialog.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS}, RC_CONTACT_STARTUP_PERMISSION_GRANTED);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RC_CONTACT_STARTUP_PERMISSION_GRANTED:
                boolean permissionGranted = true;
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }

                if (permissionGranted) {
                    Log.d(TAG,"Permission granted!");
                } else {
                    Log.d(TAG,"Permission denied!");
                }
                return;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new EmailConfigFragment();
                case 1:
                    return new MessageLogFragment();
                case 2:
                    return new SettingsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}