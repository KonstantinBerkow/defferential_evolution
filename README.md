# differential_evolution
DE with Akka

# Arguments for DEFrontendMain:
## Mandatory:
1. -port - specify port on which Actor should listen (integer)
2. -lowerBounds - specify lower bounds for solution (decimals separated by ',')
3. -upperBounds - specify upper bounds for solution (decimals separated by ',')
## Optional:
1. -maxIterations - specify max iterations limit (integer)
2. -maxStale - specify max "stable" iterations (changes of solution value lower then precision) limit (integer)
3. -problemId - specify id of calculation task (from 1 to 10)
4. -populationSize - specify population size (integer)
5. -splitCount - specify quantity of different simultaneous calculations with different control parameters (integer)
6. -randomSeed - seed for random, used only for initial solution generation
7. -amplification - factor of mutation (double 0..2)
8. -crossover - crossover probability (double 0..1)
9. -precision - calculations precision

# Samples:
## Backend nodes
Backend nodes performs calculation they must be started first, sample usage:

$ java -cp <path to jar> me.berkow.diffeval.DEBackendMain <hostname> <port number>

$ java -cp <path to jar> me.berkow.diffeval.DEBackendMain 196.168.0.108 2552

$ java -cp <path to jar> me.berkow.diffeval.DEBackendMain 196.168.0.108 2553

__Use at least 2552 or 2553 this ports are crucial for cluster__

##Frontend node
This node runs control specific calculation on cluster

$ java -cp <path to jar> me.berkow.diffeval.DEFrontendMain <args...>

Example for problem 5

$ java -cp <path to jar> me.berkow.diffeval.DEFrontendMain -port 2551 -problemId 5 -lowerBounds "-5.12, -5.12, -5.12, -5.12, -5.12, -5.12" -upperBounds "5.12, 5.12, 5.12, 5.12, 5.12, 5.12"

#You can run me.berkow.diffeval.Main to see action without arguments