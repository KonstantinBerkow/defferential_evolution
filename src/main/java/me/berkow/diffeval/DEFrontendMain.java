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

import java.io.Console;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class DEFrontendMain {

    private static volatile boolean sCanProcessInput = true;

    public static void main(String[] args) {
        final Map<String, String> argsMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argsMap.put(args[i], args[i + 1]);
        }

        final String port = argsMap.containsKey("-port") ? argsMap.get("-port") : "0";

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

        system.log().debug("Started up at {}", System.currentTimeMillis());

        final Props taskActorProps = Props.create(DETaskActor.class, port);

        final ActorRef taskActorRef = system.actorOf(taskActorProps, "frontend");

        startReadingInput(system, taskActorRef);

//        system.scheduler().scheduleOnce(FiniteDuration.apply(10, TimeUnit.SECONDS), new Runnable() {
//            @Override
//            public void run() {
//                process(task, system, taskActorRef);
//            }
//        }, system.dispatcher());
    }

    private static void startReadingInput(final ActorSystem system, ActorRef taskActorRef) {
        final Console console = System.console();

        boolean working = true;
        while (working) {
            final String taskRawData = console.readLine("Input: ");
            if ("stop".equals(taskRawData)) {
                working = false;
            } else if (sCanProcessInput) {
                sCanProcessInput = false;

                final String[] splits = taskRawData.split("\\s+");

                final Map<String, String> argsMap = new HashMap<>();
                for (int i = 0; i < splits.length; i += 2) {
                    argsMap.put(splits[i], splits[i + 1]);
                }

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

                final Problem problem = Problems.createProblemWithConstraints(problemId, lowerBounds, upperBounds);

                final Random random = randomSeed == -1 ? new Random() : new Random(randomSeed);

                final Population population = Problems.createRandomPopulation(populationSize, problem, random);

                final MainDETask task = new MainDETask(maxIterations, maxStale, population,
                        amplification, crossoverProbability, splitCount, problem, precision);

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
                }, system.dispatcher()).onComplete(new OnComplete<DEResult>() {
                    @Override
                    public void onComplete(Throwable failure, DEResult success) throws Throwable {
                        sCanProcessInput = true;
                        if (failure == null) {
                            onCompleted(system, success, "none");
                        } else {
                            onFailure(system, failure);
                        }
                    }
                }, system.dispatcher());
            } else {
                system.log().debug("Please wait for previous task!");
            }
        }
    }

    private static void onCompleted(ActorSystem system, DEResult result, String type) {
        final LoggingAdapter log = system.log();
        log.info("Completed due: {}", type);
        log.info("result population: {}", result.getPopulation());
    }

    private static void onFailure(ActorSystem system, Throwable failure) {
        system.log().error(failure, "System {} start up failed!", system);
    }

//    private static void onResult(MainDETask task, ActorSystem system, DEResult result, ActorRef actor) {
//        sCurrentIteration++;
//
//        system.log().debug("new result control values F: {}, CR: {}", result.getAmplification(), result.getCrossoverProbability());
//        system.log().debug("average value: {}", result.getValue());
//
//        final float amplification = result.getAmplification();
//        final float crossoverProbability = result.getCrossoverProbability();
//
//        if (Math.abs(amplification + crossoverProbability - sPrevious) < task.getPrecision()) {
//            sStaleIterationsCount++;
//        } else {
//            sStaleIterationsCount = 0;
//            sPrevious = amplification + crossoverProbability;
//        }
//
//        if (sStaleIterationsCount >= task.getMaxStaleCount()) {
//            onCompleted(system, result, "stale");
//            return;
//        }
//
//        if (sCurrentIteration >= task.getMaxIterationsCount()) {
//            onCompleted(system, result, "max_iterations");
//            return;
//        }
//
//
//        final MainDETask newTask = new MainDETask(task.getMaxIterationsCount(), task.getMaxStaleCount(),
//                result.getPopulation(), amplification, crossoverProbability, task.getSplitSize(), result.getProblem(),
//                task.getPrecision());
//        process(newTask, system, actor);
//    }
}
