package me.berkow.diffeval.util;

import akka.dispatch.Mapper;

/**
 * Created by konstantinberkow on 6/19/17.
 */
public class CastMapper<T, R> extends Mapper<T, R> {
    @Override
    public R checkedApply(T parameter) throws Throwable {
        return (R) parameter;
    }
}
