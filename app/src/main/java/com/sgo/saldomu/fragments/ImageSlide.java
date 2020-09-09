package com.sgo.saldomu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.sgo.saldomu.R;

/*
  Created by Administrator on 8/24/2015.
 */
public class ImageSlide extends Fragment {

    private int indexImage;
    private View v;

    private final int[] mImage = {
            R.drawable.slide_image_first_screen_1,
            R.drawable.slide_image_first_screen_2,

    };

    public static ImageSlide newInstance(int index){
        ImageSlide mFrag = new ImageSlide();
        mFrag.setIndexImage(index);
        return mFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.frag_image_slide, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ImageView imgContainer = (ImageView) v.findViewById(R.id.image_container);

        imgContainer.setImageResource(mImage[getIndexImage()]);
    }

    private int getIndexImage() {
        return indexImage;
    }

    private void setIndexImage(int indexImage) {
        this.indexImage = indexImage;
    }
}