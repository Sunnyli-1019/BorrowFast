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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.ContentValues.TAG;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import edu.cuhk.csci3310.borrowfast.Book;
import edu.cuhk.csci3310.borrowfast.FirestoreAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.cuhk.csci3310.borrowfast.R;
//import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of books.
 */
public class BookListAdapter extends FirestoreAdapter<BookListAdapter.ViewHolder> {

    public interface OnBookSelectedListener {

        void onBookSelected(DocumentSnapshot book);

    }

    private OnBookSelectedListener mListener;

    public BookListAdapter(Query query, OnBookSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_book, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.book_item_image)
        ImageView imageView;

        @BindView(R.id.book_item_name)
        TextView nameView;

        @BindView(R.id.book_item_publisher)
        TextView publisher;

        @BindView(R.id.book_item_year)
        TextView year;

        @BindView(R.id.book_item_author)
        TextView authorView;

        @BindView(R.id.book_item_dueDate)
        TextView dueDateView;

        @BindView(R.id.expanded_view)
        RelativeLayout expanded_view;

        @BindView(R.id.book_renew)
        Button book_renew;

        @BindView(R.id.book_return)
        Button book_return;

        @BindView(R.id.book_info)
        Button book_info;

        private FirebaseFirestore mFirestore;
        private FirebaseFirestore mFirestore2;
        private FirebaseFirestore mFirestore3;
        private String uid;
        private Query mQuery;
        private Query mQuery2;
        final String[] bookDocumentID = new String[1];
        final String[] bookCopyID = new String[1];
//        @BindView(R.id.book_item_rating)
//        MaterialRatingBar ratingBar;
//
//        @BindView(R.id.book_item_num_ratings)
//        TextView numRatingsView;
//
//        @BindView(R.id.book_item_price)
//        TextView priceView;
//
//        @BindView(R.id.book_item_category)
//        TextView categoryView;
//
//        @BindView(R.id.book_item_city)
//        TextView cityView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnBookSelectedListener listener) {

            Book book = snapshot.toObject(Book.class);
            //BookCopyInfo bookCopyInfo = snapshot.toObject(BookCopyInfo.class);
            Resources resources = itemView.getResources();

            // Load image
            String url = book.getImg();
            Log.d(TAG, url);
            byte[] decode = null;
            if(isBase64Img(url)){
                url = url.split(",")[1];
                Log.d(TAG, url);
                decode = Base64.decode(url, Base64.DEFAULT);
            }

            Glide.with(imageView.getContext())
                    .load(decode==null?url:decode)
                    .into(imageView);
            Log.d(TAG, book.getTitle());

            nameView.setText("Title: " + book.getTitle());
            publisher.setText("Publisher: " + book.getPublisher());
            year.setText("Year of Publish: " + book.getYearOfPublish());
            authorView.setText("Author:" + book.getAuthor());
            //System.out.println("Timestamp" + book.getDueDate());

            //DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            //dueDateView.setText(df.format(bookCopyInfo.getDueDate().toDate()));

//            ratingBar.setRating((float) book.getAvgRating());
//            cityView.setText(book.getCity());
//            categoryView.setText(book.getCategory());
//            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
//                    book.getNumRatings()));
//            priceView.setText(bookUtil.getPriceString(book));

