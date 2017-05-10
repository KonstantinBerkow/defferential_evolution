package me.berkow.diffeval;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Created by konstantinberkow on 5/10/17.
 */
public class DEBackendMain {

    public static void main(String[] args) {
        // Override the configuration of the port when specified as program argument
        final String port = args.length > 0 ? args[0] : "0";
        final Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
                withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]")).
                withFallback(ConfigFactory.load());

        ActorSystem system = ActorSystem.create("DifferentialEvolution", config);

        system.actorOf(Props.create(DECalculationActor.class, port), "backend");
    }
}
