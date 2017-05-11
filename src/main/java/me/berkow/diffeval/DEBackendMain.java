package me.berkow.diffeval;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by konstantinberkow on 5/10/17.
 */
public class DEBackendMain {

    public static void main(String[] args) {
        // Override the configuration of the port and ip when specified as program argument
        final String hostname = args.length > 0 ? args[0] : null;
        final String port = args.length > 1 ? args[1] : "0";

        Map<String, Object> map = new HashMap<>();
        if (hostname != null) {
            map.put("akka.remote.netty.tcp.hostname", hostname);
            map.put("akka.cluster.seed-nodes", Arrays.asList(
                    "akka.tcp://DifferentialEvolution@" + hostname + ":2552",
                    "akka.tcp://DifferentialEvolution@" + hostname + ":2553"
            ));
        }
        map.put("akka.remote.netty.tcp.port", port);

        Config config = ConfigFactory.parseMap(map)
                .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
                .withFallback(ConfigFactory.load());

        ActorSystem system = ActorSystem.create("DifferentialEvolution", config);

        system.actorOf(Props.create(DECalculationActor.class, port), "backend");
    }
}
