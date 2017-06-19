package me.berkow.diffeval.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import me.berkow.diffeval.Algorithms;
import me.berkow.diffeval.message.DEResult;
import me.berkow.diffeval.message.DETask;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.stream.StreamSupport;


public class DECalculationActor extends AbstractActor {

    private final String port;
    private final Random random = new Random();
    private Cluster cluster;

    public DECalculationActor(String port) {
        this.port = port;
    }

    @Override
    public void preStart() throws Exception {
        final ActorSystem system = getContext().getSystem();

        cluster = Cluster.get(system);
        cluster.subscribe(getSelf(), ClusterEvent.MemberUp.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DETask.class, task -> {
                    final ExecutionContextExecutor dispatcher = getContext().getSystem().dispatcher();
                    final Future<DEResult> result = Futures.future(createCalculationCallable(task), dispatcher);

                    Patterns.pipe(result, dispatcher).to(getSender(), getSelf());
                })
                .match(ClusterEvent.CurrentClusterState.class, state -> {
                    StreamSupport.stream(state.getMembers().spliterator(), false)
                            .filter(member -> member.status().equals(MemberStatus.up()))
                            .forEach(this::register);
                })
                .match(ClusterEvent.MemberUp.class, mUp -> register(mUp.member()))
                .build();
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    private void register(Member member) {
        if (member.hasRole("frontend")) {
            getContext().actorSelection(member.address() + "/user/frontend")
                    .tell(DETaskActor.BACKEND_REGISTRATION, getSelf());
        }
    }

    private Callable<DEResult> createCalculationCallable(final DETask task) {
        return () -> Algorithms.standardDE(task, random);
    }

    @Override
    public String toString() {
        return "DECalculationActor{" +
                "port='" + port + '\'' +
                ", cluster=" + cluster +
                '}';
    }
}
