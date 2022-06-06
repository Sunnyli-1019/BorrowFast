package edu.cuhk.csci3310.borrowfast;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;
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

public class SearchResultsFragment extends Fragment implements
        BookListAdapter.OnBookSelectedListener{

    @BindView(R.id.recycler_search)
    RecyclerView mBooksRecycler;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private ListenerRegistration mRegistration;
    private BookListAdapter mAdapter;

    private static final int RC_SIGN_IN = 9001;
    private MainActivityViewModel mViewModel;

    ArrayList<String> mFilterList = new ArrayList<>();

    SearchView mSearchView;
    private String mSearchString;

    public static SearchResultsFragment newInstance(ArrayList<String> FilterList) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("FilterList", FilterList);
        System.out.println("FilterList");
        System.out.println(FilterList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View model
        mViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        mFilterList = getArguments().getStringArrayList("FilterList");
        System.out.println("mFilterList2");
        System.out.println(mFilterList);
        initFirestore();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        initRecyclerView(view);
//        mKeyword =  SearchResultsFragmentArgs.fromBundle(getArguments()).getKeyword();
        mFilterList = getArguments().getStringArrayList("FilterList");
        System.out.println("mFilterList1");
        System.out.println(mFilterList);
        ActionBar toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initFirestore() {

        mFirestore = FirebaseFirestore.getInstance();
        System.out.println("123");
        System.out.println(mFilterList);
        //debug codes
        String uid = "";
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        if (mFilterList.isEmpty()){
            Toast.makeText(getActivity(),"No such result", Toast.LENGTH_SHORT).show();
        }
        else{
            mQuery = mFirestore.collection("books").whereIn("title", mFilterList);
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
                    mBooksRecycler.setVisibility(View.GONE);
//                    mEmptyView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "no item");
                } else {
                    mBooksRecycler.setVisibility(View.VISIBLE);
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
        mBooksRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBooksRecycler.setAdapter(mAdapter);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_back,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                //create indent to the scanning page
                getActivity().onBackPressed();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

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

    private void startSignIn() {
        // Sign in with FirebaseUI
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(ImmutableList.of(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
        mViewModel.setIsSigningIn(true);
    }


    private void showScanpage() {
        Intent intent = new Intent(getActivity(), ScannerPage.class);
        startActivity(intent);
    }

}