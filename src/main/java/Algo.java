import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Algo {

  private static final Logger ALGO_LOGGER = Logger.getLogger(Algo.class.getName());

  // distance between clusters lies in [0, 1]
  // distance is less for closer (more similar) clusters
  // so, we want to know when two clusters are close enough to merge
  // threshold is maximal acceptable distance for merging clusters
  private double threshold = 0.01;

  // bipartite weighted graph
  // groups describe query clusters and document clusters respectively
  private Set<Cluster<Query, Document>> queryClusters;
  private Set<Cluster<Document, Query>> documentClusters;

  // distances
  private DistanceMatrix<Cluster<Query, Document>> distancesBetweenQueries;
  private DistanceMatrix<Cluster<Document, Query>> distancesBetweenDocuments;

  public Algo() {
    this(0.01);
  }

  public Algo(double threshold) {
    if (threshold > 1 || threshold < 0) {
      throw new IllegalArgumentException("Threshold value should belong to [0, 1]");
    }
    this.threshold = threshold;
    queryClusters = new HashSet<>();
    documentClusters = new HashSet<>();
  }

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double threshold) {
    if (threshold > 1 || threshold < 0) {
      throw new IllegalArgumentException("Threshold value should belong to [0, 1]");
    }
    this.threshold = threshold;
  }

  public Set<Set<Query>> clusterQueries(Set<Query> queries) {
    initState(queries);

    ALGO_LOGGER.log(Level.FINE, "Count of query clusters: {0}", queryClusters.size());
    ALGO_LOGGER.log(Level.FINE, "Count of document clusters: {0}", documentClusters.size());
    ALGO_LOGGER.log(Level.FINE, "Count of distances between query clusters: {0}",
        distancesBetweenQueries.size());
    ALGO_LOGGER.log(Level.FINE, "Count of distances between document clusters: {0}",
        distancesBetweenDocuments.size());

    runClustering();

    Set<Set<Query>> clusters = new HashSet<>();
    for (Cluster<Query, Document> cluster : queryClusters) {
      clusters.add(cluster.getClusteredElements());
    }

    return clusters;
  }

  private void runClustering() {
    boolean canMergeClusters = true;
    long iterCount = 0;
    long startTime = System.nanoTime();
    while (canMergeClusters) {
      ++iterCount;
      canMergeClusters = tryMerge(queryClusters, distancesBetweenQueries,
          distancesBetweenDocuments);
      canMergeClusters |= tryMerge(documentClusters, distancesBetweenDocuments,
          distancesBetweenQueries);
    }
    long elapsedTime = System.nanoTime() - startTime;
    ALGO_LOGGER.log(Level.FINE, "Time: {0}s", elapsedTime / 1e9);
    ALGO_LOGGER.log(Level.FINE, "Count of iterations: {0}", iterCount);
  }

  /**
   * This method performs one step of clustering for one of the parts of the graph
   * (for document clusters or for query clusters)
   *
   * @param currentClusters are clusters where pair with min distance will be looked for
   * @param currentDistances are distances between currentClusters
   * @param neighbourDistances distances between neighbour clusters (actually it only needed for
   * recalculating distance between neighbour after two currentClusters were merged)
   * @param <CType> type of current clusters
   * @param <NType> type of neighbour clusters
   * @return true if there was pair of clusters with distance less than threshold, false otherwise
   */
  private <CType, NType> boolean tryMerge(Set<Cluster<CType, NType>> currentClusters,
      DistanceMatrix<Cluster<CType, NType>> currentDistances,
      DistanceMatrix<Cluster<NType, CType>> neighbourDistances) {

    UnorderedPair<Cluster<CType, NType>> minDistancePair =
        currentDistances.getPairWithMinDistance();
    if (minDistancePair != null) {
      Cluster<CType, NType> firstCluster = minDistancePair.getNotEqualTo(null);
      Cluster<CType, NType> secondCluster = minDistancePair.getNotEqualTo(firstCluster);

      Double distance = currentDistances.getDistance(firstCluster, secondCluster);
      if (distance != null && distance < threshold) {
        Cluster<CType, NType> newMergedCluster =
            Cluster.mergeClusters(firstCluster, secondCluster);
        updateGraphOnMerge(firstCluster, secondCluster, newMergedCluster, currentClusters);
        updateDistanceOnMerge(firstCluster, secondCluster, newMergedCluster,
            currentDistances, neighbourDistances);
        return true;
      }
    }
    return false;
  }

  private void initState(Set<Query> queries) {
    queryClusters = new HashSet<>();
    documentClusters = new HashSet<>();
    buildGraph(queries);
    initDistances();
  }

  private void buildGraph(Set<Query> queries) {
    Map<Query, Cluster<Query, Document>> tmpQueryClusters = new HashMap<>();
    Map<Document, Cluster<Document, Query>> tmpDocumentClusters = new HashMap<>();
    for (Query q : queries) {
      Cluster<Query, Document> queryCluster = new Cluster<>(q);
      tmpQueryClusters.put(q, queryCluster);
      for (Document d : q.getRelatedDocuments()) {
        if (!tmpDocumentClusters.containsKey(d)) {
          tmpDocumentClusters.put(d, new Cluster<>(d));
        }
        Cluster<Document, Query> documentCluster = tmpDocumentClusters.get(d);
        long linksCount = q.getLinksCount(d);
        queryCluster.addNeighbour(documentCluster, linksCount);
        documentCluster.addNeighbour(queryCluster, linksCount);
      }
    }
    queryClusters.addAll(tmpQueryClusters.values());
    documentClusters.addAll(tmpDocumentClusters.values());
  }

  private void initDistances() {
    distancesBetweenDocuments = createDistanceMatrixForSetOfClusters(documentClusters);
    distancesBetweenQueries = createDistanceMatrixForSetOfClusters(queryClusters);
  }

  private <CType, NType> DistanceMatrix<Cluster<CType, NType>>
  createDistanceMatrixForSetOfClusters(Set<Cluster<CType, NType>> clusters) {
    Set<Cluster<CType, NType>> alreadyDone = new HashSet<>();
    DistanceMatrix<Cluster<CType, NType>> distances = new DistanceMatrixTree<>();

    for (Cluster<CType, NType> c : clusters) {
      alreadyDone.add(c);
      Set<Cluster<CType, NType>> siblings = getSiblings(c);
      for (Cluster<CType, NType> s : siblings) {
        if (!alreadyDone.contains(s)) {
          distances.addDistance(c, s, computeDistanceBetweenClusters(c, s));
        }
      }
    }
    return distances;
  }

  private <CType, NType> double
  computeDistanceBetweenClusters(Cluster<CType, NType> firstCluster,
      Cluster<CType, NType> secondCluster) {
    Set<Cluster<NType, CType>> neighC1 = firstCluster.getNeighbours();
    Set<Cluster<NType, CType>> neighC2 = secondCluster.getNeighbours();
    double commonLinksCount = 0;
    double totalCountOfLinks =
        firstCluster.getTotalCountOfLinks() + secondCluster.getTotalCountOfLinks();

    // possible overflow
    if (totalCountOfLinks < 0) {
      throw new RuntimeException("Total count of links is below zero while computing distance " +
          "between cluster (possible overflow)");
    }
    // handle possible divide by zero exc
    // we return maximum possible distance for metric -- 1
    if (totalCountOfLinks == 0) {
      return 1;
    }

    // cluster should be smallest in terms of count of neighbours
    if (neighC1.size() > neighC2.size()) {
      Set swp = neighC2;
      neighC2 = neighC1;
      neighC1 = swp;
    }
    for (Cluster<NType, CType> c : neighC1) {
      if (neighC2.contains(c)) {
        commonLinksCount += c.getLinksCountToNeighbour(firstCluster);
        commonLinksCount += c.getLinksCountToNeighbour(secondCluster);
      }
    }
    return 1. - commonLinksCount / totalCountOfLinks;
  }

  private <CType, NType> Set<Cluster<CType, NType>>
  getSiblings(Cluster<CType, NType> cluster) {
    Set<Cluster<CType, NType>> siblings = new HashSet<>();
    for (Cluster<NType, CType> c : cluster.getNeighbours()) {
      siblings.addAll(c.getNeighbours());
    }
    siblings.remove(cluster);
    return siblings;
  }

  /**
   * After two cluster were merged new cluster, which is merge result, must be added to the graph.
   * To do this new links to the mergeResult should be added to its neighbours and old links which
   * lead to two old clusters should be removed. Also these two cluster should be removed from
   * corresponding set
   *
   * @param secondCluster Should be mentioned that the order of fisrtCluster and secondCluster are
   * passed to the method is not important
   * @param mergeResult cluster which is the result of merging firstCluster and secondCluster
   * @param clusterSet set of cluster which contains firstCluster and secondCluster and will contain
   * mergeResult
   * @param <CType> type of elements which are stored in clusters
   * @param <NType> type of elements which are stored in neighbour clusters
   */
  private <CType, NType> void
  updateGraphOnMerge(Cluster<CType, NType> firstCluster,
      Cluster<CType, NType> secondCluster,
      Cluster<CType, NType> mergeResult,
      Set<Cluster<CType, NType>> clusterSet) {
    for (Cluster<NType, CType> n : firstCluster.getNeighbours()) {
      long linksCount = mergeResult.getLinksCountToNeighbour(n);
      n.addNeighbour(mergeResult, linksCount);
      n.deleteNeighbour(firstCluster);
    }

    for (Cluster<NType, CType> n : secondCluster.getNeighbours()) {
      long linksCount = mergeResult.getLinksCountToNeighbour(n);
      n.addNeighbour(mergeResult, linksCount);
      n.deleteNeighbour(secondCluster);
    }

    clusterSet.add(mergeResult);
    clusterSet.remove(firstCluster);
    clusterSet.remove(secondCluster);
  }

  /**
   * We assume that mergeResult cluster already properly added to the graph. So, the next step is to
   * update distances between some clusters. Obviously, we don't know distances from mergeResult,
   * new cluster, to its siblings, also some distances between its neighbours have been changed.
   * Method updates some values in appropriate distance matrices
   *
   * @param secondCluster Should be mentioned that the order of first and second clusters is not
   * important
   * @param mergeResult cluster which is the result of merging firstCluster and secondCluster
   * @param siblingsDistance distance between nodes of current part of the graph
   * @param neighbourDistance distance between neighbour nodes
   * @param <CType> type of elements which are stored in clusters
   * @param <NType> type of elements which are stored in neighbour clusters
   */
  private <CType, NType> void
  updateDistanceOnMerge(Cluster<CType, NType> firstCluster,
      Cluster<CType, NType> secondCluster,
      Cluster<CType, NType> mergeResult,
      DistanceMatrix<Cluster<CType, NType>> siblingsDistance,
      DistanceMatrix<Cluster<NType, CType>> neighbourDistance) {
    // deleteRow is convenient, but waaaay too inefficient, so we delete distances one by one
    getSiblings(firstCluster).forEach(sib -> siblingsDistance.deleteDistance(sib, firstCluster));
    getSiblings(secondCluster).forEach(sib -> siblingsDistance.deleteDistance(sib, secondCluster));

    // we deleted all links which lead to firstCluster and secondCluster thus they are not siblings anymore
    // so we need to delete distance between them manually
    siblingsDistance.deleteDistance(firstCluster, secondCluster);

    // we need to add distances between mergeResult and its siblings to appropriate distance matrix
    addDistanceBetweenClusterAndSiblings(mergeResult,
        getSiblings(mergeResult),
        siblingsDistance);

    // distance between clusters who were neighbours to firstCluster and secondCluster simultaneously
    // didn't change. For other pairs of neighbours we need to recalculate distance
    Set<Cluster<NType, CType>> commonNeighbours = new HashSet<>(firstCluster.getNeighbours());
    commonNeighbours.retainAll(secondCluster.getNeighbours());

    Set<Cluster<NType, CType>> onlyFirstClusterNeighbours = new HashSet<>(
        firstCluster.getNeighbours());
    onlyFirstClusterNeighbours.removeAll(commonNeighbours);

    Set<Cluster<NType, CType>> onlySecondClusterNeighbours = new HashSet<>(
        secondCluster.getNeighbours());
    onlySecondClusterNeighbours.removeAll(commonNeighbours);

    updateDistancesForSubsetOfClusters(onlyFirstClusterNeighbours,
        onlySecondClusterNeighbours,
        commonNeighbours,
        neighbourDistance);
  }

  private <CType, NType> void
  addDistanceBetweenClusterAndSiblings(Cluster<CType, NType> cluster,
      Set<Cluster<CType, NType>> siblings,
      DistanceMatrix<Cluster<CType, NType>> distances) {
    for (Cluster<CType, NType> sibling : siblings) {
      distances.addDistance(cluster, sibling, computeDistanceBetweenClusters(cluster, sibling));
    }
  }

  /**
   * This method recalculates distances for some subset of clusters. This subset forms as follows:
   * 1) All pairs where one element is from firstSet and another is from secondSet
   * 2) All pairs where one element is from firstSet and another is from commonSet
   * 3) All pairs where one element is from secondSet and another is from commonSet
   *
   * @param firstSet clusters which are neighbours for only firstCluster
   * @param secondSet clusters which are neighbours for only secondCluster
   * @param commonSet clusters which are common for both clusters
   * @param distances is distance matrix which stores all distances between clusters of
   * corresponding set of clusters
   * @param <CType> type of elements which are stored in clusters
   * @param <NType> type of elements which are stored in neighbour clusters
   */
  private <CType, NType> void
  updateDistancesForSubsetOfClusters(Set<Cluster<CType, NType>> firstSet,
      Set<Cluster<CType, NType>> secondSet,
      Set<Cluster<CType, NType>> commonSet,
      DistanceMatrix<Cluster<CType, NType>> distances) {
    for (Cluster<CType, NType> cluster : firstSet) {
      for (Cluster<CType, NType> sibling : commonSet) {
        distances.addDistance(cluster, sibling, computeDistanceBetweenClusters(cluster, sibling));
      }
      for (Cluster<CType, NType> sibling : secondSet) {
        distances.addDistance(cluster, sibling, computeDistanceBetweenClusters(cluster, sibling));
      }
    }

    for (Cluster<CType, NType> cluster : secondSet) {
      for (Cluster<CType, NType> sibling : commonSet) {
        distances.addDistance(cluster, sibling, computeDistanceBetweenClusters(cluster, sibling));
      }
    }
  }

  /**
   * describes the cluster which is also node in bipartite graph
   *
   * @param <CType> is the type of elements which are stored in current cluster
   * @param <NType> is the type of elements which are stored in neighbour cluster
   */
  private static class Cluster<CType, NType> {

    private static final Logger CLUSTER_LOGGER = Logger.getLogger(Cluster.class.getName());
    private Set<CType> clusteredElements;
    private HashMap<Cluster<NType, CType>, Long> neighbours;
    private long totalCountOfLinks;

    private Cluster() {
      totalCountOfLinks = 0;
      clusteredElements = new HashSet<>();
      neighbours = new HashMap<>();
    }

    Cluster(CType element) {
      this();
      clusteredElements.add(element);
    }

    Cluster(Set<CType> elements) {
      this();
      clusteredElements.addAll(elements);
    }

    static <CType, NType> Cluster<CType, NType>
    mergeClusters(Cluster<CType, NType> firstCluster, Cluster<CType, NType> secondCluster) {
      Set<CType> newSetOfClusteredElements = new HashSet<>();
      newSetOfClusteredElements.addAll(firstCluster.clusteredElements);
      newSetOfClusteredElements.addAll(secondCluster.clusteredElements);
      HashMap<Cluster<NType, CType>, Long> newNeighbours = new HashMap<>(firstCluster.neighbours);

      for (Map.Entry<Cluster<NType, CType>, Long> c2Neighbour : secondCluster.neighbours
          .entrySet()) {
        long newLinksCount = c2Neighbour.getValue();
        newLinksCount += newNeighbours.getOrDefault(c2Neighbour.getKey(), 0L);
        newNeighbours.put(c2Neighbour.getKey(), newLinksCount);
      }

      Cluster<CType, NType> newMergedCluster = new Cluster<>();
      newMergedCluster.clusteredElements = newSetOfClusteredElements;
      newMergedCluster.neighbours = newNeighbours;
      newMergedCluster.totalCountOfLinks =
          firstCluster.totalCountOfLinks + secondCluster.totalCountOfLinks;

      if (newMergedCluster.totalCountOfLinks < 0) {
        CLUSTER_LOGGER.log(Level.SEVERE, "Total links count is below zero (possible overflow):",
            new Object[]{newMergedCluster.totalCountOfLinks});
        throw new RuntimeException("Total links count is below zero (possible overflow)");
      }

      return newMergedCluster;
    }

    void addNeighbour(Cluster<NType, CType> c, long linksCount) throws IllegalArgumentException {
      if (linksCount < 0) {
        throw new IllegalArgumentException("weight must be greater or equal to zero");
      }
      Long oldLinksCount = neighbours.put(c, linksCount);
      totalCountOfLinks -= oldLinksCount == null ? 0 : oldLinksCount;
      totalCountOfLinks += linksCount;

      if (totalCountOfLinks < 0) {
        CLUSTER_LOGGER.log(Level.SEVERE,
            "In cluster. Total links count is below zero (possible overflow): {0}",
            totalCountOfLinks);
        throw new RuntimeException(
            "In cluster. ITotal links count is below zero (possible overflow)");
      }
    }

    long deleteNeighbour(Cluster<NType, CType> neighbour) {
      Long linksCount = neighbours.remove(neighbour);
      totalCountOfLinks -= linksCount == null ? 0 : linksCount;
      return linksCount == null ? 0 : linksCount;
    }

    long getLinksCountToNeighbour(Cluster<NType, CType> neighbour) {
      return neighbours.getOrDefault(neighbour, 0L);
    }

    long getTotalCountOfLinks() {
      return totalCountOfLinks;
    }

    Set<Cluster<NType, CType>> getNeighbours() {
      return Collections.unmodifiableSet(neighbours.keySet());
    }

    Set<CType> getClusteredElements() {
      return Collections.unmodifiableSet(clusteredElements);
    }

    @Override
    public boolean equals(Object other) {
      return this == other;
    }

  }
}