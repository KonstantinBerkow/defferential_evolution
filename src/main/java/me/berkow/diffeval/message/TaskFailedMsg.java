package me.berkow.diffeval.message;

import java.io.Serializable;


public class TaskFailedMsg implements Serializable {
    private final String msg;
    private final MainDETask task;

    public TaskFailedMsg(String msg, MainDETask task) {

        this.msg = msg;
        this.task = task;
    }

    public String getMsg() {
        return msg;
    }

    public MainDETask getTask() {
        return task;
    }

    @Override
    public String toString() {
        return "TaskFailedMsg{" +
                "msg='" + msg + '\'' +
                ", task=" + task +
                '}';
    }
}
