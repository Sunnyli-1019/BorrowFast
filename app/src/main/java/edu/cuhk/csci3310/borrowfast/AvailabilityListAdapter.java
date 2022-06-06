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

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
//import me.zhanghai.android.materialratingbar.MaterialRatingBar;

/**
 * RecyclerView adapter for a list of books.
 */
public class AvailabilityListAdapter extends FirestoreAdapter<AvailabilityListAdapter.ViewHolder> {

    public interface OnBookSelectedListener {

        void onBookSelected(DocumentSnapshot book);

    }

    private OnBookSelectedListener mListener;

    public AvailabilityListAdapter(Query query) {
        super(query);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.availability_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.status)
        TextView status;

        @BindView(R.id.location)
        TextView libraryName;

        @BindView(R.id.dateAvailable)
        TextView availableDate;

        @BindView(R.id.showLocation)
        ImageButton showLocation;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot) {

            BookCopyInfo bookCopyInfo = snapshot.toObject(BookCopyInfo.class);
            libraryName.setText(bookCopyInfo.getLibraryName());
            if (bookCopyInfo.getBorrowedUID()==""){
                status.setText("Not borrowed");
                availableDate.setText("NOW");
                availableDate.setTextColor(Color.GREEN);
            }else{
                status.setText("Borrowed");
                DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
                status.setTextColor(Color.RED);
                availableDate.setText(df.format(bookCopyInfo.getDueDate().toDate()));
            }
            showLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GeoPoint location = bookCopyInfo.getLibraryLocation();
                    Log.d(TAG, String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f(%s)", location.getLatitude(), location.getLongitude(), bookCopyInfo.getLibraryName().replaceAll("\\s+", "+") ));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(Locale.ENGLISH, "geo:0,0?q=%f,%f(%s)", location.getLatitude(), location.getLongitude(), bookCopyInfo.getLibraryName().replaceAll("\\s+", "+") )));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    view.getContext().startActivity(mapIntent);
                }
            });
        }
    }

    //helper functions


}
