package uk.ac.cam.ks920;

public class CommunityMember extends Member {

    private int CommunityNum = -1;
    private COVID19_Population.MyCommunity Community;

    public CommunityMember(int x, int y, boolean willIsolateIfSick) {
        super(x, y, willIsolateIfSick);
    }

    //Community-related methods:

    public void SetCommunity(COVID19_Population.MyCommunity community, int communityNumber) {
        Community = community;
        CommunityNum = communityNumber;
    }

    public boolean IsAssignedToCommunity() {
        return (CommunityNum != -1);
    }

    public int GetCommunityNumber() throws Exception {
        if (CommunityNum == -1)
            throw new Exception("Member community not assigned");
        return CommunityNum;
    }

    public COVID19_Population.MyCommunity GetCommunity() throws Exception {
        if (CommunityNum == -1)
            throw new Exception("Member community not assigned");
        return Community;
    }

    //Community-related overrides:

    @Override
    public void ContractCOVID() {
        super.ContractCOVID();
        Community.StateCounters[1] += 1;
        Community.StateCounters[0] -= 1;
    }

    @Override
    public void Heal() {
        super.Heal();
        Community.StateCounters[1] -= 1;
        Community.StateCounters[2] += 1;
    }

    @Override
    public void Deceased() {
        super.Deceased();
        Community.StateCounters[1] -= 1;
        Community.StateCounters[3] += 1;
    }
}
