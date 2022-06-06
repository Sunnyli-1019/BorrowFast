package edu.cuhk.csci3310.borrowfast;

import static android.content.ContentValues.TAG;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.ImmutableList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements FeaturedBookListAdapter.OnBookSelectedListener,
        CategoryBookListAdapter.OnBookSelectedListener {

    @BindView(R.id.recycler_featured)
    RecyclerView mFeaturedBooksRecycler;

    @BindView(R.id.recycler_category)
    RecyclerView mCategoryBooksRecycler;

    EditText mSearchbar;
    Button mSearchBtn;

    SearchView mSearchView;
    private String mSearchString;

    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private FirebaseFirestore mFirestore2;
    private Query mQuery2;
    private FirebaseFirestore mFirestore3;
    private Query mQuery3;
    private Query mQueryCategory;

    private FeaturedBookListAdapter mAdapter;
    private CategoryBookListAdapter mAdapter2;

    private List<ExampleItem> BookList = new ArrayList<>();
    private List<ExampleItem> BookListFull = new ArrayList<>();

    private static final int RC_SIGN_IN = 9001;
    private MainActivityViewModel mViewModel;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    //private RecyclerView mRecyclerView;



    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        initFirestore();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);
        initRecyclerView(view);
        ((AppCompatActivity)getActivity()).getSupportActionBar();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        mSearchbar = view.findViewById(R.id.searchbar);
//        mSearchBtn = view.findViewById(R.id.searchBtn);

    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
        mFirestore2 = FirebaseFirestore.getInstance();
        //debug codes
        String uid = "";
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        mQuery = mFirestore.collection("books").whereGreaterThan("rating", 3);
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

        BookList.clear();

        mQuery2 = mFirestore2.collection("books");
        //show the documents under collection "books" in debug logs to ensure that the connection is working
        mQuery2.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        System.out.println("bookList");

                        BookList.add(new ExampleItem(document.getString("title")));


                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

        mQueryCategory = mFirestore.collection("category");
    }

    private void initRecyclerView(View view) {
        if (mQuery == null && mQueryCategory == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }
        mAdapter2 = new CategoryBookListAdapter(mQueryCategory, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mCategoryBooksRecycler.setVisibility(View.GONE);
//                    mEmptyView.setVisibility(View.VISIBLE);
                    Log.d("adapter2", "no item");
                } else {
                    mCategoryBooksRecycler.setVisibility(View.VISIBLE);
//                    mEmptyView.setVisibility(View.GONE);
                    Log.d("adapter2", "there is item");
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(view.findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };
        mAdapter = new FeaturedBookListAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mFeaturedBooksRecycler.setVisibility(View.GONE);
//                    mEmptyView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "no item");
                } else {
                    mFeaturedBooksRecycler.setVisibility(View.VISIBLE);
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

        //Log.d(TAG, "hi");
        mFeaturedBooksRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));
        mFeaturedBooksRecycler.setAdapter(mAdapter);
        mCategoryBooksRecycler.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mCategoryBooksRecycler.setAdapter(mAdapter2);
    }
    @Override
    public void onStart() {
        super.onStart();
        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
        if (mAdapter2 != null){
            mAdapter2.startListening();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
        if (mAdapter2 != null){
            mAdapter2.stopListening();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        menu.clear();
        inflater.inflate(R.menu.menu_searchbar,menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        mSearchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        mSearchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));
        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener()
        {
            final Toast toast = new Toast(getActivity());
            public boolean onQueryTextChange(String newText)
            {
                mSearchString = newText;
                //doFilterAsync(mSearchString);
//                toast.makeText(getActivity(), "Test1", Toast.LENGTH_LONG).show();
                return true;
            }

            public boolean onQueryTextSubmit(String query)
            {
                mSearchString = query;
                System.out.println(BookList);

                ArrayList<String> FilterList = new ArrayList<>();
                String filterPattern = query.toString().toLowerCase().trim();

                for(ExampleItem item : BookList){
                    if(item.getTitle1().toLowerCase().contains(filterPattern)){
                        FilterList.add(item.getTitle1());
                    }
                }

                /*mFirestore3 = FirebaseFirestore.getInstance();
                mQuery3 = mFirestore3.collection("books").whereIn("title", FilterList);
                //show the documents under collection "books" in debug logs to ensure that the connection is working
                mQuery3.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                System.out.println(document.getString("title"));
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });*/




//                Navigation.findNavController(getView()).navigate(HomeFragmentDirections.showSearchResults(mSearchString));
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_content,SearchResultsFragment.newInstance(FilterList)).addToBackStack(null).commit();
                //doFilterAsync(mSearchString);
//                toast.makeText(getActivity(), "Test2", Toast.LENGTH_LONG).show();

                return true;
            }
        };
        mSearchView.setOnQueryTextListener(queryTextListener);
        super.onCreateOptionsMenu(menu, inflater);

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