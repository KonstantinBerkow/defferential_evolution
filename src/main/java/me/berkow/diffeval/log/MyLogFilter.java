package me.berkow.diffeval.log;

import akka.actor.ActorSystem;
import akka.event.EventStream;

/**
 * Created by konstantinberkow on 6/6/17.
 */
public class MyLogFilter extends akka.event.slf4j.Slf4jLoggingFilter {
    public MyLogFilter(ActorSystem.Settings settings, EventStream eventStream) {
        super(settings, eventStream);
    }

    @Override
    public boolean isErrorEnabled(Class<?> logClass, String logSource) {
        return true;
    }

    @Override
    public boolean isWarningEnabled(Class<?> logClass, String logSource) {
        return true;
    }

    @Override
    public boolean isInfoEnabled(Class<?> logClass, String logSource) {
        return true;
    }

    @Override
    public boolean isDebugEnabled(Class<?> logClass, String logSource) {
        return false;
    }
}
