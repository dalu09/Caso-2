public class FaultsCounter {
    private int faults = 0;
    private int hits = 0;

    public void countFault() {
        faults++;
    }

    public void countHit() {
        hits++;
    }

    public int getFaults() {
        return faults;
    }

    public int getHits() {
        return hits;
    }
}
