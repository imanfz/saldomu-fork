package com.sgo.saldomu.coreclass;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sgo.saldomu.R;

/**
 * Created by Yessica on 12/04/17.
 */

public class GlideManager {
    private static GlideManager INTERFACEMANAGER = null;

    public static GlideManager sharedInstance() {
        if (INTERFACEMANAGER == null) {
            INTERFACEMANAGER = new GlideManager();

        }
        return INTERFACEMANAGER;
    }

    public GlideManager() {

    }

    public void initializeGlide(Context context, Object source, Drawable errorDrawable, ImageView target){
        Glide.with(context)
                .load(source)
                .centerCrop()
                .fitCenter()
                .error(errorDrawable)
                .transform(new CircleTransform(context))
                .placeholder(R.drawable.progress_animation)
                .into(target);
    }

    public void initializeGlidePromo(Context context, Object source, Drawable errorDrawable, ImageView target){
        Glide.with(context)
                .load(source)
                .centerCrop()
                .fitCenter()
                .error(errorDrawable)
                .placeholder(R.drawable.progress_animation)
                .into(target);
    }

    public void initializeGlideProfile(Context context, Object source, ImageView target){
        Glide.with(context)
                .load(source)
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                .into(target);
    }

//    public void initializeGlide(Context context, String source, Drawable errorDrawable, ImageView target){
//        Glide.with(context)
//                .load(source)
//                .centerCrop()
//                .error(errorDrawable)
//                .transform(new CircleTransform(context))
//                .placeholder(R.drawable.progress_animation)
//                .into(target);
//    }
//    public void initializeGlide(Context context, Integer source, Drawable errorDrawable, ImageView target){
//        Glide.with(context)
//                .load(source)
//                .centerCrop()
//                .error(errorDrawable)
//                .transform(new CircleTransform(context))
//                .placeholder(R.drawable.progress_animation)
//                .into(target);
//    }
//    public void initializeGlide(Context context, Uri source, Drawable errorDrawable, ImageView target){
//        Glide.with(context)
//                .load(source)
//                .centerCrop()
//                .error(errorDrawable)
//                .transform(new CircleTransform(context))
//                .placeholder(R.drawable.progress_animation)
//                .into(target);
//    }

}
