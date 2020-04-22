package uk.ac.cam.ks920;

import java.util.*;

public class COVID19_Population {

    public class MyCommunity {
        public final double x;
        public final double y;
        public final double radius;
        private int Population = 0;
        public int[] StateCounters;

        MyCommunity(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        public void SetPopulationTo(int n) {
            Population = n;
        }

        public void IncrementPopulation() {
            Population++;
        }

        public int GetPopulation() {
            return Population;
        }

        public void InitializeStateCounters() {
            StateCounters = new int[] {Population, 0, 0, 0};
        }

        public double ProportionInfected() {
            return (double)StateCounters[1]/Population;
        }

        public double ProportionDeceased() {
            return (double)StateCounters[3]/Population;
        }

        public MyCommunity myclone() {
            MyCommunity toreturn = new MyCommunity(x, y, radius);
            toreturn.SetPopulationTo(Population);
            return toreturn;
        }
    }

    public ArrayList<CommunityMember> CommunityMembers;
    private ArrayList<Random> RVs;
    private HashMap<Member, Random> dict_RVs;
    private HashMap<Member, Random> health_RVs;
    int[] GlobalStateCounters;
    /* For the state counters above:
    [0]: Number of normal ppl
    [1]: Number of infected ppl
    [2]: Number of healed ppl
    [3]: Number of deceased ppl
     */
    public final int Capacity;
    private ArrayList<MyCommunity> Communities;
    public final int numCommunities;
    public final int sideLength;//in metres
    public final int numMovesPerDay;

    public COVID19_Population(int Capacity, int numCommunities, double probOfGoingHomeIfSick, int SideLengthInMetres, boolean InitializeImmediately, int numMovesPerDay) {
        CommunityMembers = new ArrayList<CommunityMember>(Capacity);
        Communities = new ArrayList<MyCommunity>(numCommunities);
        this.numCommunities = numCommunities;
        sideLength = SideLengthInMetres;
        this.numMovesPerDay = numMovesPerDay;
        InitializeMembersAndCommunities(Capacity, probOfGoingHomeIfSick);
        this.Capacity = CommunityMembers.size();
        RVs = new ArrayList<Random>(this.Capacity);
        dict_RVs = new HashMap<Member, Random>(this.Capacity);
        health_RVs = new HashMap<Member, Random>(this.Capacity);
        ReinitializeRVs();
        GlobalStateCounters = new int[]{this.Capacity, 0, 0, 0};
        if (InitializeImmediately)
            InitializeInfection(1);
    }

    public void InitializeCommunityLocations() {
        Random X = new Random();
        Random Y = new Random();
        double comRadius = Math.pow((Math.pow(sideLength,2)/numCommunities)/Math.PI, 0.5);//Divides sandbox area by number of communities

        for (int i = 0; i < numCommunities; i++) {
            Communities.add(new MyCommunity(
                    (int)(comRadius + X.nextDouble() * (sideLength - 2 * comRadius)),
                    (int)(comRadius + Y.nextDouble() * (sideLength - 2 * comRadius)),
                    comRadius));
        }
    }

    public void InitializeCommunityMemberAssignment() {
        ArrayList<CommunityMember> ToRemove = new ArrayList<CommunityMember>();
        for (CommunityMember m : CommunityMembers) {
            int comCount = 0;
            while (!m.IsAssignedToCommunity() && comCount < numCommunities) {
                MyCommunity currentCom = Communities.get(comCount);
                if (m.DistanceTo(currentCom.x, currentCom.y) <= currentCom.radius) {
                    m.SetCommunity(Communities.get(comCount), comCount);
                    Communities.get(comCount).IncrementPopulation();
                }
                comCount++;
            }
            if (comCount == numCommunities)//discard member
                ToRemove.add(m);
        }
        for (CommunityMember m : ToRemove) {
            CommunityMembers.remove(m);
        }
    }

    public ArrayList<MyCommunity> GetCommunities() {
        ArrayList<MyCommunity> CommunitiesClone = new ArrayList<MyCommunity>(numCommunities);
        for (MyCommunity c : Communities) {
            CommunitiesClone.add(c.myclone());
        }
        return CommunitiesClone;
    }

