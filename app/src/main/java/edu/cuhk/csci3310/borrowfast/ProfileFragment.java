package edu.cuhk.csci3310.borrowfast;

import static android.content.ContentValues.TAG;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private String username;
    private String useremail;
    private String userid;
    TextView mUsername;
    TextView mEmailaddress;
    TextView mPhonenum;
    Button mSignoutBtn;
    TextView mForgotpassword;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    //    private void initFirestore() {
//        mFirestore = FirebaseFirestore.getInstance();
//        //debug codes
//        String uid = "";
//        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
//            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        }
//        mQuery = mFirestore.collection("books").whereEqualTo("borrowedUID", uid);
//        //show the documents under collection "books" in debug logs to ensure that the connection is working
//        mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Log.d(TAG, document.getId() + " => " + document.getData());
//                    }
//                } else {
//                    Log.d(TAG, "Error getting documents: ", task.getException());
//                }
//            }
//        });
////        Book book = snapshot.toObject(Book.class);
////        test.setText(book.getTitle());
//    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //int id = Integer.parseInt(mPhotoId);
        //ImageView imageView = view.findViewById(R.id.imageView);
        //Uri uri = Uri.parse(mImagePathList.get(id));
        //imageView.setImageURI(uri);
        mUsername = view.findViewById(R.id.profileUsername);
        mEmailaddress = view.findViewById(R.id.profileEmail_address);
        mPhonenum = view.findViewById(R.id.profilePhone_num);
        mSignoutBtn = view.findViewById(R.id.signoutBtn);
        mForgotpassword = view.findViewById(R.id.forgotpassword);

        mFirestore = FirebaseFirestore.getInstance();
        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mQuery = mFirestore.collection("User").whereEqualTo("uid", userid);
        mQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //Log.d("Profile", document.getId() + " => " + document.getData());
                        //Log.d("Profile", document.getString("email").toString());
                        mUsername.setText(document.getString("user_name").toString());
                        mPhonenum.setText(document.getString("phone_num").toString());
                        mEmailaddress.setText(document.getString("email").toString());
                    }
                } else {
                    Log.d("Profile", "Error getting documents: ", task.getException());
                }
            }
        });

        mForgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity().getApplicationContext(), ForgotPassword.class));
            }
        });

        mSignoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance().signOut(((AppCompatActivity)getActivity()));
                startActivity(new Intent(getActivity().getApplicationContext(), Login.class));
            }
        });
    }
}