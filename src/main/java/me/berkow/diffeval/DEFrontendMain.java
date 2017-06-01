package me.berkow.diffeval;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import me.berkow.diffeval.message.DEResult;
import me.berkow.diffeval.message.MainDETask;
import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;
import me.berkow.diffeval.problem.Problems;
import scala.Function1;
import scala.concurrent.duration.FiniteDuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class DEFrontendMain {
    public static final double PRECISION = 1E-6;

    private static int sCurrentIteration = 0;
    private static int sStaleIterationsCount = 0;
    private static double sPrevious = Double.NaN;

    public static void main(String[] args) {
        final Map<String, String> argsMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argsMap.put(args[i], args[i + 1]);
        }

        final String port = argsMap.containsKey("-port") ? argsMap.get("-port") : "0";

        final int maxIterations = Util.getIntOrDefault(argsMap, "-maxIterations", 100);
        final int maxStale = Util.getIntOrDefault(argsMap, "-maxStale", 10);
        final int problemId = Util.getIntOrDefault(argsMap, "-problemId", 5);
        final int populationSize = Util.getIntOrDefault(argsMap, "-populationSize", 100);
        final int splitCount = Util.getIntOrDefault(argsMap, "-splitCount", 10);
        final long randomSeed = Util.getLongOrDefault(argsMap, "-randomSeed", -1);

        float amplification = Util.getFloatOrDefault(argsMap, "-amplification", 0.9F);
        amplification = Math.max(0, Math.min(amplification, 2));

        float crossoverProbability = Util.getFloatOrDefault(argsMap, "-crossover", 0.5F);
        crossoverProbability = Math.max(0, Math.min(crossoverProbability, 1));

        final double[] lowerBounds = Util.getDoubleArrayOrThrow(argsMap, "-lowerBounds", "Supply lower bounds!");
        final double[] upperBounds = Util.getDoubleArrayOrThrow(argsMap, "-upperBounds", "Supply upper bounds!");

        final double precision = Util.getDoubleOrDefault(argsMap, "-precision", 1e-6);

        Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port);

        try {
            InetAddress localHost = InetAddress.getLocalHost();

            String hostAddress = localHost.getHostAddress();

            Map<String, Object> map = new HashMap<>();

            map.put("akka.remote.netty.tcp.hostname", hostAddress);
            map.put("akka.cluster.seed-nodes", Arrays.asList(
                    "akka.tcp://DifferentialEvolution@" + hostAddress + ":2552",
                    "akka.tcp://DifferentialEvolution@" + hostAddress + ":2553"
            ));

            config = config.withFallback(ConfigFactory.parseMap(map));

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        config = config
                .withFallback(ConfigFactory.parseString("akka.cluster.roles = [frontend]"))
                .withFallback(ConfigFactory.load());

        final ActorSystem system = ActorSystem.create("DifferentialEvolution", config);

        final Props taskActorProps = Props.create(DETaskActor.class, port);

        final ActorRef taskActorRef = system.actorOf(taskActorProps, "frontend");

        final Problem problem = Problems.createProblemWithConstraints(problemId, lowerBounds, upperBounds);

        final Random random = randomSeed == -1 ? new Random() : new Random(randomSeed);

        final Population population = Problems.createRandomPopulation(populationSize, problem, random);

        final MainDETask task = new MainDETask(maxIterations, maxStale, population,
                amplification, crossoverProbability, splitCount, problem, precision);

        system.scheduler().scheduleOnce(FiniteDuration.apply(10, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {
                process(task, system, taskActorRef);
            }
        }, system.dispatcher());
    }

    private static void process(final MainDETask task, final ActorSystem system, final ActorRef taskActorRef) {
        Patterns.ask(taskActorRef, task, 10000).transform(new Function1<Object, DEResult>() {
            @Override
            public DEResult apply(Object v1) {
                return (DEResult) v1;
            }
        }, new Function1<Throwable, Throwable>() {
            @Override
            public Throwable apply(Throwable error) {
                return error;
            }
        }, system.dispatcher())
                .onComplete(new OnComplete<DEResult>() {
                    @Override
                    public void onComplete(Throwable failure, DEResult success) throws Throwable {
                        if (failure != null) {
                            onFailure(system, failure);
                        } else {
                            onResult(task, system, success, taskActorRef);
                        }
                    }
                }, system.dispatcher());
    }

    private static void onResult(MainDETask task, ActorSystem system, DEResult result, ActorRef actor) {
        sCurrentIteration++;

        system.log().debug("new result control values F: {}, CR: {}", result.getAmplification(), result.getCrossoverProbability());
        system.log().debug("average value: {}", result.getValue());

        final float amplification = result.getAmplification();
        final float crossoverProbability = result.getCrossoverProbability();

        if (Math.abs(amplification + crossoverProbability - sPrevious) < task.getPrecision()) {
            sStaleIterationsCount++;
        } else {
            sStaleIterationsCount = 0;
            sPrevious = amplification + crossoverProbability;
        }

        if (sStaleIterationsCount >= task.getMaxStaleCount()) {
            onCompleted(system, result, "stale");
            return;
        }

        if (sCurrentIteration >= task.getMaxIterationsCount()) {
            onCompleted(system, result, "max_iterations");
            return;
        }


        final MainDETask newTask = new MainDETask(task.getMaxIterationsCount(), task.getMaxStaleCount(),
                result.getPopulation(), amplification, crossoverProbability, task.getSplitSize(), result.getProblem(),
                task.getPrecision());
        process(newTask, system, actor);
    }

    private static void onFailure(ActorSystem system, Throwable failure) {
        system.log().error(failure, "Failed!");
    }

    private static void onCompleted(ActorSystem system, DEResult result, String type) {
        final LoggingAdapter log = system.log();
        log.info("Completed due: {}", type);
        log.info("result population: {}", result.getPopulation());
    }
}
