package uk.ac.cam.ks920;

import java.util.ArrayList;

public class COVID19_Plotter {

    private COVID19_Population pop;
    private ArrayList<int[]> Data;
    private int numDaysPassed = 0;

    public COVID19_Plotter(COVID19_Population pop) {
        this.pop = pop;
        Data = new ArrayList<int[]>();
    }

    public void TakeReading() {
        Data.add(pop.GlobalStateCounters.clone());
    }

    public void IncrementNumDaysPassed() {
        numDaysPassed++;
    }

    public int GetNumDaysPassed() {
        return numDaysPassed;
    }

    public ArrayList<int[]> GetData() {
        ArrayList<int[]> DataClone = new ArrayList<int[]>();
        for (int[] iar : Data) {
            DataClone.add(iar.clone());
        }
        return DataClone;
    }

    public COVID19_Population GetPopulation() {
        return pop;
    }
}
