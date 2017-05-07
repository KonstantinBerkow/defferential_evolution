package me.berkow.diffeval;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.japi.pf.FI;

/**
 * Created by konstantinberkow on 5/7/17.
 */
public class Terminator extends AbstractLoggingActor {

    private final ActorRef ref;

    public Terminator(ActorRef ref) {
        this.ref = ref;
        getContext().watch(ref);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Terminated.class, new FI.UnitApply<Terminated>() {
                    public void apply(Terminated t) throws Exception {
                        log().info("{} has terminated, shutting down system", ref.path());
                        getContext().system().terminate();
                    }
                })
                .build();
    }
}
