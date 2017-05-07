package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.japi.pf.FI;

public class HelloWorld extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(Msg.DONE, new FI.UnitApply<Msg>() {
                    public void apply(Msg m) throws Exception {
                        // when the greeter is done, stop this actor and with it the application
                        HelloWorld.this.getContext().stop(HelloWorld.this.self());
                    }
                })
                .build();
    }

    @Override
    public void preStart() {
        // create the greeter actor
        final ActorRef greeter = getContext().actorOf(Props.create(Greeter.class), "greeter");
        // tell it to perform the greeting
        greeter.tell(Msg.GREET, self());
    }
}
