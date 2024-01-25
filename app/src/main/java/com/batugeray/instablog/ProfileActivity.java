package com.batugeray.instablog;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.batugeray.instablog.Helper.PostsViewHolder;
import com.batugeray.instablog.Model.Blog;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity {

    private ImageView back_btn, header_image;
    private CircleImageView profile_image;
    private TextView followers_count, following_count, profile_name, error;
    private Button follow_btn;
    private RecyclerView post_list;
    private FirebaseAuth mAuth;
    private String user_id, name, photo_url;
    private FirestoreRecyclerAdapter<Blog,PostsViewHolder> adapter;
    private FloatingActionButton add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        back_btn = findViewById(R.id.back_btn);
        header_image = findViewById(R.id.header_image);
        profile_image = findViewById(R.id.profile_image);
        followers_count = findViewById(R.id.followers_count);
        following_count = findViewById(R.id.following_count);
        profile_name = findViewById(R.id.profile_name);
        follow_btn = findViewById(R.id.follow_btn);
        post_list = findViewById(R.id.post_list);
        error = findViewById(R.id.error);
        add = findViewById(R.id.add);



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mAuth = FirebaseAuth.getInstance();
        Bundle bundle = getIntent().getExtras();
        user_id = bundle.getString("UserId");
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.super.onBackPressed();
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, PostActivity.class));
            }
        });


        populateBlogList();
        post_list.setAdapter(adapter);
        post_list.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        post_list.setLayoutManager(gridLayoutManager);
        adapter.startListening();

        if (adapter.getItemCount() == 0){
            showError();
        }else {
            error.setVisibility(View.GONE);
        }


        setUserDetails();
    }

    private void populateBlogList(){

        Query query = FirebaseFirestore.getInstance()
                .collection("Posts")
                .whereEqualTo("User", user_id)
                .orderBy("Views", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Blog> options = new FirestoreRecyclerOptions.Builder<Blog>()
                .setQuery(query, Blog.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Blog, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Blog model) {

                holder.showPostDetails(model.getImage(), model.getUser(), model.getTime(), model.getTitle(), model.getDetails()
                , model.getViews(), ProfileActivity.this);

            }

            @Override
            public PostsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(ProfileActivity.this).inflate(R.layout.post_grid, parent, false);
                return new PostsViewHolder(view);
            }

            @Override
            public void onDataChanged() {
                if (adapter.getItemCount() == 0){
                    showError();
                }else {
                    error.setVisibility(View.GONE);
                }

            }
        };

    }

    private void setUserDetails(){


        if (itsMe()){

            final String user_id = mAuth.getCurrentUser().getUid();
            name = mAuth.getCurrentUser().getDisplayName();
            photo_url = mAuth.getCurrentUser().getPhotoUrl().toString();
            DocumentReference user_doc = FirebaseFirestore.getInstance().collection("Users").document(user_id);

            profile_name.setText(name);
            Glide.with(ProfileActivity.this)
                    .applyDefaultRequestOptions(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .load(photo_url)
                    .into(profile_image);

            header_image.setImageResource(R.drawable.profilebg);


            user_doc.collection("Followers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        followers_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                    }
                }
            });

            user_doc.collection("Following").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        following_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                    }
                }
            });

            follow_btn.setText("Ayarlar");
            follow_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent settings = new Intent(ProfileActivity.this, SettingsActivity.class);
                    settings.putExtra("user", user_id);
                    startActivity(settings);

                }
            });


        }else {


            final DocumentReference user_doc = FirebaseFirestore.getInstance().collection("Users").document(user_id);

            user_doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()){

                        name = task.getResult().get("name").toString();
                        photo_url = task.getResult().get("url").toString();

                        profile_name.setText(name);
                        Glide.with(ProfileActivity.this)
                                .applyDefaultRequestOptions(new RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                .load(photo_url)
                                .into(profile_image);

                        header_image.setImageResource(R.drawable.nonuserprofilebg);


                    }

                }
            });

            user_doc.collection("Followers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        followers_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                    }
                }
            });

            user_doc.collection("Following").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()){
                        following_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                    }
                }
            });

            user_doc.collection("Followers").document(mAuth.getCurrentUser().getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (documentSnapshot.exists()) {

                        follow_btn.setText("Takip Ediliyor");
                        follow_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.tick, 0);
                        follow_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                user_doc.collection("Followers").document(mAuth.getCurrentUser().getUid()).delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(ProfileActivity.this, "Bir hata meydana geldi!", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    user_doc.collection("Followers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()){
                                                                followers_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });

                                FirebaseFirestore.getInstance().collection("Users").document(mAuth.getCurrentUser().getUid()).collection("Following").document(user_id).delete();

                            }
                        });


                    } else {

                        follow_btn.setText("Takip Et");
                        follow_btn.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        follow_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Map<String, Object> follower = new HashMap<>();
                                follower.put("Name", mAuth.getCurrentUser().getDisplayName());
                                follower.put("Url", mAuth.getCurrentUser().getPhotoUrl().toString());
                                follower.put("Id", mAuth.getCurrentUser().getUid());

                                user_doc.collection("Followers").document(mAuth.getCurrentUser().getUid())
                                        .set(follower)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(ProfileActivity.this, "Bir hata meydana geldi!", Toast.LENGTH_SHORT).show();
                                                }  else{
                                                    user_doc.collection("Followers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()){
                                                                followers_count.setText(String.valueOf(task.getResult().getDocuments().size()));
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });

                                FirebaseFirestore.getInstance().collection("Users").document(mAuth.getCurrentUser().getUid()).collection("Following").document(user_id)
                                        .set(follower);
                            }
                        });

                    }

                }
            });

        }
    }

    private boolean itsMe(){
        String current_user = mAuth.getCurrentUser().getUid();

        if (current_user.equals(user_id)){
            return true;
        }else {
            return false;
        }
    }



    private void showError(){

        error.setVisibility(View.VISIBLE);
        if (itsMe()){
            error.setText("Henüz bir şey paylaşmadın!");
        }else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (name!=null){
                        error.setText(name + " daha paylaşım yapmadı!");
                    }
                }
            }, 1200);
        }
    }
}