    public void InitializeMembersAndCommunities(int InitialPopulationSize, double probOfGoingHomeIfSick) {
        Random rand = new Random();
        Random sickrand = new Random();
        if (CommunityMembers.size() == 0) {
            for (int i = 0; i < InitialPopulationSize; i++) {
                CommunityMember m = new CommunityMember((int)(rand.nextDouble() * sideLength), (int)(rand.nextDouble() * sideLength),
                        (sickrand.nextDouble() <= probOfGoingHomeIfSick));
                CommunityMembers.add(m);
            }
        }
        else {
            for (Member m : CommunityMembers) {
                m = new Member((int) (rand.nextDouble() * sideLength), (int) (rand.nextDouble() * sideLength),
                        (sickrand.nextDouble() <= probOfGoingHomeIfSick));
            }
        }
        InitializeCommunityLocations();
        InitializeCommunityMemberAssignment();
        for (int i = 0; i < numCommunities; i++)
            Communities.get(i).InitializeStateCounters();
        ResetOriginalCoordinates();
    }

    public void InitializeInfection(double ProportionToInfect) {
        int numToInfect = (int)(Capacity * ProportionToInfect);
        for (int i = 0; i < numToInfect; i++) {
            CommunityMembers.get(i).ContractCOVID();
            GlobalStateCounters[0] -= 1;
            GlobalStateCounters[1] += 1;
        }
    }

    public void InitializeInfection(int numToInfect) {
        for (int i = 0; i < numToInfect; i++) {
            CommunityMembers.get(i).ContractCOVID();
            GlobalStateCounters[0] -= 1;
            GlobalStateCounters[1] += 1;
        }
    }

    public void ReinitializeRVs() {
        RVs = new ArrayList<Random>(Capacity);
        dict_RVs.clear();
        health_RVs.clear();
        for (CommunityMember m : CommunityMembers) {
            Random random = new Random();
            RVs.add(random);
            dict_RVs.put(m, random);

            Random healthrand = new Random();
            health_RVs.put(m, healthrand);
        }
    }

    private void ResetOriginalCoordinates() {
        for (Member m : CommunityMembers) {
            m.SetNewOriginalCoordinates(m.GetX(), m.GetY());
        }
    }

    private void MoveDead(Member m) {
        m.vx = m.vy = 0;
    }

    public void GenerateRandomVelocities(double MaxMetresMovedPerMovestep) {
        for (Member m : CommunityMembers) {
            //Generate random vector velocity
            m.vx = (2 * (dict_RVs.get(m).nextDouble() - 0.5)) * MaxMetresMovedPerMovestep;
            m.vy = ((dict_RVs.get(m).nextBoolean()) ? -1 : 1)
                    * Math.sqrt(Math.pow(MaxMetresMovedPerMovestep, 2) - Math.pow(m.vx, 2));

            //Bounce off edges:
            if (m.GetX() <= 0)
                m.vx = Math.abs(m.vx);
            else if (m.GetX() >= sideLength)
                m.vx = -Math.abs(m.vx);
            if (m.GetY() <= 0)
                m.vy = Math.abs(m.vy);
            else if (m.GetY() >= sideLength)
                m.vy = -Math.abs(m.vy);
        }
    }

    public void AddHomewardVelocities_AtEndOfOuting(double MaxMetresMovedPerMovestep) {
        for (Member m : CommunityMembers) {
            if (!m.IsOuting())
                continue;
            m.vx += MaxMetresMovedPerMovestep * m.UrgencyOfReturningFromOuting() * (m.GetX0() - m.GetX());
            m.vy += MaxMetresMovedPerMovestep * m.UrgencyOfReturningFromOuting() * (m.GetY0() - m.GetY());
        }
    }

    public void AddHomewardVelocities_BasedOnCommunityInfections(double MaxMetresMovedPerMovestep, double homeliness) throws Exception {
        for (CommunityMember m : CommunityMembers) {
            //Homeward vector
            double proportionInfectedInCom = m.GetCommunity().ProportionInfected();
            double home_vx = MaxMetresMovedPerMovestep * homeliness * proportionInfectedInCom * (m.GetX0() - m.GetX());
            double home_vy = MaxMetresMovedPerMovestep * homeliness * proportionInfectedInCom * (m.GetY0() - m.GetY());
            //Vector addition
            m.vx += home_vx;
            m.vy += home_vy;
        }
    }

