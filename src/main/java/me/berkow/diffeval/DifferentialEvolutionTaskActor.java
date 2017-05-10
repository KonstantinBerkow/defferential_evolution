package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.japi.Function;
import akka.japi.pf.FI;
import akka.pattern.Patterns;
import scala.Function1;
import scala.concurrent.Future;
import scala.util.Try;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by konstantinberkow on 5/8/17.
 */
public class DifferentialEvolutionTaskActor extends AbstractActor {
    public static final String BACKEND_REGISTRATION = "register";

    private final String port;
    private final List<ActorRef> backends;

    public DifferentialEvolutionTaskActor(String port) {
        this.port = port;
        backends = new ArrayList<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MainDETask.class, new FI.TypedPredicate<MainDETask>() {
                    @Override
                    public boolean defined(MainDETask task) {
                        return backends.isEmpty();
                    }
                }, new FI.UnitApply<MainDETask>() {
                    @Override
                    public void apply(MainDETask task) throws Exception {
                        TaskFailedMsg message = new TaskFailedMsg("Service unavailable, try again later", task);
                        sender().tell(message, sender());
                    }
                })
                .match(MainDETask.class, new FI.UnitApply<MainDETask>() {
                    @Override
                    public void apply(MainDETask task) throws Exception {
                        calculate(task);
                    }
                })
                .matchEquals(BACKEND_REGISTRATION, new FI.UnitApply<String>() {
                    @Override
                    public void apply(String $) throws Exception {
                        final ActorRef sender = sender();
                        context().watch(sender);
                        backends.add(sender);
                    }
                })
                .match(Terminated.class, new FI.UnitApply<Terminated>() {
                    @Override
                    public void apply(Terminated terminated) throws Exception {
                        backends.remove(terminated.getActor());
                    }
                })
                .build();
    }

    @Override
    public void postStop() throws Exception {
        context().system().log().debug("{} postStop", this);
        backends.clear(); //huh?
    }

    private void calculate(MainDETask task) {
        final ActorSystem system = context().system();

        system.log().debug("Calculate task: {}, by: {}", task, this);

        final AtomicInteger counter = new AtomicInteger();

        Future<Iterable<Object>> results = Futures.traverse(backends, new Function<ActorRef, Future<Object>>() {
            @Override
            public Future<Object> apply(ActorRef workerActorRef) throws Exception {
                ConcreteDETask deTask = new ConcreteDETask(counter.getAndIncrement());
                return Patterns.ask(workerActorRef, deTask, 10000);
            }
        }, system.dispatcher());

        Patterns.pipe(results, system.dispatcher()).to(sender(), self());
    }

    @Override
    public String toString() {
        return "DifferentialEvolutionTaskActor{" +
                "port='" + port + '\'' +
                ", backends=" + backends +
                '}';
    }
}
