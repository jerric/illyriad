package info.lliira.illyriad.map.entity;

import java.util.Date;

public class Progress extends Location<Progress.Builder> {
    public final Date lastUpdated;

    private Progress(int x, int y, Date lastUpdated) {
        super(x, y);
        this.lastUpdated = lastUpdated;
    }

    @Override
    public Builder toBuilder() {
        return null;
    }

    public static class Builder extends Location.Builder<Progress> {

        private Date lastUpdated;

        public Builder() {}

        private Builder(Progress progress) {
            super(progress);
            lastUpdated = progress.lastUpdated;
        }

        public Builder lastUpdated(Date lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        @Override
        public Progress build() {
            return new Progress(x, y, lastUpdated);
        }
    }
}
