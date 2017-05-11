package me.berkow.diffeval;

/**
 * Created by konstantinberkow on 5/10/17.
 */
public class Main {

    public static void main(String[] args) {
        DEBackendMain.main(new String[]{"192.168.0.108", "2552"});
        DEBackendMain.main(new String[]{"192.168.0.108", "2553"});
        DEFrontendMain.main(new String[]{"2551"});
    }
}
