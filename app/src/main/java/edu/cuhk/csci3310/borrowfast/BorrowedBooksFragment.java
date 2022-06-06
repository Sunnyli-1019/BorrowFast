package edu.cuhk.csci3310.borrowfast;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BorrowedBooksFragment extends Fragment implements
        BookListAdapter.OnBookSelectedListener{

    @BindView(R.id.recycler_bookList)
    RecyclerView mBooksRecycler;

    //    @BindView(R.id.view_empty)
//    ViewGroup mEmptyView;;
//
    private FirebaseFirestore mFirestore;
    private FirebaseFirestore mFirestore2;
    private FirebaseFirestore mFirestore3;
    private Query mQuery;
    private Query mQuery2;
    private Query mQuery3;

    private ListenerRegistration mRegistration;
    private BookListAdapter mAdapter;

    private static final int RC_SIGN_IN = 9001;
    private MainActivityViewModel mViewModel;

    final String[] bookDocumentID = new String[1];
    final String[] borrowTitle = new String[1];
    private String uid;

    ArrayList<String> borrowedBook = new ArrayList<String>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View model
        mViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        initFirestore();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_borrowed_books, container, false);
        ButterKnife.bind(this, view);
        ((AppCompatActivity)getActivity()).getSupportActionBar();
        setHasOptionsMenu(true);
        initRecyclerView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initFirestore() {

        mFirestore = FirebaseFirestore.getInstance();
        //debug codes
        //String uid = "";
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        mQuery = mFirestore.collection("books").whereArrayContains("borrowedUID", uid);
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

        /*mFirestore = FirebaseFirestore.getInstance();
        mFirestore2 = FirebaseFirestore.getInstance();
        mFirestore3 = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        System.out.println(uid);
        mQuery3 = mFirestore3.collection("books");
        mQuery3.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        System.out.println("11234");
                        System.out.println(document.getId());
                        bookDocumentID[0] = document.getId();
                        borrowTitle[0] = document.getString("title");


                        mQuery2 = mFirestore2.collection("books").document(bookDocumentID[0]).collection("bookCopyInfo").whereEqualTo("borrowedUID", uid);
                        mQuery2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        System.out.println("12333");
                                        //tvResult.setText(document.getString("title"));
                                        System.out.println(bookDocumentID[0]);
                                        borrowedBook.add(borrowTitle[0]);
                                        System.out.println(borrowedBook);

                                        mQuery = mFirestore.collection("books").whereIn("title", borrowedBook);
                                        mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        System.out.println("Really");
                                                        //tvResult.setText(document.getString("title"));
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

                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });*/

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_borrowed_booklist,menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:
                //create indent to the scanning page
                showScanpage();
                return true;
//            case R.id.action_signout:
//                AuthUI.getInstance().signOut(((AppCompatActivity)getActivity()));
//                startSignIn();
//                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

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
                    Log.d(TAG, "no item1");
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