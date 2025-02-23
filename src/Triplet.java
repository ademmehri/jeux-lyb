package src;

public class Triplet<V1, V2, V3> {
    private final V1 firstValue;
    private final V2 secondValue;
    private final V3 thirdValue;

    public Triplet(V1 firstValue, V2 secondValue, V3 thirdValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
        this.thirdValue = thirdValue;
    }

    public V1 getFirstValue() { return firstValue; }
    public V2 getSecondValue() { return secondValue; }
    public V3 getThirdValue() { return thirdValue; }
}
