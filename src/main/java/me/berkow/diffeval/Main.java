package me.berkow.diffeval;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Arrays;

/**
 * Created by konstantinberkow on 4/26/17.
 */
public class Main {

    public static void main(String[] args) {
        System.out.printf("I am maven proj!!!! Args: %s\n", Arrays.toString(args));

        ActorSystem system = ActorSystem.create("Hello");
        ActorRef a = system.actorOf(Props.create(HelloWorld.class), "helloWorld");
        system.actorOf(Props.create(Terminator.class, a), "terminator");
    }
}
