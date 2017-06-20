package me.berkow.diffeval;

import me.berkow.diffeval.problem.Problems;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by konstantinberkow on 6/18/17.
 */
public class TestProblems {

    @Test
    public void testProblem3() {
        Assert.assertEquals(
                0,
                Problems.calculateProblem3(new float[]{0, 0, 0, 0}),
                0.0001
        );

        Assert.assertEquals(
                20,
                Problems.calculateProblem3(new float[]{1, 2, 3, 4}),
                0.0001
        );

        Assert.assertEquals(
                -10,
                Problems.calculateProblem3(new float[]{-1, -1, -1, -1}),
                0.0001
        );
    }

    @Test
    public void testProblem7() {
        Assert.assertEquals(
                0,
                Problems.calculateProblem7(new float[]{0, 0, 0, 0}),
                0.0001
        );

        Assert.assertEquals(
                418.98287762 * 8,
                Problems.calculateProblem7(new float[]{420.96F, 420.96F, 420.96F, 420.96F, 420.96F, 420.96F, 420.96F, 420.96F}),
                0.0001
        );

        Assert.assertEquals(
                -418.98287762 * 8,
                Problems.calculateProblem7(new float[]{-420.96F, -420.96F, -420.96F, -420.96F, -420.96F, -420.96F, -420.96F, -420.96F}),
                0.0001
        );
    }

    @Test
    public void testProblem10() {
        Assert.assertEquals(
                0,
                Problems.calculateProblem10(new float[]{0, 0, 0, 0}),
                0.0001
        );
    }

    @Test
    public void testProblem12() {
        Assert.assertEquals(
                0,
                Problems.calculateProblem12(new float[]{0, 0, 0, 0}),
                0.0001
        );
    }
}
