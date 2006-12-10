package enginuity.logger.profile;

public final class UserProfileItemImpl implements UserProfileItem {
    private final String units;
    private final boolean liveDataSelected;
    private final boolean graphSelected;
    private final boolean dashSelected;

    public UserProfileItemImpl(String units, boolean liveDataSelected, boolean graphSelected, boolean dashSelected) {
        this.units = units;
        this.liveDataSelected = liveDataSelected;
        this.graphSelected = graphSelected;
        this.dashSelected = dashSelected;
    }

    public boolean isDashSelected() {
        return dashSelected;
    }

    public boolean isGraphSelected() {
        return graphSelected;
    }

    public boolean isLiveDataSelected() {
        return liveDataSelected;
    }

    public String getUnits() {
        return units;
    }

}
