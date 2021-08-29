package com.example.instagramclone.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagramclone.MainActivity;
import com.example.instagramclone.Model.User;
import com.example.instagramclone.R;
import com.example.instagramclone.fragments.ProfileFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private boolean isFragment;
    private FirebaseUser firebaseUser;

    public UserAdapter(Context context, List<User> mUsers, boolean b) {
        this.mContext = context;
        this.mUsers = mUsers;
        this.isFragment = b;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User  user = mUsers.get(position);
        holder.btnfollow.setVisibility(View.VISIBLE);
        holder.username.setText(user.getUsername());
        holder.fullname.setText(user.getFullname());

        Picasso.get().load(user.getImageurl()).placeholder(R.mipmap.ic_launcher).into(holder.imageProfile);
        isFollowed(user.getId(), holder.btnfollow);

        if(user.getId().equals(firebaseUser.getUid())){
            holder.btnfollow.setVisibility(View.GONE);
        }
        holder.btnfollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.btnfollow.getText().toString().equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following").child(user.getId()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child(firebaseUser.getUid()).setValue(true);
                    addNotification(user.getId());
                }else{
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("following").child(user.getId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFragment){
                    mContext.getSharedPreferences("PROFILE", Context.MODE_PRIVATE).edit().putString("profileId", user.getId()).apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                }else{
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("publisherId", user.getId());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    private void addNotification(String id) {
        HashMap<String, Object> map = new HashMap<>();

        map.put("userid", id);
        map.put("text", "Started following you");
        map.put("postid", "");
        map.put("isPost", false);

        FirebaseDatabase.getInstance().getReference().child("Notifications").child(firebaseUser.getUid()).push().setValue(map);
    }

    private void isFollowed(final String id, Button btnfollow) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                .child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(id).exists()){
                    btnfollow.setText("following");
                }else{
                    btnfollow.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView imageProfile;
        public TextView username;
        public TextView fullname;
        public Button btnfollow;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            btnfollow = itemView.findViewById(R.id.btn_follow);
        }
    }
}
