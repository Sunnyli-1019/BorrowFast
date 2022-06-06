package edu.cuhk.csci3310.borrowfast; /**
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

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;
//import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of books.
 */
public class CommentListAdapter extends FirestoreAdapter<CommentListAdapter.ViewHolder> {

    public interface OnBookSelectedListener {

        void onBookSelected(DocumentSnapshot book);

    }

    public CommentListAdapter(Query query) {
        super(query);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.comment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.username_comment)
        TextView username;

        @BindView(R.id.comment_section)
        TextView comment_text;

//        @BindView(R.id.upvote)
//        ImageButton upvote;

//        @BindView(R.id.downvote)
//        ImageButton downvote;
//
//        @BindView(R.id.downvote_count)
//        TextView downvote_count;

//        @BindView(R.id.upvote_count)
//        TextView upvote_count;


        private DocumentReference mVoteRef;
        private Query mUserRef;
        private FirebaseFirestore mFirestore;
        int mVote = -1;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
//            upvote.setColorFilter(Color.GRAY);
//            downvote.setColorFilter(Color.GRAY);
        }

        public void bind(final DocumentSnapshot snapshot) {

            Comment comment = snapshot.toObject(Comment.class);
            mFirestore = FirebaseFirestore.getInstance();
//            mUserRef = mFirestore.collection("User").whereEqualTo("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
//                    //.document(FirebaseAuth.getInstance().getCurrentUser().getUid());
//            mUserRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot user = task.getResult();
//                        if (user.exists()) {
//                            username.setText(user.getString("user_name"));
//                            Log.d(TAG, "Document exists!");
//                        } else {
//                            Log.d(TAG, "Document does not exist!");
//                        }
//                    } else {
//                        Log.d(TAG, "Failed with: ", task.getException());
//                    }
//                }
//            });
            mUserRef = mFirestore.collection("User").whereEqualTo("uid", comment.getUid());
            mUserRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            System.out.println("66123");
                            //tvResult.setText(document.getString("title"));
                            username.setText(document.getString("user_name"));
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
            comment_text.setText(comment.getComment());
//            mVoteRef = snapshot.getReference().collection("votes").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
//            mVoteRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot vote = task.getResult();
//                        if (vote.exists()) {
//                            mVote = vote.getDouble("vote").intValue();
//                            setColorThumbsButton();
//                            Log.d(TAG, "Document exists!");
//                        } else {
//                            Log.d(TAG, "Document does not exist!");
//                        }
//                    } else {
//                        Log.d(TAG, "Failed with: ", task.getException());
//                    }
//                }
//            });

//            upvote.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (mVote ==0){
//                        mVote = -1;
//                    }else{
//                        mVote = 0;
//                    }
//                    Vote vote = new Vote(FirebaseAuth.getInstance().getCurrentUser().getUid(), mVote);
//                    mFirestore.runTransaction(new Transaction.Function<Void>() {
//                        @Override
//                        public Void apply(Transaction transaction)
//                                throws FirebaseFirestoreException {
//                            Comment comment = transaction.get(snapshot.getReference())
//                                    .toObject(Comment.class);
//                            int newvote = mVote==0?1:-1;
//                            int oldNumUpvotes = comment.getNumUpvotes();
//                            int newNumUpvotes = oldNumUpvotes+newvote;
//                            comment.setNumUpvotes(newNumUpvotes);
//                            transaction.set(snapshot.getReference(), comment, SetOptions.merge());
//                            transaction.set(mVoteRef, vote, SetOptions.merge());
//                            upvote_count.setText(newNumUpvotes);
//                            return null;
//                        }
//                    });
//                    setColorThumbsButton();
//                }
//            });

//            downvote.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (mVote ==1){
//                        mVote = -1;
//                    }else{
//                        mVote = 1;
//                    }
//                    Vote vote = new Vote(FirebaseAuth.getInstance().getCurrentUser().getUid(), mVote);
//                    mFirestore.runTransaction(new Transaction.Function<Void>() {
//                        @Override
//                        public Void apply(Transaction transaction)
//                                throws FirebaseFirestoreException {
//                            Comment comment = transaction.get(snapshot.getReference())
//                                    .toObject(Comment.class);
//                            int oldNumDownvotes = comment.getnumDownvotes();
//                            int newvote = mVote==0?1:-1;
//                            int newNumDownvotes = oldNumDownvotes+newvote;
//                            comment.setnumDownvotes(newNumDownvotes);
//                            transaction.set(snapshot.getReference(), comment, SetOptions.merge());
//                            transaction.set(mVoteRef, vote, SetOptions.merge());
//                            downvote_count.setText(newNumDownvotes);
//                            return null;
//                        }
//                    });
//                    setColorThumbsButton();
//                }
//            });
        }
        //helper functions
//        public void setColorThumbsButton(){
//            if (mVote == 0){
//                upvote.setColorFilter(Color.BLACK);
//                downvote.setColorFilter(Color.GRAY);
//            }else if (mVote== 1){
//                upvote.setColorFilter(Color.GRAY);
//                downvote.setColorFilter(Color.BLACK);
//            }else{
//                upvote.setColorFilter(Color.GRAY);
//                downvote.setColorFilter(Color.GRAY);
//            }
//        }
    }
}
