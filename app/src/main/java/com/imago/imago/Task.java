package com.imago.imago;

/**
 * Created by werton on 12.12.17.
 */

public class Task {
    private String task;
    private int number;

    public Task(){
        task = "";
        number = 0;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
