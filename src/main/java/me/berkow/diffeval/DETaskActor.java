package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Pair;
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

    private int currentIterationCount = 0;
    private int currentStaleParamsIterationsCount = 0;
    private float previousParamsValue = Float.NaN;
    private int currentStalePopulationIterationsCount = 0;
    private float previousPopulationValue = Float.NaN;
    private MainDETask currentTask = null;

    private LoggingAdapter logger;

    public DETaskActor(String port) {
        this.port = port;
        backends = new ArrayList<>();
        random = new Random();
    }

    private static DETask createTask(MainDETask task) {
        return new DETask(task.getMaxIterationsCount(), task.getPopulation(),
                task.getAmplification(), task.getCrossoverProbability(), task.getProblem(), task.getPrecision());
    }

    private static DETask createTask(MainDETask task, Random random) {
        final float f0 = task.getAmplification();
        final float c0 = task.getCrossoverProbability();

        final float rawF = f0 + (random.nextFloat() - 0.5F) / 2F;
        final float newF = Math.max(0F, Math.min(rawF, 2F));

        final float rawC = c0 + (random.nextFloat() - 0.5F) / 4F;
        final float newC = Math.max(0F, Math.min(rawC, 1F));

        return new DETask(task.getMaxIterationsCount(), task.getPopulation(), newF, newC, task.getProblem(), task.getPrecision());
    }

    private static DEResult selectResult(Iterable<DEResult> results) {
        final Iterator<DEResult> iterator = results.iterator();
        float previousValue = Float.MAX_VALUE;
        DEResult bestResult = null;
        while (iterator.hasNext()) {
            final DEResult result = iterator.next();
            final float value = result.getValue();

            if (bestResult == null) {
                previousValue = value;
                bestResult = result;
            } else if (value < previousValue) {
                previousValue = value;
                bestResult = result;
            }
        }

        return bestResult;
    }

    @Override
    public void preStart() throws Exception {
        logger = Logging.getLogger(this);

        logger.debug("{} pre start!", this);
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
                        TaskFailedMsg message = new TaskFailedMsg("Service unavailable, try again later", task);
                        sender().tell(message, sender());
                    }
                })
                .match(MainDETask.class, new FI.UnitApply<MainDETask>() {
                    @Override
                    public void apply(MainDETask task) throws Exception {
                        currentIterationCount = 0;

                        currentStaleParamsIterationsCount = 0;
                        previousParamsValue = Float.NaN;

                        currentStalePopulationIterationsCount = 0;
                        previousPopulationValue = Float.NaN;

                        currentTask = task;

                        calculate(task, sender());
                    }
                })
                .match(Pair.class, new FI.UnitApply<Pair>() {
                    @Override
                    public void apply(Pair pair) throws Exception {
                        final DEResult result = (DEResult) pair.first();
                        final ActorRef originalSender = (ActorRef) pair.second();

                        proceedResults(result, originalSender);
                    }
                })
                .matchEquals(BACKEND_REGISTRATION, new FI.UnitApply<String>() {
                    @Override
                    public void apply(String $) throws Exception {
                        final ActorRef sender = sender();
                        context().watch(sender);
                        backends.add(sender);
                    }
                })
                .match(Terminated.class, new FI.UnitApply<Terminated>() {
                    @Override
                    public void apply(Terminated terminated) throws Exception {
                        backends.remove(terminated.getActor());
                    }
                })
                .build();
    }

    private void proceedResults(DEResult result, ActorRef originalSender) {
        currentIterationCount++;
        currentStaleParamsIterationsCount = 0;

        final float amplification = result.getAmplification();
        final float crossoverProbability = result.getCrossoverProbability();

        final float newParamsValue = amplification + crossoverProbability;
        if (Float.isNaN(previousParamsValue)) {
            previousParamsValue = newParamsValue;
        }

        final float newPopulationValue = result.getValue();
        if (Float.isNaN(previousPopulationValue)) {
            previousPopulationValue = newPopulationValue;
        }

        logger.info("new amplification: {}", result.getAmplification());
        logger.info("new crossover probability: {}", result.getCrossoverProbability());
        logger.info("new params value: {}", newParamsValue);
        logger.info("new average population value: {}", newPopulationValue);
        logger.info("new average member: {}", Util.calculateAverageMember(result.getPopulation()));

        if (Math.abs(newParamsValue - previousParamsValue) < currentTask.getPrecision()) {
            currentStaleParamsIterationsCount++;
        } else {
            currentStaleParamsIterationsCount = 0;
            previousParamsValue = newParamsValue;
        }

        if (currentStaleParamsIterationsCount >= currentTask.getMaxStaleCount()) {
            onCompleted(result, "stale_params", originalSender);
            return;
        }

        if (Math.abs(newPopulationValue - previousPopulationValue) < currentTask.getPrecision()) {
            currentStalePopulationIterationsCount++;
        } else {
            currentStalePopulationIterationsCount = 0;
            previousPopulationValue = newPopulationValue;
        }

        if (currentStalePopulationIterationsCount >= currentTask.getMaxStaleCount()) {
            onCompleted(result, "stale_population", originalSender);
            return;
        }

        if (currentIterationCount >= currentTask.getMaxIterationsCount()) {
            onCompleted(result, "max_iterations", originalSender);
            return;
        }

        final MainDETask newTask = new MainDETask(currentTask.getMaxIterationsCount(), currentTask.getMaxStaleCount(),
                result.getPopulation(), amplification, crossoverProbability, currentTask.getSplitSize(), result.getProblem(),
                currentTask.getPrecision());

        calculate(newTask, originalSender);
    }

    private void onCompleted(DEResult result, String type, ActorRef originalSender) {
        final MainDEResult mainDEResult = new MainDEResult(result, type, currentIterationCount);
        originalSender.tell(mainDEResult, self());
    }

    @Override
    public void postStop() throws Exception {
        logger.debug("{} postStop", this);
        backends.clear(); //huh?
    }

    private void calculate(MainDETask task, final ActorRef originalSender) {
        final ActorSystem system = context().system();

        logger.debug("Calculate from: {}, by: {}", task.getPopulation(), this);

        final int splitSize = task.getSplitSize();

        final List<DETask> tasks = new ArrayList<>(splitSize);
        tasks.add(createTask(task));
        for (int i = 1; i < splitSize; i++) {
            tasks.add(createTask(task, random));
        }

        final List<Future<DEResult>> futures = new ArrayList<>(splitSize);
        for (int i = 0; i < tasks.size(); i++) {
            final DETask splitedTask = tasks.get(i);

            logger.debug("new control values F: {}, CR: {}", splitedTask.getAmplification(), splitedTask.getCrossoverProbability());

            final Future<DEResult> future = Patterns.ask(backends.get(i % backends.size()), splitedTask, 10000)
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

        final Future<Pair<DEResult, ActorRef>> resultFuture = resultsFuture.transform(new Mapper<Iterable<DEResult>, Pair<DEResult, ActorRef>>() {
            @Override
            public Pair<DEResult, ActorRef> apply(Iterable<DEResult> results) {
                return new Pair<>(selectResult(results), originalSender);
            }
        }, new Function1<Throwable, Throwable>() {
            @Override
            public Throwable apply(Throwable v1) {
                return v1;
            }
        }, system.dispatcher());

        Patterns.pipe(resultFuture, system.dispatcher()).to(self(), self());
    }

    @Override
    public String toString() {
        return "DETaskActor{" +
                "port='" + port + '\'' +
                ", backends=" + backends +
                '}';
    }
}
