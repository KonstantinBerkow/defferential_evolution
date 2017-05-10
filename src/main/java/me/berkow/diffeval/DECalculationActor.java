package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.japi.pf.FI;

/**
 * Created by konstantinberkow on 5/10/17.
 */
public class DECalculationActor extends AbstractActor {

    private final String port;
    private Cluster cluster;

    public DECalculationActor(String port) {
        this.port = port;
    }

    @Override
    public void preStart() throws Exception {
        cluster = Cluster.get(getContext().system());
        cluster.subscribe(self(), ClusterEvent.MemberUp.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConcreteDETask.class, new FI.UnitApply<ConcreteDETask>() {
                    @Override
                    public void apply(ConcreteDETask task) throws Exception {
                        calculate(task);
                    }
                })
                .match(ClusterEvent.CurrentClusterState.class, new FI.UnitApply<ClusterEvent.CurrentClusterState>() {
                    @Override
                    public void apply(ClusterEvent.CurrentClusterState state) throws Exception {
                        for (Member member : state.getMembers()) {
                            if (member.status().equals(MemberStatus.up())) {
                                register(member);
                            }
                        }
                    }
                })
                .match(ClusterEvent.MemberUp.class, new FI.UnitApply<ClusterEvent.MemberUp>() {
                    @Override
                    public void apply(ClusterEvent.MemberUp mUp) throws Exception {
                        register(mUp.member());
                    }
                })
                .build();
    }

    @Override
    public void postStop() {
        cluster.unsubscribe(self());
    }

    private void register(Member member) {
        if (member.hasRole("frontend")) {
            context().actorSelection(member.address() + "/user/frontend").tell(DifferentialEvolutionTaskActor.BACKEND_REGISTRATION, self());
        }
    }

    private void calculate(ConcreteDETask task) {
        context().system().log().debug("Calculate task: {}, by: {}", task, this);
        final ConcreteDEResult result = new ConcreteDEResult();
        sender().tell(result, self());
    }

    @Override
    public String toString() {
        return "DECalculationActor{" +
                "port='" + port + '\'' +
                ", cluster=" + cluster +
                '}';
    }
}
