package com.example.nearbuyhq.users;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nearbuyhq.R;
import com.example.nearbuyhq.core.firebase.FirebaseConfig;
import com.example.nearbuyhq.data.repository.DataCallback;
import com.example.nearbuyhq.data.repository.UserRepository;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UsersList extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private UsersAdapter usersAdapter;
    private List<User> usersList;
    private ImageView btnBack;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        btnBack = findViewById(R.id.btnBack);

        usersList = new ArrayList<>();
        userRepository = new UserRepository();

        // Setup RecyclerView
        usersAdapter = new UsersAdapter(usersList, this::onUserClick);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(usersAdapter);

        // Back button click listener
        btnBack.setOnClickListener(v -> finish());

        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void initSampleUsers() {
        usersList.clear();
        usersList.add(new User("1", "John Doe", "john@example.com", "Active"));
        usersList.add(new User("2", "Jane Smith", "jane@example.com", "Active"));
        usersList.add(new User("3", "Mike Johnson", "mike@example.com", "Suspended"));
        usersList.add(new User("4", "Sarah Wilson", "sarah@example.com", "Active"));
        usersList.add(new User("5", "Tom Brown", "tom@example.com", "Active"));
        usersList.add(new User("6", "Emma Davis", "emma@example.com", "Active"));
    }

    private void loadUsers() {
        if (!FirebaseConfig.isFirebaseEnabled()) {
            initSampleUsers();
            usersAdapter.notifyDataSetChanged();
            return;
        }

        userRepository.getUsers(new DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                usersList.clear();
                usersList.addAll(data);
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception exception) {
                Toast.makeText(UsersList.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                initSampleUsers();
                usersAdapter.notifyDataSetChanged();
            }
        });
    }

    private void onUserClick(User user) {
        Intent intent = new Intent(UsersList.this, UserDetails.class);
        intent.putExtra("user_id", user.getId());
        intent.putExtra("user_name", user.getName());
        intent.putExtra("user_email", user.getEmail());
        intent.putExtra("user_status", user.getStatus());
        startActivity(intent);
    }
}

