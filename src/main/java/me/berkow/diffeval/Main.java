package me.berkow.diffeval;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Future;

/**
 * Created by konstantinberkow on 4/26/17.
 */
public class Main {

    public static void main(String[] args) {
        final Config config = ConfigFactory.load("application.conf");

        final ActorSystem system = ActorSystem.create("DifferentialEvolution", config);

        final Props jobActorProps = Props.create(DifferentialEvolutionJobActor.class);

        final ActorRef jobActorRef = system.actorOf(jobActorProps, "JobActor");

        Future<Object> problemSolvage = Patterns.ask(jobActorRef, new DEJob(1), 10000L);

        problemSolvage.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable failure, Object success) throws Throwable {
                LoggingAdapter log = system.log();
                if (failure != null) {
                    log.error(failure, "Failed to complete task #1!");
                } else {
                    log.debug("Task #1 result: {}", success);
                }
                system.terminate();
            }
        }, system.dispatcher());
    }
}
