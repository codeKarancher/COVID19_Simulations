package uk.ac.cam.ks920;

import java.awt.*;

public class SimulationRunner {

    public static void main(String[] args) throws Exception {
        int InitialPopulation = 2000;//This is the population before communities are isolated
        int numCommunities = 40;

        //How much people stay home proportional to the effects of COVID19 on the population (e.g # community infections)
        double homelinessFactor = 0.5;

        //Probability that upon getting infected a person decides to head home
        double probOfGoingHomeIfSick = 0.3;

        int sandBoxSideLength = 500;//Side length of sandbox in metres
        int timestep = 1152;//milliseconds per day
        int movestep = 2;//milliseconds per movement
        //currently (timestep = 576 * movestep) means that people move every 15s

        //For members taking outings every day, the movestep range in which such outings are planned:
        double percentageAwakeTime = 0.5;
        double percentageAwakeTimeOuting = 0.5;//The maximum percentage of the awake part of each day that members can spend on outings
        int OutingMovestepLB = (int)(((timestep / movestep) * percentageAwakeTime) * (1 - percentageAwakeTimeOuting) / 2);
        int OutingMovestepUB = (int)((timestep / movestep) * percentageAwakeTime) - OutingMovestepLB;

        boolean AreMembersSafeAtHome = true;//Each member is a household, so if an entire household is at home, it is safe.
        //This model could in future be changed so each member is a person, in which case this variable may be set to false.

        COVID19_Population Population = new COVID19_Population(
                InitialPopulation, numCommunities,
                probOfGoingHomeIfSick,
                sandBoxSideLength,
                false,
                timestep/movestep);
        COVID19_Plotter Plotter = new COVID19_Plotter(Population);
        Population.InitializeInfection(1);
        MyWindow simWindow = new MyWindow(Population, Plotter, 600);

        //Show just communities first
        simWindow.PaintAllCommunities(Color.ORANGE);
        try { Thread.sleep(2000); }//viewing delay
        catch(InterruptedException ignored) { }

        int count = 0;
        while (Population.GlobalStateCounters[1] > 0) {
            if (count % (timestep/movestep) == 0) { //one day has passed
                count = count % (timestep/movestep);
                Plotter.IncrementNumDaysPassed();
                Population.DailyOutingPlanner(OutingMovestepLB, OutingMovestepUB);
            }

            //Population behaviour
            Population.MemberMovestepIncrement();
            Population.GenerateRandomVelocities(10);
            Population.AddHomewardVelocities_AtEndOfOuting(1);
            Population.AddHomewardVelocities_BasedOnCommunityInfections(1.5, homelinessFactor);
            //Population.AddHomewardVelocities_BasedOnCommunityDeaths(1.5, homelinessFactor);
            Population.AddHomewardVelocities_IfInfected(1.5, homelinessFactor);
            Population.MoveAll();

            Population.ExecuteTransmissionStepOnce(!AreMembersSafeAtHome);

            //animation delay
            try { Thread.sleep(movestep); }
            catch(InterruptedException ignored) { }

            Population.DailyHealChecker();
            Plotter.TakeReading();
            simWindow.Update();

            count++;
        }
        simWindow.Update();
    }

}
