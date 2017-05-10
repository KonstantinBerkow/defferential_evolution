package me.berkow.diffeval;

import java.io.Serializable;

/**
 * Created by konstantinberkow on 5/10/17.
 */
public class ConcreteDETask implements Serializable {
    private final int id;

    public ConcreteDETask(int id) {

        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "ConcreteDETask{" +
                "id=" + id +
                '}';
    }
}
