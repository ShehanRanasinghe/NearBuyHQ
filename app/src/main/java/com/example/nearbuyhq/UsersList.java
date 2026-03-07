package com.example.nearbuyhq;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UsersList extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private UsersAdapter usersAdapter;
    private List<User> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        // Hide ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);

        // Initialize sample data
        initSampleUsers();

        // Setup RecyclerView
        usersAdapter = new UsersAdapter(usersList, this::onUserClick);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(usersAdapter);
    }

    private void initSampleUsers() {
        usersList = new ArrayList<>();
        usersList.add(new User("1", "John Doe", "john@example.com", "Active"));
        usersList.add(new User("2", "Jane Smith", "jane@example.com", "Active"));
        usersList.add(new User("3", "Mike Johnson", "mike@example.com", "Suspended"));
        usersList.add(new User("4", "Sarah Wilson", "sarah@example.com", "Active"));
        usersList.add(new User("5", "Tom Brown", "tom@example.com", "Active"));
        usersList.add(new User("6", "Emma Davis", "emma@example.com", "Active"));
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

