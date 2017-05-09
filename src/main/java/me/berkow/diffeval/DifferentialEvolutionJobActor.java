package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.japi.pf.FI;

/**
 * Created by konstantinberkow on 5/8/17.
 */
public class DifferentialEvolutionJobActor extends AbstractActor {

    @Override
    public void preStart() throws Exception {
        super.preStart();
    }

    public Receive createReceive() {
        return receiveBuilder()
                .match(DEJob.class, new FI.UnitApply<DEJob>() {
                    public void apply(DEJob deJob) throws Exception {
                        final String result = compute(deJob);
                        sender().tell(result, self());
                    }
                })
                .build();
    }

    private String compute(DEJob deJob) {
        return "Done job #" + deJob.problemNum;
    }
}
