package me.berkow.diffeval;

import me.berkow.diffeval.problem.Problems;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by konstantinberkow on 6/18/17.
 */
public class TestProblems {

    @Test
    public void testProblem1() {
        Assert.assertEquals(
                0,
                Problems.calculateProblem1(new float[]{0, 0, 0, 0}),
                0.0001
        );
    }

    @Test
    public void testProblem2() {
        Assert.assertEquals(
                0,
                Problems.calculateProblem2(new float[]{0, 0, 0, 0}),
                0.0001
        );
    }

    @Test
    public void testProblem3() {
        Assert.assertEquals(
                0,
                Problems.calculateProblem3(new float[]{0, 0, 0, 0}),
                0.0001
        );
    }

    @Test
    public void testProblem4() {
        Assert.assertEquals(
                0,
                Problems.calculateProblem4(new float[]{0, 0, 0, 0}),
                0.0001
        );
    }

    @Test
    public void testProblem5() {
//        Assert.assertEquals(
//                0,
//                Problems.calculateProblem5(new float[]{1, 1, 1, 1}),
//                0.0001
//        );
//
//        Assert.assertEquals(
//                0,
//                Problems.calculateProblem5(new float[]{-30, -30, -30}),
//                0.0001
//        );
    }

    @Test
    public void testProblem6() {
        Assert.assertEquals(
                0,
                Problems.calculateProblem6(new float[]{0, 0, 0, 0}),
                0.0001
        );
    }

//    @Test
//    public void testProblem7() {
//        Assert.assertEquals(
//                0,
//                Problems.calculateProblem7(new float[]{0, 0, 0, 0}),
//                0.0001
//        );
//
//        Assert.assertEquals(
//                418.98287762 * 8,
//                Problems.calculateProblem7(new float[]{420.96F, 420.96F, 420.96F, 420.96F, 420.96F, 420.96F, 420.96F, 420.96F}),
//                0.0001
//        );
//
//        Assert.assertEquals(
//                -418.98287762 * 8,
//                Problems.calculateProblem7(new float[]{-420.96F, -420.96F, -420.96F, -420.96F, -420.96F, -420.96F, -420.96F, -420.96F}),
//                0.0001
//        );
//    }
//
//    @Test
//    public void testProblem10() {
//        Assert.assertEquals(
//                0,
//                Problems.calculateProblem10(new float[]{0, 0, 0, 0}),
//                0.0001
//        );
//    }
//
//    @Test
//    public void testProblem11() {
//        Assert.assertEquals(
//                0,
//                Problems.calculateProblem11(new float[]{1, 1, 1, 1}),
//                0.0001
//        );
//    }
//
//    @Test
//    public void testPenaltyFunction() {
//        Assert.assertEquals(
//                100,
//                Problems.penalty(11, 10, 100, 4),
//                0.0001
//        );
//
//        Assert.assertEquals(
//                100,
//                Problems.penalty(-11, 10, 100, 4),
//                0.0001
//        );
//    }
//
//    @Test
//    public void testProblem12() {
//        Assert.assertEquals(
//                0,
//                Problems.calculateProblem12(new float[]{0, 0, 0, 0}),
//                0.0001
//        );
//    }
}
