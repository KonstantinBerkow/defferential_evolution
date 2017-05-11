package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.dispatch.Futures;
import akka.japi.pf.FI;
import akka.pattern.Patterns;
import me.berkow.diffeval.message.DEResult;
import me.berkow.diffeval.message.DETask;
import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;
import me.berkow.diffeval.problem.Problems;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;


public class DECalculationActor extends AbstractActor {

    private final String port;
    private final Random random = new Random();
    private Cluster cluster;

    public DECalculationActor(String port) {
        this.port = port;
    }

    private static Population createNewGeneration(final Population previousGeneration, DETask task, Random random) {
        final List<me.berkow.diffeval.problem.Member> newVectors = new ArrayList<>(task.getPopulationSize());

        final Problem problem = task.getProblem();
        final int size = problem.getSize();

        final float crossoverProbability = task.getCrossoverProbability();
        final float amplification = task.getAmplification();

        final me.berkow.diffeval.problem.Member[] members = previousGeneration.getMembers();

        final double[] lowerConstraints = problem.getLowerConstraints();
        final double[] upperConstraints = problem.getUpperConstraints();

        for (int i = 0; i < task.getPopulationSize(); i++) {
            final me.berkow.diffeval.problem.Member oldVector = members[i];
            final int[] indexes = Util.selectIndexes(0, oldVector.size(), i, 3, random);
            final int mandatoryIndex = random.nextInt(size);
            final double[] newVector = new double[size];

            for (int j = 0; j < size; j++) {
                if (mandatoryIndex == j || random.nextDouble() < crossoverProbability) {
                    newVector[j] = members[indexes[0]].get(j) + amplification * (members[indexes[1]].get(j) - members[indexes[2]].get(j));
                } else {
                    newVector[j] = oldVector.get(j);
                }

                final double min = lowerConstraints[j];
                final double max = upperConstraints[j];

                if (newVector[j] < min || max < newVector[j]) {
                    newVector[j] = Util.nextDouble(min, max, random);
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
        double previousValue = Double.NaN;
        for (int generation = 0; generation < task.getMaxIterationsCount(); generation++) {
            final Population newVectors = createNewGeneration(previousGeneration, task, random);

            final double newValue = Problems.calculatePopulationValue(problem, newVectors);

            if (Math.abs(newValue - previousValue) < DEFrontendMain.PRECISION) {
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
        final ActorSystem system = context().system();

        cluster = Cluster.get(system);
        cluster.subscribe(self(), ClusterEvent.MemberUp.class);

        system.log().debug("{} pre start!", this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DETask.class, new FI.UnitApply<DETask>() {
                    @Override
                    public void apply(DETask task) throws Exception {
                        final Future<DEResult> result = Futures.future(createCalculationCallable(task), context().system().dispatcher());

                        Patterns.pipe(result, context().system().dispatcher()).to(sender(), self());
                    }
                })
                .match(ClusterEvent.CurrentClusterState.class, new FI.UnitApply<ClusterEvent.CurrentClusterState>() {
                    @Override
                    public void apply(ClusterEvent.CurrentClusterState state) throws Exception {
                        for (Member member : state.getMembers()) {
                            if (member.status().equals(MemberStatus.up())) {
                                register(member);
                            }
                        }
                    }
                })
                .match(ClusterEvent.MemberUp.class, new FI.UnitApply<ClusterEvent.MemberUp>() {
                    @Override
                    public void apply(ClusterEvent.MemberUp mUp) throws Exception {
                        register(mUp.member());
                    }
                })
                .build();
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    private void register(Member member) {
        if (member.hasRole("frontend")) {
            context().actorSelection(member.address() + "/user/frontend").tell(DETaskActor.BACKEND_REGISTRATION, self());
        }
    }

    private Callable<DEResult> createCalculationCallable(final DETask task) {
        return new Callable<DEResult>() {
            @Override
            public DEResult call() throws Exception {
                return de(task, random);
            }
        };
    }

    @Override
    public String toString() {
        return "DECalculationActor{" +
                "port='" + port + '\'' +
                ", cluster=" + cluster +
                '}';
    }
}
