package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.japi.pf.FI;
import akka.pattern.Patterns;
import me.berkow.diffeval.message.*;
import scala.Function1;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by konstantinberkow on 5/8/17.
 */
public class DETaskActor extends AbstractActor {
    public static final String BACKEND_REGISTRATION = "register";

    private final String port;
    private final List<ActorRef> backends;
    private final Random random;

    public DETaskActor(String port) {
        this.port = port;
        backends = new ArrayList<>();
        random = new Random();
    }

    private static DETask createTask(MainDETask task) {
        return new DETask(task.getMaxIterationsCount(), task.getInitialPopulation(),
                task.getInitialAmplification(), task.getInitialConvergence(), task.getProblem());
    }

    private static DETask createTask(MainDETask task, Random random) {
        final float f0 = task.getInitialAmplification();
        final float c0 = task.getInitialConvergence();

        final float rawF = f0 + (random.nextFloat() - 1F) / 2F;
        final float newF = Math.max(0F, Math.min(rawF, 2F));

        final float rawC = c0 + (random.nextFloat() - 1F) / 4F;
        final float newC = Math.max(0F, Math.min(rawC, 1F));

        return new DETask(task.getMaxIterationsCount(), task.getInitialPopulation(), newF, newC, task.getProblem());
    }

    @Override
    public void preStart() throws Exception {
        context().system().log().debug("{} pre start!", this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MainDETask.class, new FI.TypedPredicate<MainDETask>() {
                    @Override
                    public boolean defined(MainDETask task) {
                        return backends.isEmpty();
                    }
                }, new FI.UnitApply<MainDETask>() {
                    @Override
                    public void apply(MainDETask task) throws Exception {
                        context().system().log().debug("{} could not calculate {}", DETaskActor.this, task);
                        TaskFailedMsg message = new TaskFailedMsg("Service unavailable, try again later", task);
                        sender().tell(message, sender());
                    }
                })
                .match(MainDETask.class, new FI.UnitApply<MainDETask>() {
                    @Override
                    public void apply(MainDETask task) throws Exception {
                        context().system().log().debug("{} received task {}", DETaskActor.this, task);
                        calculate(task);
                    }
                })
                .matchEquals(BACKEND_REGISTRATION, new FI.UnitApply<String>() {
                    @Override
                    public void apply(String $) throws Exception {
                        final ActorRef sender = sender();
                        context().watch(sender);
                        backends.add(sender);

                        context().system().log().debug("{} received registration of {}", DETaskActor.this, sender);
                    }
                })
                .match(Terminated.class, new FI.UnitApply<Terminated>() {
                    @Override
                    public void apply(Terminated terminated) throws Exception {
                        context().system().log().debug("{} received termination of {}", DETaskActor.this, terminated.getActor());
                        backends.remove(terminated.getActor());
                    }
                })
                .build();
    }

    @Override
    public void postStop() throws Exception {
        context().system().log().debug("{} postStop", this);
        backends.clear(); //huh?
    }

    private void calculate(MainDETask task) {
        final ActorSystem system = context().system();

        system.log().debug("Calculate task: {}, by: {}", task, this);

        final int splitSize = task.getSplitSize();

        final List<DETask> tasks = new ArrayList<>(splitSize);
        tasks.add(createTask(task));
        for (int i = 1; i < splitSize; i++) {
            tasks.add(createTask(task, random));
        }

        final List<Future<DEResult>> futures = new ArrayList<>(splitSize);
        for (int i = 0; i < tasks.size(); i++) {
            final Future<DEResult> future = Patterns.ask(backends.get(i % backends.size()), tasks.get(i), 10000)
                    .transform(new Mapper<Object, DEResult>() {
                        @Override
                        public DEResult apply(Object parameter) {
                            return (DEResult) parameter;
                        }
                    }, new Function1<Throwable, Throwable>() {
                        @Override
                        public Throwable apply(Throwable v1) {
                            return v1;
                        }
                    }, system.dispatcher());

            futures.add(future);
        }

        final Future<Iterable<DEResult>> resultsFuture = Futures.sequence(futures, system.dispatcher());

        final Future<MainDEResult> resultFuture = resultsFuture.transform(new Mapper<Iterable<DEResult>, MainDEResult>() {
            @Override
            public MainDEResult apply(Iterable<DEResult> results) {
                return processResults(results);
            }
        }, new Function1<Throwable, Throwable>() {
            @Override
            public Throwable apply(Throwable v1) {
                return v1;
            }
        }, system.dispatcher());

        Patterns.pipe(resultFuture, system.dispatcher()).to(sender(), self());
    }

    private MainDEResult processResults(Iterable<DEResult> results) {
        final Iterator<DEResult> iterator = results.iterator();
        double previousValue = Double.MAX_VALUE;
        DEResult bestResult = null;
        while (iterator.hasNext()) {
            final DEResult result = iterator.next();
            final double value = result.getValue();

            if (bestResult == null) {
                previousValue = value;
                bestResult = result;
            } else if (value < previousValue) {
                previousValue = value;
                bestResult = result;
            }
        }

        final MainDEResult result = new MainDEResult(bestResult.getPopulation(), bestResult.getAmplification(),
                bestResult.getConvergence(), bestResult.getProblem(), bestResult.getValue());

        return result;
    }

    @Override
    public String toString() {
        return "DETaskActor{" +
                "port='" + port + '\'' +
                ", backends=" + backends +
                '}';
    }
}
