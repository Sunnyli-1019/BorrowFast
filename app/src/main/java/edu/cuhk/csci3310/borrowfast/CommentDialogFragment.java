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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog Fragment containing rating form.
 */
public class CommentDialogFragment extends DialogFragment {

    public static final String TAG = "CommentDialog";

    @BindView(R.id.comment_form_text)
    EditText mCommentText;

    interface CommentListener {

        void onComment(Comment comment);

    }

    private CommentListener mCommentListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_comment, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CommentListener) {
            mCommentListener = (CommentListener) context;
        }
    }

    public void setListener(CommentListener listener){
        mCommentListener = listener;
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @OnClick(R.id.comment_form_button)
    public void onSubmitClicked(View view) {
        Comment comment = new Comment(
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                mCommentText.getText().toString(),0,0);

        if (mCommentListener != null) {
            mCommentListener.onComment(comment);
        }

        dismiss();
    }

    @OnClick(R.id.comment_form_cancel)
    public void onCancelClicked(View view) {
        dismiss();
    }
}
