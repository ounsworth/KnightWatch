package com.loyola.robotics.knightwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.List;

/**
 * Created by cnnr2 on 2015-10-31.
 */
public class SecondaryTab extends Fragment {
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.secondary_fragment_tab,null);


    }



    }
