package me.berkow.diffeval;

/**
 * Created by konstantinberkow on 5/10/17.
 */
public class ConcreteDETask {
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
