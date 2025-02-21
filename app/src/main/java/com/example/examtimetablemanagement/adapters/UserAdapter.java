package com.example.examtimetablemanagement.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.examtimetablemanagement.fragments.UserInfoFragment;

import com.bumptech.glide.Glide;
import com.example.examtimetablemanagement.R;
import com.example.examtimetablemanagement.models.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;
    private List<User> filteredList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(Context context, OnUserClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.userList = new ArrayList<>();
        this.filteredList = new ArrayList<>();
    }

    public void setUsers(List<User> users) {
        this.userList = users;
        this.filteredList = new ArrayList<>(users);
        notifyDataSetChanged();
    }

    public void filterByRole(String role) {
        filteredList.clear();
        if (role.equals("All")) {
            filteredList.addAll(userList);
        } else {
            for (User user : userList) {
                if (user.getUserRole().equalsIgnoreCase(role)) {
                    filteredList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = filteredList.get(position);
        
        holder.nameTextView.setText(user.getName());
        holder.emailTextView.setText(user.getEmail());
        holder.roleTextView.setText(user.getUserRole());

        Glide.with(context)
                .load(user.getImage())
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(holder.profileImageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);



                Fragment fragment = UserInfoFragment.newInstance(user.getUsername());
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            showPopupMenu(v, user);
            return true;
        });
    }

    private void showPopupMenu(View view, User user) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.user_options_menu);
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_remove) {
                removeUser(user);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void removeUser(User user) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUsername());
        userRef.removeValue().addOnSuccessListener(aVoid -> {
            int position = filteredList.indexOf(user);
            if (position != -1) {
                userList.remove(user);
                filteredList.remove(user);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        TextView emailTextView;
        TextView roleTextView;
        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            roleTextView = itemView.findViewById(R.id.roleTextView);
        }
    }
}