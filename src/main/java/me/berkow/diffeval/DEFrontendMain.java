package me.berkow.diffeval;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import me.berkow.diffeval.actor.TaskActor;
import me.berkow.diffeval.message.MainResult;
import me.berkow.diffeval.message.MainTask;
import me.berkow.diffeval.problem.Population;
import me.berkow.diffeval.problem.Problem;
import me.berkow.diffeval.problem.ProblemsKt;
import me.berkow.diffeval.util.Util;

import java.io.Console;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DEFrontendMain {

    private static volatile boolean sCanProcessInput = true;

    public static void main(String[] args) {
        final Map<String, String> argsMap = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            argsMap.put(args[i], args[i + 1]);
        }

        final String port = argsMap.getOrDefault("-port", "0");

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

        final Props taskActorProps = Props.create(TaskActor.class, port);

        final ActorRef taskActorRef = system.actorOf(taskActorProps, "frontend");

        startReadingInput(system, taskActorRef);
    }

    private static void startReadingInput(final ActorSystem system, ActorRef taskActorRef) {
        final LoggingAdapter logger = Logging.getLogger(system, "Frontend");
        final Console console = System.console();

        boolean working = true;
        logger.info("Input your task's parameters:");
        while (working) {
            final String input = console.readLine();
            if ("stop".equals(input)) {
                working = false;
            } else if (sCanProcessInput) {
                try {
                    processInput(system, taskActorRef, input, logger);
                    sCanProcessInput = false;
                } catch (Exception e) {
                    logger.error("Failed to process your input: {} due: {}", input, e);
                }
            } else {
                logger.error("Please wait for previous task!");
            }
        }

        System.exit(0);
    }

    private static void processInput(final ActorSystem system, ActorRef taskActorRef, String input, final LoggingAdapter logger) {
        final List<String> splits = new ArrayList<>();
        final Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        final Matcher regexMatcher = regex.matcher(input);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                splits.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                splits.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                splits.add(regexMatcher.group());
            }
        }

        final Map<String, String> argsMap = new HashMap<>();
        for (int i = 0; i < splits.size(); i += 2) {
            argsMap.put(splits.get(i), splits.get(i + 1));
        }

        final int maxIterations = Util.getIntOrDefault(argsMap, "-maxIterations", 100);
        final int problemId = Util.getIntOrDefault(argsMap, "-problemId", 5);
        final int populationSize = Util.getIntOrDefault(argsMap, "-populationSize", 100);
        final int splitCount = Util.getIntOrDefault(argsMap, "-splitCount", 10);
        final long randomSeed = Util.getLongOrDefault(argsMap, "-randomSeed", -1);

        float amplification = Util.getFloatOrDefault(argsMap, "-amplification", 0.9F);
        amplification = Math.max(0, Math.min(amplification, 2));

        float crossoverProbability = Util.getFloatOrDefault(argsMap, "-crossover", 0.5F);
        crossoverProbability = Math.max(0, Math.min(crossoverProbability, 1));

        final float[] lowerBounds = Util.getFloatArrayOrThrow(argsMap, "-lowerBounds", "Supply lower bounds!");
        final float[] upperBounds = Util.getFloatArrayOrThrow(argsMap, "-upperBounds", "Supply upper bounds!");

        final int precision = Util.getIntOrDefault(argsMap, "-precision", 6);

        final Problem problem = Problems.createProblemWithConstraints(problemId, lowerBounds, upperBounds);

        final Random random = randomSeed == -1 ? new Random() : new Random(randomSeed);

        final Population population = ProblemsKt.createRandomPopulation(problem, populationSize, random);

        final MainTask task = new MainTask(maxIterations, population,
                amplification, crossoverProbability, splitCount, problem, precision);

        final long nanoTime = System.nanoTime();

        Patterns.ask(taskActorRef, task, 10000).transform(
                value -> (MainResult) value,
                error -> error, system.dispatcher()).onComplete(new OnComplete<MainResult>() {
            @Override
            public void onComplete(Throwable failure, MainResult success) throws Throwable {
                sCanProcessInput = true;
                if (failure == null) {
                    onCompleted(system, problemId, success, logger, task, System.nanoTime() - nanoTime);
                } else {
                    onFailure(system, failure);
                }
            }
        }, system.dispatcher());
    }

    private static void onCompleted(ActorSystem system, int taskId, MainResult result, LoggingAdapter logger,
                                    MainTask task, long timeConsumed) {
        final NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(task.getPrecision());

//        Path file = Paths.get("task#" + taskId + ".csv");
//        final Member averageMember = Problems.calculateAverageMember(result.getPopulation());
//        final String formattedAverage = Util.prettyFloatArray(averageMember.toArray(), format);
//        final String dump = taskId + ";" + formattedAverage + ";" + result.getIterationsCount();
//        try {
//            Files.write(file, Collections.singletonList(dump), Charset.forName("UTF-8"));
//        } catch (IOException e) {
//            logger.error(e, "Failed to dump task result!");
//        }

        logger.info("Completed due: {}", result.getType());
        logger.info("Result iterations: {}", result.getIterationsCount());
//        logger.info("Result population average: {}", formattedAverage);
//        logger.info("Average value: {}", task.getProblem().calculate(averageMember));
        logger.info("Time consumed: {}", Util.prettyNumber(timeConsumed / 1000000000.0, format));
    }

    private static void onFailure(ActorSystem system, Throwable failure) {
        system.log().error(failure, "Failed due {}", failure);
    }
}
