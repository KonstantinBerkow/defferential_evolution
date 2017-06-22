package me.berkow.diffeval;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import me.berkow.diffeval.actor.WorkerActor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class DEBackendMain {

    public static void main(String[] args) {
        // Override the configuration of the port and ip when specified as program argument
        final String remoteHostname = args.length > 0 ? args[0] : null;
        final String port = args.length > 1 ? args[1] : "0";

        Map<String, Object> map = new HashMap<>();
        if (remoteHostname != null) {
            map.put("akka.cluster.seed-nodes", Arrays.asList(
                    "akka.tcp://DifferentialEvolution@" + remoteHostname + ":2552",
                    "akka.tcp://DifferentialEvolution@" + remoteHostname + ":2553"
            ));
        }
        map.put("akka.remote.netty.tcp.port", port);

        try {
            InetAddress localHost = InetAddress.getLocalHost();

            String hostAddress = localHost.getHostAddress();

            map.put("akka.remote.netty.tcp.hostname", hostAddress);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Config config = ConfigFactory.parseMap(map)
                .withFallback(ConfigFactory.parseString("akka.cluster.roles = [backend]"))
                .withFallback(ConfigFactory.load());

        ActorSystem system = ActorSystem.create("DifferentialEvolution", config);

        system.actorOf(Props.create(WorkerActor.class, port), "backend");
    }
}
