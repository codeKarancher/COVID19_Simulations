package uk.ac.cam.ks920;

public class Member {
    private double x, y;//coordinates in metres
    private double x0, y0;//original coordinates in metres
    public double vx;
    public double vy;
    private int TimeContracted = -1;
    private boolean Deceased = false;
    public double MortalityRate = 0.01;
    public final boolean WillIsolateIfSick;
    private int movestepCounter = 0;
    private int outingStart, outingEnd;

    public Member(int x, int y, boolean willIsolateIfSick) {
        this.x = x;
        this.y = y;
        vx = 0;
        vy = 0;
        WillIsolateIfSick = willIsolateIfSick;
    }

    public double GetX() {return x;}
    public double GetY() {return y;}
    public double GetX0() {return x0;}
    public double GetY0() {return y0;}
    public int GetTC() {return TimeContracted;}
    public void ContractCOVID() {
        TimeContracted = 0;
    }
    public boolean IsInfected() { return (TimeContracted >= 0);}
    public void IncrementTC() {
        if (TimeContracted >= 0)
            TimeContracted += 1;
    }
    public void Heal() {
        TimeContracted = -2;
    }
    public boolean IsDeceased() {return Deceased;}
    public boolean IsHealed() { return TimeContracted == -2;}
    public double GetMR() { return MortalityRate;}

    public boolean HasImmunity() {
        return (TimeContracted >= 0 || IsHealed() || IsDeceased());
    }

    public void VectorMoveIfOuting() {
        if (IsOuting()) {
            x += vx;
            y += vy;
        }
    }

    public void MoveBackHomeAtEndOfOuting() {
        if (movestepCounter == outingEnd) {
            x = x0;
            y = y0;
        }
    }

    public void Move(int newx, int newy) {
        x = newx;
        y = newy;
    }

    public void SetNewOriginalCoordinates(double newx0, double newy0) {
        x0 = newx0;
        y0 = newy0;
    }

    public double DistanceTo(Member m) {
        return Math.sqrt(Math.pow((x - m.GetX()), 2) + Math.pow((y - m.GetY()), 2));
    }

    public double DistanceTo(double x, double y) {
        return Math.sqrt(Math.pow(x - this.x, 2) + Math.pow(y - this.y, 2));
    }

    public void Deceased() {
        Deceased = true;
        TimeContracted = -3;
    }

    public boolean IsOuting() { return (movestepCounter >= outingStart && movestepCounter <= outingEnd); }

    public void SetOutingTime(int i1, int i2) {
        outingStart = i1;
        outingEnd = i2;
    }

    public void ResetMovestepCounter() {
        movestepCounter = 0;
    }

    public void IncrMovestepCounter() {
        movestepCounter++;
    }

    public double UrgencyOfReturningFromOuting() {
        return 1 + (double)(movestepCounter - outingEnd) / (outingEnd - outingStart); //Linear model
    }
}

