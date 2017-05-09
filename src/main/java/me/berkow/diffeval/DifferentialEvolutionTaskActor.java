package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.dispatch.Futures;
import akka.japi.pf.FI;
import akka.pattern.Patterns;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.Callable;

/**
 * Created by konstantinberkow on 5/8/17.
 */
public class DifferentialEvolutionTaskActor extends AbstractActor {

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(MainDETask.class, new FI.UnitApply<MainDETask>() {
                    public void apply(MainDETask task) throws Exception {
                        final Future<MainResult> resultFuture = createComputeFuture(task);
                        Patterns.pipe(resultFuture, context().dispatcher())
                                .to(sender(), self());

                    }
                })
                .build();
    }

    private Future<MainResult> createComputeFuture(final MainDETask deJob) {
        return Futures.future(new Callable<MainResult>() {
            public MainResult call() throws Exception {
                return doCompute(deJob);
            }
        }, ExecutionContext.Implicits$.MODULE$.global());
    }

    private MainResult doCompute(MainDETask task) {
        context().system().log().debug("Future thread: {}, task: {}", Thread.currentThread(), task);



        return new MainResult(task.getProblemId(), new double[task.getProblemSize()]);
    }
}