    public void AddHomewardVelocities_BasedOnCommunityDeaths(double MaxMetresMovedPerMovestep, double homeliness) throws Exception {
        for (CommunityMember m : CommunityMembers) {
            //Homeward vector
            double proportionInfectedInCom = m.GetCommunity().ProportionDeceased();
            double home_vx = MaxMetresMovedPerMovestep * homeliness * proportionInfectedInCom * (m.GetX0() - m.GetX());
            double home_vy = MaxMetresMovedPerMovestep * homeliness * proportionInfectedInCom * (m.GetY0() - m.GetY());
            //Vector addition
            m.vx += home_vx;
            m.vy += home_vy;
        }
    }

    public void AddHomewardVelocities_IfInfected(double MaxMetresMovedPerMovestep, double homeliness) throws Exception {
        for (Member m : CommunityMembers) {
            if (m.WillIsolateIfSick) {
                double sick_vx = MaxMetresMovedPerMovestep * homeliness * (m.GetX0() - m.GetX());
                double sick_vy = MaxMetresMovedPerMovestep * homeliness * (m.GetY0() - m.GetY());
                m.vx += sick_vx;
                m.vy += sick_vy;
            }
        }
    }

    public void MoveAll() {
        for (Member m: CommunityMembers) {
            if (m.IsDeceased())
                MoveDead(m);
            m.VectorMoveIfOuting();
            m.MoveBackHomeAtEndOfOuting();
        }
    }

    public void DailyHealChecker() {
        IncrementTCs();
        for (Member m : CommunityMembers) {
            if (m.GetTC() >= COVID19.MinDaysSick) { //Is COVID19 carrier who can begin healing
                double reading = health_RVs.get(m).nextDouble();
                double probHeal = 1 / (numMovesPerDay * (double)(COVID19.AvgDaysSick - COVID19.MinDaysSick));
                double probDeath = m.MortalityRate / (numMovesPerDay * (double)(COVID19.AvgDaysBeforeDeath - COVID19.MinDaysAlive));
                if (reading <= probHeal) { //Geometric Distribution
                    m.Heal();//Healed Member
                    GlobalStateCounters[1] -= 1;
                    GlobalStateCounters[2] += 1;
                }
                else if (reading <= probHeal + probDeath) { //Geometric Distribution
                    m.Deceased();//Deceased
                    GlobalStateCounters[1] -= 1;
                    GlobalStateCounters[3] += 1;
                }
            }
        }
    }

    public void DailyOutingPlanner(int movestepLB, int movestepUB) throws OutingOutOfMovestepBoundException {
        if (movestepLB < 0 || movestepUB > numMovesPerDay)
            throw new OutingOutOfMovestepBoundException(movestepLB, movestepUB, numMovesPerDay);
        for (Member m : CommunityMembers) {
            m.ResetMovestepCounter();
            Random memberRand = dict_RVs.get(m);
            int[] outingTimes = new int[] {
                    movestepLB + memberRand.nextInt(movestepUB - movestepLB),
                    movestepLB + memberRand.nextInt(movestepUB - movestepLB) };
            Arrays.sort(outingTimes);
            m.SetOutingTime(outingTimes[0], outingTimes[1]);
        }
    }

    public void MemberMovestepIncrement() {
        for (Member m : CommunityMembers)
            m.IncrMovestepCounter();
    }

    private void IncrementTCs() {
        for (Member m : CommunityMembers) {
            m.IncrementTC();
        }
    }

    public void ExecuteTransmissionStepOnce(boolean MembersCanGetInfectedAtHome) {
        Random rand = new Random();
        ArrayList<Member> newlyinfected = new ArrayList<Member>();
        for (Member m : CommunityMembers) {
            if (m.GetTC() >= 0) { //Sick member identified
                for (Member n : CommunityMembers) {
                    if (n.equals(m) || n.HasImmunity())
                        continue;
                    double d = m.DistanceTo(n);
                    if (d <= 2 * COVID19.SpreadRadius && //n is within spreading radius of m
                            rand.nextDouble() <= COVID19.ProbabilityOfSpread && //randomness of meeting between m and n
                            (n.IsOuting() || MembersCanGetInfectedAtHome) && //n may be safe at home?
                            !newlyinfected.contains(n)) { //Transmit
                        newlyinfected.add(n);
                        GlobalStateCounters[0] -= 1;
                        GlobalStateCounters[1] += 1;
                    }
                }
            }
        }
        for (Member n : newlyinfected) {
            n.ContractCOVID();
        }
    }
}

