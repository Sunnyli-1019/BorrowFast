/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cuhk.csci3310.borrowfast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * Dialog Fragment containing rating form.
 */
public class RatingDialogFragment extends DialogFragment {

    public static final String TAG = "RatingDialog";

    private double mOldRating=0;

    @BindView(R.id.book_form_rating)
    MaterialRatingBar mRatingBar;

    @BindView(R.id.book_form_rating_title)
    TextView mRatingTitle;

    public static RatingDialogFragment newInstance(double old_rating) {
        RatingDialogFragment f = new RatingDialogFragment();

        Bundle args = new Bundle();
        args.putDouble("old_rating", old_rating);
        f.setArguments(args);

        return f;
    }

    interface RatingListener {

        void onRating(Rating rating);

    }

    private RatingListener mRatingListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOldRating = getArguments().getDouble("old_rating");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_rating, container, false);
        ButterKnife.bind(this, v);
        mRatingBar.setRating((float)mOldRating);
        if (mOldRating==0){
            mRatingTitle.setText("Modify Rating");
        }

        return v;
    }

    public void setListener(RatingListener listener){
        mRatingListener = listener;
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//
//        if (context instanceof RatingListener) {
//            mRatingListener = (RatingListener) context;
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @OnClick(R.id.book_form_button)
    public void onSubmitClicked(View view) {
        Rating rating = new Rating(
                FirebaseAuth.getInstance().getCurrentUser(),
                mRatingBar.getRating());

        if (mRatingListener != null) {
            Log.d(TAG, "test");
            mRatingListener.onRating(rating);
        }

        dismiss();
    }

    @OnClick(R.id.book_form_cancel)
    public void onCancelClicked(View view) {
        dismiss();
    }
}
