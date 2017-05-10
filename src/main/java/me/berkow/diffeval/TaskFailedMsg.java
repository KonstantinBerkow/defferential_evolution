package me.berkow.diffeval;

import java.io.Serializable;

/**
 * Created by konstantinberkow on 5/10/17.
 */
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
