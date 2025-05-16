package com.example.enhancelostandfoundapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemsListActivity extends AppCompatActivity implements ItemAdapter.OnItemClickListener {

    private RecyclerView itemsRecyclerView;
    private TextView emptyListText;
    private ItemAdapter itemAdapter;
    private DatabaseHelper databaseHelper;
    private List<Item> itemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lost & Found Items");
        }

        databaseHelper = new DatabaseHelper(this);

        itemsRecyclerView = findViewById(R.id.itemsRecyclerView);
        emptyListText = findViewById(R.id.emptyListText);

        itemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        // Get all items from database
        itemsList = databaseHelper.getAllItems();

        if (itemsList.isEmpty()) {
            emptyListText.setVisibility(View.VISIBLE);
            itemsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyListText.setVisibility(View.GONE);
            itemsRecyclerView.setVisibility(View.VISIBLE);

            itemAdapter = new ItemAdapter(this, itemsList);
            itemAdapter.setOnItemClickListener(this);
            itemsRecyclerView.setAdapter(itemAdapter);
        }
    }

    @Override
    public void onItemClick(Item item) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("item_id", item.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}