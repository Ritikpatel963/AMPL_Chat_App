package com.agromarket.ampl_chat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agromarket.ampl_chat.adapters.ChatAdapter;
import com.agromarket.ampl_chat.models.ChatItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ChatAdapter adapter;
    ArrayList<ChatItem> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    systemBars.bottom
            );
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();

        list.add(new ChatItem("Quiche Hollandaise", "Quisque blandit arcu quis turpis...", "15 min", 1));
        list.add(new ChatItem("Jake Weary", "Sed ligula erat, dignissim sit amet...", "32 min", 2));
        list.add(new ChatItem("Ingredia Nutrisha", "Duis eget nibh tincidunt odio...", "1 hour", 0));

        adapter = new ChatAdapter(this, list);
        recyclerView.setAdapter(adapter);
    }
}
