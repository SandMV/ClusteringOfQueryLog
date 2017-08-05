/**
 * Created by sandulmv on 28.07.17.
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This implementation does not allow to have null in coordinates
 *
 * @param <T> type of coordinates
 */
public class DistanceMatrixTree<T> implements DistanceMatrix<T> {

  // The reason to have two Maps is to reduce search time of pair with minimal/maximal distance and have
  // acceptable asymptotics for modification operations such as delete and add
  // First map uses pairs of points as keys and stores distances between these points
  // Second one uses distances between point as keys and stores pairs of points which have such distance
  // So we can always get distance by coordinates and coordinates by distance. That can be useful in
  // deleteDistance method where only coordinates are know, but we also should have distance to alter all maps
  private HashMap<UnorderedPair<T>, Double> distMatrixPairKey;
  private TreeMap<Double, Set<UnorderedPair<T>>> distMatrixDistKey;

  public DistanceMatrixTree() {
    distMatrixDistKey = new TreeMap<>();
    distMatrixPairKey = new HashMap<>();
  }

  /**
   * @param point1 is one of two coordinates with which distance will be associated
   * @param point2 is one of two coordinates with which distance will be associated
   * @param distance between point1 and point2
   * @return old distance between point1 and point2 or null if there wasn't such distance
   * @throws IllegalArgumentException when point1 or point2 is null or distance less then 0
   */
  @Override
  public Double addDistance(T point1, T point2, double distance) throws IllegalArgumentException {
    if (distance < 0) {
      throw new IllegalArgumentException("Distance shouldn't be less than 0");
    }

    if (point1 == null || point2 == null) {
      throw new IllegalArgumentException("Points should be not null");
    }

    UnorderedPair<T> key = new UnorderedPairHash<>(point1, point2);
    Double oldDistance = distMatrixPairKey.put(key, distance);

    deletePairFromDistMatrixDistKey(oldDistance, key);

    if (!distMatrixDistKey.containsKey(distance)) {
      distMatrixDistKey.put(distance, new HashSet<>());
    }

    distMatrixDistKey.get(distance).add(key);

    return oldDistance;
  }

  @Override
  public Double deleteDistance(T point1, T point2) {
    if (point1 == null || point2 == null) {
      return null;
    }

    UnorderedPair<T> key = new UnorderedPairHash<>(point1, point2);
    Double oldDistance = distMatrixPairKey.remove(key);

    deletePairFromDistMatrixDistKey(oldDistance, key);

    return oldDistance;
  }

  @Override
  public Map<T, Double> deleteRow(T point) {
    if (point == null) {
      return new HashMap<>();
    }

    Set<Map.Entry<UnorderedPair<T>, Double>> row =
        distMatrixPairKey
            .entrySet()
            .parallelStream()
            .filter(e -> e.getKey().inPair(point))
            .collect(Collectors.toSet());

    row.forEach(e -> deletePairFromDistMatrixDistKey(e.getValue(), e.getKey()));

    row.forEach(e -> distMatrixPairKey.remove(e.getKey()));

    return row
        .parallelStream()
        .collect(Collectors.toMap(e -> e.getKey().getNotEqualTo(point), Map.Entry::getValue));
  }

  @Override
  public Double getDistance(T point1, T point2) {
    if (point1 == null || point2 == null) {
      return null;
    }

    return distMatrixPairKey.get(new UnorderedPairHash<>(point1, point2));
  }

  @Override
  public boolean containsDistance(T point1, T point2) {
    if (point1 == null || point2 == null) {
      return false;
    }

    return distMatrixPairKey.containsKey(new UnorderedPairHash<>(point1, point2));
  }

  @Override
  public UnorderedPair<T> getPairWithMinDistance() {
    if (distMatrixPairKey.isEmpty()) {
      return null;
    }

    for (UnorderedPair<T> pair : distMatrixDistKey.firstEntry().getValue()) {
      return pair;
    }

    return null;
  }

  @Override
  public UnorderedPair<T> getPairWithMaxDistance() {
    if (distMatrixPairKey.isEmpty()) {
      return null;
    }

    for (UnorderedPair<T> pair : distMatrixDistKey.lastEntry().getValue()) {
      return pair;
    }

    return null;
  }

  @Override
  public int size() {
    return distMatrixPairKey.size();
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DistanceMatrixTree)) {
      return false;
    }
    if (other == this) {
      return true;
    }
    DistanceMatrixTree otherDM = (DistanceMatrixTree) other;
    return distMatrixPairKey.equals(otherDM.distMatrixPairKey);
  }

  @Override
  public int hashCode() {
    return distMatrixPairKey.hashCode();
  }

  private void deletePairFromDistMatrixDistKey(Double distance, UnorderedPair<T> pair) {
    if (distance != null) {
      Set<UnorderedPair<T>> associatedPairs = distMatrixDistKey.get(distance);
      associatedPairs.remove(pair);
      if (associatedPairs.isEmpty()) {
        distMatrixDistKey.remove(distance);
      }
    }
  }

}
