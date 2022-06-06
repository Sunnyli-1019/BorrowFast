package edu.cuhk.csci3310.borrowfast;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CategoryResultsFragment extends Fragment implements
        BookListAdapter.OnBookSelectedListener{

    @BindView(R.id.recycler_category)
    RecyclerView mCategoryRecycler;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private ListenerRegistration mRegistration;
    private BookListAdapter mAdapter;

    private static final int RC_SIGN_IN = 9001;
    private MainActivityViewModel mViewModel;

    //ArrayList<String> mFilterList = new ArrayList<>();
    private String mcategory;

    SearchView mSearchView;
    private String mSearchString;


    public CategoryResultsFragment() {
        // Required empty public constructor
    }

    public static CategoryResultsFragment newInstance(String category){
        CategoryResultsFragment fragment = new CategoryResultsFragment();
        Bundle args = new Bundle();
        args.putString("Category", category);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        mcategory = getArguments().getString("Category");
        initFirestore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category_results, container, false);
        ButterKnife.bind(this, view);
        initRecyclerView(view);
        mcategory = getArguments().getString("Category");
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
        //System.out.println("123");
        //System.out.println(mFilterList);
        //debug codes
        String uid = "";
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        mQuery = mFirestore.collection("books").whereArrayContains("category", mcategory);
                //.whereEqualTo("category", mcategory);
        //show the documents under collection "books" in debug logs to ensure that the connection is working
        mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
    private void initRecyclerView(View view) {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }
        mAdapter = new BookListAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mCategoryRecycler.setVisibility(View.GONE);
//                    mEmptyView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "no item");
                } else {
                    mCategoryRecycler.setVisibility(View.VISIBLE);
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
        Log.d(TAG, "hi");
        mCategoryRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCategoryRecycler.setAdapter(mAdapter);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onBookSelected(DocumentSnapshot book){
        // Go to the details page for the selected restaurant
//            Intent intent = new Intent(this, RestaurantDetailActivity.class);
//            intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant.getId());
//
//            startActivity(intent);
    }
}