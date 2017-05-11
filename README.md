# differential_evolution
DE with Akka

# Arguments for DEFrontendMain:
* Mandatory:
1. -port - specify port on which Actor should listen (integer)
2. -lowerBounds - specify lower bounds for solution (decimals separated by ',')
3. -upperBounds - specify upper bounds for solution (decimals separated by ',')
* Optional:
1. -maxIterations - specify max iterations limit (integer)
2. -maxStale - specify max "stable" iterations (changes of solution value lower then precision) limit (integer)
3. -problemId - specify id of calculation task (from 1 to 10)
4. -populationSize - specify population size (integer)
5. -splitCount - specify quantity of different simultaneous calculations with different control parameters (integer)
6. -randomSeed - seed for random, used only for initial solution generation
7. -amplification - factor of mutation (double 0..2)
8. -crossover - crossover probability (double 0..1)

# Samples:
