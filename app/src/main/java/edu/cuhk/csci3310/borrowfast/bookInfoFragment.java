package edu.cuhk.csci3310.borrowfast;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class bookInfoFragment extends Fragment
        implements RatingDialogFragment.RatingListener, CommentDialogFragment.CommentListener {

//    private AppBarConfiguration appBarConfiguration;
//    private ActivityBookInfoBinding binding;

    @BindView(R.id.cover)
    ImageView mCoverImg;

    @BindView(R.id.title)
    TextView mTitle;

    @BindView(R.id.author)
    TextView mAuthor;

    @BindView(R.id.category)
    TextView mCategory;

    @BindView(R.id.rating)
    MaterialRatingBar mRatingIndicator;

    @BindView(R.id.rating_number)
    TextView mRatingNumber;

    @BindView(R.id.description)
    ExpandableTextView mDescription;

    @BindView(R.id.recycler_locationList)
    RecyclerView mLocationsRecycler;

    @BindView(R.id.recycler_commentList)
    RecyclerView mCommentsRecycler;

    @BindView(R.id.tab)
    TabLayout mTab;

    @BindView(R.id.description_infopage)
    ConstraintLayout mDescription_layout;

    @BindView(R.id.availability)
    ConstraintLayout mAvailability_layout;

    @BindView(R.id.comment_section)
    ConstraintLayout mComment_layout;

    @BindView(R.id.add_comment)
    Button mComment_button;


    private FirebaseFirestore mFirestore;

    private DocumentReference mBookRef;
    private CollectionReference mBookCopyRef;
    private CollectionReference mCommentRef;


    private Book book;
    private List<BookCopyInfo> bookCopyInfos = new ArrayList<>();

    private Query mAvailabilityQuery;
    private Query mCommentQuery;

    private AvailabilityListAdapter mAvailabilityAdapter;
    private CommentListAdapter mCommentListAdapter;

    private RatingDialogFragment mRatingDialog;
    private CommentDialogFragment mCommentDialog;
    private double mOldRating = 0;
    private boolean mRatingChanged = false;

    public static bookInfoFragment newInstance(String docID) {
        bookInfoFragment fragment = new bookInfoFragment();
        Bundle args = new Bundle();
        args.putString("docID", docID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_book_info, container, false);
        ButterKnife.bind(this, view);
        mComment_button.setVisibility(View.INVISIBLE);

        ((AppCompatActivity)getActivity()).getSupportActionBar();
        setHasOptionsMenu(true);

//        String docID = bookInfoFragmentArgs.fromBundle(getArguments()).getDocID();
        String docID = getArguments().getString("docID");

        mFirestore = FirebaseFirestore.getInstance();
        mBookRef = mFirestore.collection("books").document(docID);
        mBookCopyRef = mBookRef.collection("bookCopyInfo");
        mCommentRef = mBookRef.collection("comments");
        mAvailabilityQuery = mBookCopyRef;
        mCommentQuery = mCommentRef;

        mBookRef.addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                book = snapshot.toObject(Book.class);
                // Load image
                String url = book.getImg();
                byte[] decode = null;
                if(isBase64Img(url)){
                    url = url.split(",")[1];
                    decode = Base64.decode(url, Base64.DEFAULT);
                }
                Glide.with(mCoverImg.getContext())
                        .load(decode==null?url:decode)
                        .into(mCoverImg);
                mTitle.setText(book.getTitle());
                mAuthor.setText(book.getAuthor());
                List<String> category = book.getCategory();
                mCategory.setText(category.get(0));
                mDescription.setText(book.getDescription());
                mRatingIndicator.setRating((float)book.getRating());
                mRatingNumber.setText(String.format("%.2f/5", book.getRating()));

            }
        });
        initRecyclerView(view);
        RatingDialogFragment.RatingListener ratingListener = this;
        final DocumentReference ratingRef = mBookRef.collection("rating")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ratingRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot rating = task.getResult();
                    if (rating.exists()) {
                        mOldRating = rating.getDouble("rating");
                        mRatingChanged = true;
                        mRatingDialog = RatingDialogFragment.newInstance(mOldRating);
                        mRatingDialog.setListener(ratingListener);
                        Log.d(TAG, "Document exists!");
                    } else {
                        Log.d(TAG, "Document does not exist!");
                        mRatingDialog = RatingDialogFragment.newInstance(0);
                        mRatingDialog.setListener(ratingListener);
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });


        CommentDialogFragment.CommentListener commentListener = this;
        mCommentDialog = new CommentDialogFragment();
        mCommentDialog.setListener(commentListener);

        mAvailability_layout.setVisibility(View.GONE);
        mComment_layout.setVisibility(View.GONE);
        mTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                int position = tab.getPosition();
                if(position==0){
                    mDescription_layout.setVisibility(View.VISIBLE);
                }else if(position==1){
                    mAvailability_layout.setVisibility(View.VISIBLE);
                }else{
                    mComment_layout.setVisibility(View.VISIBLE);
                    mComment_button.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                int position = tab.getPosition();
                if(position==0){
                    mDescription_layout.setVisibility(View.GONE);
                }else if(position==1){
                    mAvailability_layout.setVisibility(View.GONE);
                }else{
                    mComment_layout.setVisibility(View.GONE);
                    mComment_button.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initRecyclerView(View view) {
        if (mAvailabilityQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }
        mAvailabilityAdapter = new AvailabilityListAdapter(mAvailabilityQuery) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mLocationsRecycler.setVisibility(View.GONE);
//                    mEmptyView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "no item");
                } else {
                    mLocationsRecycler.setVisibility(View.VISIBLE);
//                    mEmptyView.setVisibility(View.GONE);
                    Log.d(TAG, "there is item");
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(view.findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mCommentListAdapter = new CommentListAdapter(mCommentQuery) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mCommentsRecycler.setVisibility(View.GONE);
//                    mEmptyView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "no item");
                } else {
                    mCommentsRecycler.setVisibility(View.VISIBLE);
//                    mEmptyView.setVisibility(View.GONE);
                    Log.d(TAG, "there is item");
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(view.findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mLocationsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLocationsRecycler.setAdapter(mAvailabilityAdapter);

        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCommentsRecycler.setAdapter(mCommentListAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Start listening for Firestore updates
        if (mAvailabilityAdapter != null) {
            mAvailabilityAdapter.startListening();
        }

        if (mCommentListAdapter != null) {
            mCommentListAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAvailabilityAdapter != null) {
            mAvailabilityAdapter.stopListening();
        }

        if (mCommentListAdapter != null) {
            mCommentListAdapter.stopListening();
        }
    }

    @OnClick(R.id.give_rating)
    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getActivity().getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @OnClick(R.id.add_comment)
    public void onAddCommentClicked(View view) {
        mCommentDialog.show(getActivity().getSupportFragmentManager(), CommentDialogFragment.TAG);
    }

    private Task<Void> addRating(final DocumentReference bookRef, final Rating rating) {
        // Create a reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = bookRef.collection("rating")
                .document(rating.getUid());
        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {
                Book book = transaction.get(bookRef)
                        .toObject(Book.class);
                int newNumRatings = book.getNumRatings();
                double oldRatingTotal;
                double newAvgRating = 0;
                // Compute new number of ratings
                if (mRatingChanged==false){
                    newNumRatings = book.getNumRatings() + 1;
                }else{
                    newNumRatings = book.getNumRatings();
                }
                // Compute new average rating
                oldRatingTotal = book.getRating() *
                        book.getNumRatings();
                Log.d(TAG, String.format("%f", oldRatingTotal));
                Log.d(TAG, String.format("%f", mOldRating));
                newAvgRating = (oldRatingTotal + rating.getRating()-mOldRating) /
                        newNumRatings;
                mOldRating = rating.getRating();
                // Set new restaurant info
                book.setNumRatings(newNumRatings);
                book.setRating(newAvgRating);
                // Commit to Firestore
                transaction.set(bookRef, book, SetOptions.merge());
                transaction.set(ratingRef, rating, SetOptions.merge());
                mRatingChanged = true;
                return null;
            }
        });
    }

    @Override
    public void onRating(Rating rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mBookRef, rating)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        // Hide keyboard
                        hideKeyboard();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Failed to add rating",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onComment(Comment comment) {
        // In a transaction, add the new rating and update the aggregate totals
        addComment(mBookRef, comment)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Comment added");

                        // Hide keyboard
                        hideKeyboard();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add comment failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Failed to add comment",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private Task<Void> addComment(final DocumentReference bookRef, final Comment comment) {
        // Create a reference for new rating, for use inside the transaction
        final DocumentReference commentRef = bookRef.collection("comments")
                .document();
        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {
                transaction.set(commentRef, comment, SetOptions.merge());
                return null;
            }
        });
    }



    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_book_info);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }

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