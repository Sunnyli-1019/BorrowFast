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

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryBookListAdapter extends FirestoreAdapter<CategoryBookListAdapter.ViewHolder>{

    public interface OnBookSelectedListener {
        void onBookSelected(DocumentSnapshot book);
    }

    private OnBookSelectedListener mListener;

    public CategoryBookListAdapter(Query query, OnBookSelectedListener listener){
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_category, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.category_image)
        ImageView imageView;

        @BindView(R.id.category_name)
        TextView nameView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
        public void bind(final DocumentSnapshot snapshot,
                         final OnBookSelectedListener listener) {

            System.out.println(snapshot);
            Category book_category = snapshot.toObject(Category.class);
            Resources resources = itemView.getResources();
            //ArrayList<String> categoryList = new ArrayList<>();
            // Load image
            String url = book_category.getImg();
            //Log.d(TAG, url);
            System.out.println("The categorybooklist adapter url is "+url);
            byte[] decode = null;
            if (isBase64Img(url)) {
                url = url.split(",")[1];
                Log.d(TAG, url);
                decode = Base64.decode(url, Base64.DEFAULT);
            }

            Glide.with(imageView.getContext())
                    .load(decode == null ? url : decode)
                    .into(imageView);
            Log.d(TAG, book_category.getCategory());

            //categoryList.add(book_category.getCategory().toString().toLowerCase());
            nameView.setText(book_category.getCategory());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onBookSelected(snapshot);
                        ((FragmentActivity)view.getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.main_content,CategoryResultsFragment.newInstance(book_category.getCategory())).addToBackStack(null).commit();
                    }
                }
            });
        }
        //check if the img is in base64 format
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
