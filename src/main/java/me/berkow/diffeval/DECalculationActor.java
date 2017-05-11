package me.berkow.diffeval;

import akka.actor.AbstractActor;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.japi.pf.FI;
import me.berkow.diffeval.message.DEResult;
import me.berkow.diffeval.message.DETask;

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
        final ActorSystem system = context().system();

        cluster = Cluster.get(system);
        cluster.subscribe(self(), ClusterEvent.MemberUp.class);

        system.log().debug("{} pre start!", this);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(DETask.class, new FI.UnitApply<DETask>() {
                    @Override
                    public void apply(DETask task) throws Exception {
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
            context().system().log().debug("{} try to register {}", this, member);
            context().actorSelection(member.address() + "/user/frontend").tell(DETaskActor.BACKEND_REGISTRATION, self());
        }
    }

    private void calculate(DETask task) {
        context().system().log().debug("Calculate task: {}, by: {}", task, this);
        final DEResult result = new DEResult();
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
