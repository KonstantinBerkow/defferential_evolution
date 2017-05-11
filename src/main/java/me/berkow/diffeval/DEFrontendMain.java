package me.berkow.diffeval;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import me.berkow.diffeval.message.MainDEResult;
import me.berkow.diffeval.message.MainDETask;
import me.berkow.diffeval.problem.Problem;
import me.berkow.diffeval.problem.Problems;
import scala.Function1;
import scala.concurrent.duration.FiniteDuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by konstantinberkow on 5/10/17.
 */
public class DEFrontendMain {
    private static final double PRECISION = 1E-6;

    private static int sCurrentIteration = 0;
    private static int sStaleIterationsCount = 0;
    private static double sPrevius = Double.NaN;

    public static void main(String[] args) {
        // Override the configuration of the port when specified as program argument
        final String port = args.length > 0 ? args[0] : "0";

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

        system.scheduler().scheduleOnce(FiniteDuration.apply(10, TimeUnit.SECONDS), new Runnable() {
            @Override
            public void run() {
                final Problem problem6 = Problems.createProblemWithConstraints(6,
                        new double[]{-1.28, -1.28, -1.28, -1.28},
                        new double[]{1.28, 1.28, 1.28, 1.28}
                );

                final List<double[]> population = Problems.createRandomPopulation(40, problem6, new Random());

                final MainDETask task = new MainDETask(100, population,
                        0.9F, 0.5F, 4, problem6);

                process(task, system, taskActorRef);
            }
        }, system.dispatcher());
    }

    private static void process(MainDETask task, final ActorSystem system, final ActorRef taskActorRef) {
        Patterns.ask(taskActorRef, task, 10000).transform(new Function1<Object, MainDEResult>() {
            @Override
            public MainDEResult apply(Object v1) {
                return (MainDEResult) v1;
            }
        }, new Function1<Throwable, Throwable>() {
            @Override
            public Throwable apply(Throwable error) {
                return error;
            }
        }, system.dispatcher())
                .onComplete(new OnComplete<MainDEResult>() {
                    @Override
                    public void onComplete(Throwable failure, MainDEResult success) throws Throwable {
                        if (failure != null) {
                            onFailure(system, failure);
                        } else {
                            onResult(system, success, taskActorRef);
                        }
                    }
                }, system.dispatcher());
    }

    private static void onResult(ActorSystem system, MainDEResult result, ActorRef actor) {
        sCurrentIteration++;

        if (Math.abs(result.value() - sPrevius) < PRECISION) {
            sStaleIterationsCount++;
        } else {
            sStaleIterationsCount = 0;
            sPrevius = result.value();
        }

        if (sStaleIterationsCount >= 10) {
            onCompleted(system, result, "stale");
            return;
        }

        if (sCurrentIteration >= 100) {
            onCompleted(system, result, "max_iterations");
            return;
        }

        final MainDETask newTask = new MainDETask(100, result.getPopulation(),
                result.getAmplification(), result.getConvergence(), 4, result.getProblem());
        process(newTask, system, actor);
    }

    private static void onFailure(ActorSystem system, Throwable failure) {
        system.log().error(failure, "Failed!");
    }

    private static void onCompleted(ActorSystem system, MainDEResult result, String type) {
        system.log().info("Completed due: {}", type);
        system.log().info("result: {}", result);
    }
}
