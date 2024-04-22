package com.group2.practicenakakainis;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.group2.practicenakakainis.Utils.DataBaseHelper;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ListView listView;
    private final List<ToDoModel> taskList = new ArrayList<>();
    private TaskAdapter adapter;
    private DataBaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        listView = view.findViewById(R.id.folder_list);
        db = new DataBaseHelper(getContext());

        adapter = new TaskAdapter(getContext(), taskList);
        listView.setAdapter(adapter);

        loadTasks(); // Load tasks from database

        return view;
    }

    public ListView getListView() {
        return listView;
    }

    public List<ToDoModel> getTaskList() {
        return taskList;
    }

    public void loadTasks() {
        taskList.clear(); // Clear the existing list
        taskList.addAll(db.getAllTasks()); // Fetch tasks from database
        adapter.notifyDataSetChanged(); // Notify adapter about the data change
    }
}
