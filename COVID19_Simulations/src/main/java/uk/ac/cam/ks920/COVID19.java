package uk.ac.cam.ks920;

public class COVID19 {
    /* Each pixel is half a metre
     * Each 'daystep' is a day
     * Each 'timestep' is an hour
     * */
    public static final int SpreadRadius = 2;
    public static final int MinDaysSick = 5;
    public static final int AvgDaysSick = 14;
    public static final int MinDaysAlive = 10;
    public static final int AvgDaysBeforeDeath = 20;
    public static final double ProbabilityOfSpread = 0.1;
}