            // Click listener
            System.out.println("yes");
            mFirestore = FirebaseFirestore.getInstance();
            mFirestore2 = FirebaseFirestore.getInstance();
            mFirestore3 = FirebaseFirestore.getInstance();
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            System.out.println(uid);
            mQuery = mFirestore.collection("books").whereEqualTo("title", book.getTitle());
            mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {

                            System.out.println("234");
                            System.out.println(document.getId());
                            bookDocumentID[0] = document.getId();

                            mQuery2 = mFirestore2.collection("books").document(bookDocumentID[0]).collection("bookCopyInfo").whereEqualTo("borrowedUID", uid);
                            mQuery2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            System.out.println("123");
                                            //tvResult.setText(document.getString("title"));
                                            bookCopyID[0] = document.getId();
                                            System.out.println(bookCopyID[0]);
                                            System.out.println(document.getLong("renew_time"));
                                            int barodeNo = document.getLong("barcode").intValue();

                                            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                                            dueDateView.setVisibility(View.VISIBLE);
                                            dueDateView.setText("Due Date: " + df.format(document.getDate("dueDate")));
                                            System.out.println(Calendar.getInstance().getTime());
                                            int a = document.getDate("dueDate").compareTo(Calendar.getInstance().getTime());
                                            if(a < 0){
                                                dueDateView.setTextColor(Color.RED);
                                                book_renew.setVisibility(View.INVISIBLE);
                                            }

                                            book_renew.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    final int[] copyTime = {document.getLong("renew_time").intValue()};
                                                    Date dueDate = Date.from(LocalDateTime.now().plusDays(14).atZone(ZoneId.systemDefault()).toInstant());
                                                    System.out.println(copyTime[0]);
                                                    if (copyTime[0] < 5) {
                                                        mFirestore3.collection("books").document(bookDocumentID[0]).collection("bookCopyInfo").document(bookCopyID[0]).update("renew_time", FieldValue.increment(1), "dueDate", dueDate)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {
                                                                        Toast.makeText(book_return.getContext(), "Renew Success", Toast.LENGTH_SHORT).show();
                                                                        dueDateView.setText("Due Date: " + df.format(dueDate));
                                                                        Log.v("LOGMessage2", "ChangeSuccess");

                                                                        //Toast.makeText(this, "do not have the permission", Toast.LENGTH_SHORT).show();

                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {

                                                                        Log.v("LOGMessage", "ChangeFail");

                                                                    }
                                                                });

                                                    } else {
                                                        Toast.makeText(book_return.getContext(), "Renew Fail", Toast.LENGTH_SHORT).show();
                                                        Log.v("LOGMessage", "ChangeFail");
                                                    }
                                                }
                                            });
                                            book_return.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
                                                    final View barcodeView = LayoutInflater.from(view.getContext()).inflate(R.layout.image_layout, null);
                                                    final ImageView dialogImageView = (ImageView) barcodeView.findViewById(R.id.barcode_image);
                                                    int[] images = {R.drawable.barcode_0, R.drawable.barcode_1, R.drawable.barcode_2, R.drawable.barcode_3, R.drawable.barcode_4, R.drawable.barcode_5, R.drawable.barcode_6};
                                                    dialogImageView.setImageResource(images[barodeNo]);
                                                    //dialog.setTitle("Return");
                                                    dialog.setView(barcodeView);

                                                    dialog.show();
                                                }
                                            });


                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });

                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });

            book_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.main_content,bookInfoFragment.newInstance(snapshot.getId())).addToBackStack(null).commit();
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onBookSelected(snapshot);
                        int pos = getLayoutPosition();
                        //Navigation.findNavController(view).navigate(BorrowedBooksFragmentDirections.showBookInfo(snapshot.getId()));
                        if (expanded_view.getVisibility() == View.VISIBLE) {
                            expanded_view.setVisibility(View.GONE);
                        } else {
                            expanded_view.setVisibility(View.VISIBLE);
                        }
//                        Navigation.findNavController(view).navigate(BorrowedBooksFragmentDirections.showBookInfo(snapshot.getId()));
//                        ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.main_content,bookInfoFragment.newInstance(snapshot.getId())).addToBackStack(null).commit();
                    }
                }
            });
        }

    }

    //helper functions

    //check if the img is in base64 format
    public static boolean isBase64Img(String imgurl){
        if(!TextUtils.isEmpty(imgurl)&&(imgurl.startsWith("data:image/png;base64,")
                ||imgurl.startsWith("data:image/*;base64,")||imgurl.startsWith("data:image/jpg;base64,")||imgurl.startsWith("data:image/jpeg;base64,")
        ))
        {
            return true;
        }
        return false;
    }

}
