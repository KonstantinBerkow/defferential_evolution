package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.dispatch.Futures;
import akka.pattern.Patterns;
import me.berkow.diffeval.message.DEResult;
import me.berkow.diffeval.message.DETask;
import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;
import me.berkow.diffeval.problem.Problems;
import me.berkow.diffeval.util.Util;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
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

    private static Population createNewGeneration(final Population previousGeneration, DETask task, Random random) {
        final int populationSize = task.getPopulationSize();
        final List<me.berkow.diffeval.problem.Member> newVectors = new ArrayList<>(populationSize);

        final Problem problem = task.getProblem();
        final int size = problem.getSize();

        final float crossoverProbability = task.getCrossoverProbability();
        final float amplification = task.getAmplification();

        final me.berkow.diffeval.problem.Member[] members = previousGeneration.getMembers();

        final float[] lowerConstraints = problem.getLowerConstraints();
        final float[] upperConstraints = problem.getUpperConstraints();

        for (int i = 0; i < populationSize; i++) {
            final me.berkow.diffeval.problem.Member oldVector = members[i];
            final int[] indexes = Util.selectIndexes(0, populationSize, i, 3, random);
            final int mandatoryIndex = random.nextInt(size);
            final float[] newVector = new float[size];

            for (int j = 0; j < size; j++) {
                if (mandatoryIndex == j || random.nextDouble() < crossoverProbability) {
                    newVector[j] = members[indexes[0]].get(j) + amplification * (members[indexes[1]].get(j) - members[indexes[2]].get(j));
                } else {
                    newVector[j] = oldVector.get(j);
                }

                final float min = lowerConstraints[j];
                final float max = upperConstraints[j];

                if (newVector[j] < min || max < newVector[j]) {
                    newVector[j] = Util.nextFloat(min, max, random);
                }
            }

            final me.berkow.diffeval.problem.Member member = new me.berkow.diffeval.problem.Member(newVector);

            if (problem.calculate(member) < problem.calculate(oldVector)) {
                newVectors.add(member);
            } else {
                newVectors.add(oldVector);
            }
        }

        return new Population(newVectors.toArray(new me.berkow.diffeval.problem.Member[0]));
    }

    private static DEResult de(DETask task, Random random) {
        Population previousGeneration = task.getInitialPopulation();
        final Problem problem = task.getProblem();
        int staleIterationsCount = 0;
        float previousValue = Float.NaN;
        for (int generation = 0; generation < task.getMaxIterationsCount(); generation++) {
            final Population newVectors = createNewGeneration(previousGeneration, task, random);

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
