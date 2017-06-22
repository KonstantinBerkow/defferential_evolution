package me.berkow.diffeval.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.dispatch.Futures;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Pair;
import akka.pattern.Patterns;
import me.berkow.diffeval.UtilKt;
import me.berkow.diffeval.message.*;
import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.Problem;
import me.berkow.diffeval.problem.ProblemsKt;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by konstantinberkow on 5/8/17.
 */
public class TaskActor extends AbstractActor {
    static final String BACKEND_REGISTRATION = "register";

    private final String port;
    private final List<ActorRef> backends;
    private final Random random;
    private final LoggingAdapter logger = Logging.getLogger(this);
    private final NumberFormat format = NumberFormat.getInstance();

    private int currentIterationCount = 0;
    private MainTask currentTask = null;
    private float previousValue;

    public TaskActor(String port) {
        this.port = port;
        backends = new ArrayList<>();
        random = new Random();
    }

    private static SubTask createTask(MainTask task) {
        return new SubTask(task.getMaxIterationsCount(), task.getPopulation(),
                task.getAmplification(), task.getCrossoverProbability(), task.getProblem(), task.getPrecision());
    }

    private static SubTask createTask(MainTask task, Random random) {
        final float f0 = task.getAmplification();
        final float c0 = task.getCrossoverProbability();

        float newF = f0;
        while (newF == f0) {
            newF = UtilKt.nextFloat(random, 0, 2);
        }

        float newC = c0;
        while (newC == c0) {
            newC = UtilKt.nextFloat(random, 0, 1);
        }

        return new SubTask(task.getMaxIterationsCount(), task.getPopulation(), newF, newC, task.getProblem(), task.getPrecision());
    }

    private static SubResult selectResult(Iterable<SubResult> results) {
        final Iterator<SubResult> iterator = results.iterator();
        float previousValue = Float.MAX_VALUE;
        SubResult bestResult = null;
        while (iterator.hasNext()) {
            final SubResult result = iterator.next();
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
        logger.debug("{} pre start!", this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MainTask.class, task -> backends.isEmpty(), task -> {
                    TaskFailedMsg message = new TaskFailedMsg("Service unavailable, try again later", task);
                    final ActorRef sender = getSender();
                    sender.tell(message, sender);
                })
                .match(MainTask.class, task -> {
                    currentIterationCount = 0;
                    previousValue = ProblemsKt.averageValue(task.getPopulation(), task.getProblem());

                    currentTask = task;
                    format.setMaximumFractionDigits(task.getPrecision());

                    calculate(task, getSender());
                })
                .match(Pair.class, pair -> {
                    final SubResult result = (SubResult) pair.first();
                    final ActorRef originalSender = (ActorRef) pair.second();

                    proceedResults(result, originalSender);
                })
                .matchEquals(BACKEND_REGISTRATION, $ -> {
                    final ActorRef sender = getSender();
                    getContext().watch(sender);
                    backends.add(sender);
                })
                .match(Terminated.class, terminated -> backends.remove(terminated.getActor()))
                .build();
    }

    private void proceedResults(SubResult result, ActorRef originalSender) {
        currentIterationCount++;

        final int maxIterationsCount = currentTask.getMaxIterationsCount();
        final float amplification = result.getAmplification();
        final float crossoverProbability = result.getCrossoverProbability();
        final Population population = result.getPopulation();
        final Problem problem = result.getProblem();
        final int precision = currentTask.getPrecision();
        final float newValue = result.getValue();

        logger.info("iteration: {}", currentIterationCount);
        logger.info("new amplification: {}", result.getAmplification());
        logger.info("new crossover probability: {}", result.getCrossoverProbability());
        logger.info("new population value: {}", result.getValue());
        logger.info("diff: {}", Math.abs(newValue - previousValue));


        if (Math.abs(newValue - previousValue) < 1.0 / Math.pow(10, precision)) {
            onCompleted(result, "converged_population", originalSender);
            return;
        }

        if (currentIterationCount >= maxIterationsCount) {
            onCompleted(result, "max_iterations", originalSender);
            return;
        }

        final MainTask newTask = new MainTask(maxIterationsCount, population, amplification, crossoverProbability,
                currentTask.getSplitSize(), problem, precision);

        previousValue = newValue;

        calculate(newTask, originalSender);
    }

    private void onCompleted(SubResult result, String type, ActorRef originalSender) {
        final MainResult mainDEResult = new MainResult(result, type, currentIterationCount);
        originalSender.tell(mainDEResult, getSelf());
    }

    @Override
    public void postStop() throws Exception {
        logger.debug("{} postStop", this);
        backends.clear(); //huh?
    }

    private void calculate(MainTask task, final ActorRef originalSender) {
        final ActorSystem system = getContext().getSystem();

        logger.debug("Calculate from: {}, by: {}", task.getPopulation(), this);

        final int splitSize = task.getSplitSize();

        final ExecutionContextExecutor dispatcher = system.dispatcher();

        final List<SubTask> tasks = new ArrayList<>(splitSize);
        tasks.add(createTask(task));
        for (int i = 1; i < splitSize; i++) {
            tasks.add(createTask(task, random));
        }

        final List<Future<SubResult>> futures = new ArrayList<>(splitSize);
        for (int i = 0; i < tasks.size(); i++) {
            final SubTask splitedTask = tasks.get(i);

            logger.debug("new control values F: {}, CR: {}", splitedTask.getAmplification(), splitedTask.getCrossoverProbability());

            final Future<SubResult> future = Patterns.ask(backends.get(i % backends.size()), splitedTask, 10000)
                    .transform(result -> (SubResult) result, error -> error, dispatcher);

            futures.add(future);
        }

        final Future<Iterable<SubResult>> resultsFuture = Futures.sequence(futures, dispatcher);

        final Future<Pair<SubResult, ActorRef>> resultFuture = resultsFuture
                .transform(results -> new Pair<>(selectResult(results), originalSender), error -> error, dispatcher);

        final ActorRef self = getSelf();

        Patterns.pipe(resultFuture, dispatcher).to(self, self);
    }

    @Override
    public String toString() {
        return "TaskActor{" +
                "port='" + port + '\'' +
                ", backends=" + backends +
                '}';
    }
}
