import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sandulmv on 28.07.17.
 */
public class DistanceMatrix<T> implements IDistanceMatrix<T>{
    private HashMap<IUnorderedPair<T>, Double> distanceMatrix;
    public DistanceMatrix() {
        distanceMatrix = new HashMap<>();
    }

    @Override
    public double addDistance(T point1, T point2, double distance)
            throws IllegalArgumentException {
        if(distance < 0) throw new IllegalArgumentException("Distance shouldn't less then 0");

        Double oldVal = distanceMatrix.put(new UnorderedPair<>(point1, point2), distance);
        return oldVal == null ? -1 : oldVal;
    }

    @Override
    public double deleteDistance(T point1, T point2) {
        Double val = distanceMatrix.remove(new UnorderedPair<>(point1, point2));
        return val == null ? -1 : val;
    }

    @Override
    public Map<IUnorderedPair<T>, Double> deleteRow(T point) {
        Set<IUnorderedPair<T>> rowKeys = distanceMatrix.keySet()
                .stream()
                .filter(tUnorderedPair -> tUnorderedPair.inPair(point))
                .collect(Collectors.toSet());
        Map<IUnorderedPair<T>, Double> row = new HashMap<>();
        for(IUnorderedPair<T> key : rowKeys) {
            row.put(key, distanceMatrix.remove(key));
        }
        return row;
    }

    @Override
    public double getDistance(T point1, T point2) {
        Double val = distanceMatrix.get(new UnorderedPair<>(point1, point2));
        return val == null ? -1 : val;
    }

    @Override
    public boolean containsDistance(T point1, T point2) {
        return distanceMatrix.containsKey(new UnorderedPair<>(point1, point2));
    }

    @Override
    public IUnorderedPair<T> getPairWithMinDistance() {
        double minDistance = Double.MAX_VALUE;
        IUnorderedPair<T> minPair = null;
        for(Map.Entry<IUnorderedPair<T>, Double> entry : distanceMatrix.entrySet()) {
            double distance = entry.getValue();
            if(distance <= minDistance) {
                minDistance = distance;
                minPair = entry.getKey();
            }
        }
        return minPair;
    }

    @Override
    public IUnorderedPair<T> getPairWithMaxDistance() {
        double maxDistance = Double.MIN_VALUE;
        IUnorderedPair<T> minPair = null;
        for(Map.Entry<IUnorderedPair<T>, Double> entry : distanceMatrix.entrySet()) {
            double distance = entry.getValue();
            if(distance >= maxDistance) {
                maxDistance = distance;
                minPair = entry.getKey();
            }
        }
        return minPair;
    }
}
