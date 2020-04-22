# COVID19_Simulations
Simulating the spread of COVID-19 in a 2D sandbox universe

Each dot (instance of the Member class) represents a household in the universe.
The COVID19_Population class is responsible for treating a list of Members as a Population - it moves them in the universe and conducts the spread of the virus.
SimulationRunner is where the main method resides and contains values for parameters that change the way the population behaves. These can be tweaked to reproduce a variety of behaviour exhibited by the population.
The COVID19 class contains global constants that are specific to the COVID-19 virus in real life.
