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
import me.berkow.diffeval.message.*;
import me.berkow.diffeval.problem.Member;
import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;
import me.berkow.diffeval.problem.Problems;
import me.berkow.diffeval.util.Util;
import scala.concurrent.Future;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Created by konstantinberkow on 5/8/17.
 */
public class DETaskActor extends AbstractActor {
    static final String BACKEND_REGISTRATION = "register";

    private final String port;
    private final List<ActorRef> backends;
    private final Random random;

    private int currentIterationCount = 0;
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
                .match(MainDETask.class, task -> backends.isEmpty(), task -> {
                    TaskFailedMsg message = new TaskFailedMsg("Service unavailable, try again later", task);
                    final ActorRef sender = getSender();
                    sender.tell(message, sender);
                })
                .match(MainDETask.class, task -> {
                    currentIterationCount = 0;

                    currentTask = task;

                    calculate(task, getSender());
                })
                .match(Pair.class, pair -> {
                    final DEResult result = (DEResult) pair.first();
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

    private void proceedResults(DEResult result, ActorRef originalSender) {
        currentIterationCount++;

        final int maxIterationsCount = currentTask.getMaxIterationsCount();
        final float amplification = result.getAmplification();
        final float crossoverProbability = result.getCrossoverProbability();
        final Population population = result.getPopulation();
        final Problem problem = result.getProblem();
        final float precision = currentTask.getPrecision();

        logger.info("iteration: {}", currentIterationCount);
        logger.info("new amplification: {}", result.getAmplification());
        logger.info("new crossover probability: {}", result.getCrossoverProbability());

        final Member[] members = result.getPopulation().getMembers();
        final int size = members.length;
        boolean stop = false;
        for (int i = 0; i < size && !stop; i++) {
            for (int j = i + 1; j < size; j++) {
                final float diff = Math.abs(problem.calculate(members[i]) - problem.calculate(members[j]));
                if (diff >= precision) {
                    logger.info("Difference {}", Util.prettyNumber(diff));
                    final String formattedComponents = Util.prettyFloatArray(Problems.componentsDiff(members[i], members[j]));
                    logger.info("Component differences: {}", formattedComponents);
                    stop = true;
                    break;
                }
            }
        }

        if (Problems.checkConvergence(population, problem, precision)) {
            onCompleted(result, "converged_population", originalSender);
            return;
        }

        if (currentIterationCount >= maxIterationsCount) {
            onCompleted(result, "max_iterations", originalSender);
            return;
        }

        final MainDETask newTask = new MainDETask(maxIterationsCount, population, amplification, crossoverProbability,
                currentTask.getSplitSize(), problem, precision);

        calculate(newTask, originalSender);
    }

    private void onCompleted(DEResult result, String type, ActorRef originalSender) {
        final MainDEResult mainDEResult = new MainDEResult(result, type, currentIterationCount);
        originalSender.tell(mainDEResult, getSelf());
    }

    @Override
    public void postStop() throws Exception {
        logger.debug("{} postStop", this);
        backends.clear(); //huh?
    }

    private void calculate(MainDETask task, final ActorRef originalSender) {
        final ActorSystem system = getContext().getSystem();

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
                    .transform(result -> (DEResult) result, error -> error, system.dispatcher());

            futures.add(future);
        }

        final Future<Iterable<DEResult>> resultsFuture = Futures.sequence(futures, system.dispatcher());

        final Future<Pair<DEResult, ActorRef>> resultFuture = resultsFuture
                .transform(results -> new Pair<>(selectResult(results), originalSender), error -> error, system.dispatcher());

        final ActorRef self = getSelf();

        Patterns.pipe(resultFuture, system.dispatcher()).to(self, self);
    }

    @Override
    public String toString() {
        return "DETaskActor{" +
                "port='" + port + '\'' +
                ", backends=" + backends +
                '}';
    }
}
