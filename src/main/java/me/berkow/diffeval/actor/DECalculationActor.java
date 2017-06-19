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
import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;
import me.berkow.diffeval.problem.Problems;
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

    private static DEResult de(DETask task, Random random) {
        Population previousGeneration = task.getInitialPopulation();
        final Problem problem = task.getProblem();
        int staleIterationsCount = 0;
        float previousValue = Float.NaN;
        for (int generation = 0; generation < task.getMaxIterationsCount(); generation++) {
            final Population newVectors = Algorithms.createNewGeneration(previousGeneration, task, random);

            final float newValue = Problems.calculatePopulationValue(problem, newVectors);

            if (Math.abs(newValue - previousValue) < task.getPrecision()) {
                staleIterationsCount++;
            } else {
                staleIterationsCount = 0;
            }

            if (staleIterationsCount >= 10) {
                return new DEResult(newVectors, task.getAmplification(), task.getCrossoverProbability(), task.getProblem(), newValue);
            }

            previousGeneration = newVectors;
            previousValue = newValue;
        }

        return new DEResult(previousGeneration, task.getAmplification(), task.getCrossoverProbability(), task.getProblem(), previousValue);
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
        return () -> de(task, random);
    }

    @Override
    public String toString() {
        return "DECalculationActor{" +
                "port='" + port + '\'' +
                ", cluster=" + cluster +
                '}';
    }
}
