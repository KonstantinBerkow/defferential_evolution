package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.japi.pf.FI;

public class Greeter extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(Msg.GREET, new FI.UnitApply<Msg>() {
                    public void apply(Msg m) throws Exception {
                        System.out.println("Hello World!");
                        Greeter.this.sender().tell(Msg.DONE, Greeter.this.self());
                    }
                })
                .build();
    }
}
