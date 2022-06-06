package edu.cuhk.csci3310.borrowfast;

import static android.content.ContentValues.TAG;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchBookListAdapter extends FirestoreAdapter<SearchBookListAdapter.ViewHolder>{


    public interface OnBookSelectedListener {
        void onBookSelected(DocumentSnapshot book);
    }

    private OnBookSelectedListener mListener;

    public SearchBookListAdapter(Query query, OnBookSelectedListener listener){
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_searched, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.searchedbook_image)
        ImageView imageView;

        @BindView(R.id.searchedbook_name)
        TextView nameView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnBookSelectedListener listener) {

            Book book = snapshot.toObject(Book.class);
            Resources resources = itemView.getResources();

            // Load image
            String url = book.getImg();
            Log.d(TAG, url);
            byte[] decode = null;
            if (isBase64Img(url)) {
                url = url.split(",")[1];
                Log.d(TAG, url);
                decode = Base64.decode(url, Base64.DEFAULT);
            }

            Glide.with(imageView.getContext())
                    .load(decode == null ? url : decode)
                    .into(imageView);
            Log.d(TAG, book.getTitle());

            nameView.setText(book.getTitle());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onBookSelected(snapshot);
                    }
                }
            });
        }
        public static boolean isBase64Img(String imgurl) {
            if (!TextUtils.isEmpty(imgurl) && (imgurl.startsWith("data:image/png;base64,")
                    || imgurl.startsWith("data:image/*;base64,") || imgurl.startsWith("data:image/jpg;base64,") || imgurl.startsWith("data:image/jpeg;base64,")
            )) {
                return true;
            }
            return false;
        }
    }
}
