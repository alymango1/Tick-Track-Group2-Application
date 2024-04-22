package com.group2.practicenakakainis;

public class ToDoModel {
    private String task;
    private int id, status;
    private int color = R.color.background;
    private boolean pinned;  // Add this line

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isPinned() {  // Add this method
        return pinned;
    }

    public void setPinned(boolean pinned) {  // Add this method
        this.pinned = pinned;
    }
}
