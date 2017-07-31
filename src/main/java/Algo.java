import java.util.*;

public class Algo {

    private double threshold = 0.1;
    // bipartite weighted graph
    // groups describe query clusters and document clusters respectively
    private Set<Cluster<Query, Document>> queryClusters;
    private Set<Cluster<Document, Query>> documentClusters;

    // distances
    private DistanceMatrix<Cluster<Query, Document>> distancesBetweenQueries;
    private DistanceMatrix<Cluster<Document, Query>> distancesBetweenDocuments;

    public Algo() {
        queryClusters = new HashSet<>();
        documentClusters = new HashSet<>();
    }

    public Set<Set<Query>> clusterQueries(Set<Query> queries) {
        initState(queries);
        runClustering(queryClusters, documentClusters, distancesBetweenQueries, distancesBetweenDocuments);

        Set<Set<Query>> clusters = new HashSet<>();
        for (Cluster<Query, Document> cluster : queryClusters) {
            clusters.add(cluster.getClusteredElements());
        }

        return clusters;
    }

    private <CType, NType> void
    runClustering(Set<Cluster<CType, NType>> currentClusters,
                  Set<Cluster<NType, CType>> neighbourClusters,
                  DistanceMatrix<Cluster<CType, NType>> currentDistances,
                  DistanceMatrix<Cluster<NType, CType>> neighbourDistances) {
        UnorderedPair<Cluster<CType, NType>> minPair = currentDistances.getPairWithMinDistance();
        Cluster<CType, NType> firstCluster = minPair.getNotEqualTo(null);
        Cluster<CType, NType> secondCluster = minPair.getNotEqualTo(firstCluster);

        if (currentDistances.getDistance(firstCluster, secondCluster) > threshold) {
            UnorderedPair<Cluster<NType, CType>> minPairNeigh = neighbourDistances.getPairWithMinDistance();
            Cluster<NType, CType> firstClusterNeigh = minPairNeigh.getNotEqualTo(null);
            Cluster<NType, CType> secondClusterNeigh = minPairNeigh.getNotEqualTo(firstClusterNeigh);

            if (neighbourDistances.getDistance(firstClusterNeigh, secondClusterNeigh) > threshold) {
                runClustering(neighbourClusters, currentClusters, neighbourDistances, currentDistances);
            }

            return;
        }

        Cluster<CType, NType> newMergedCluster = Cluster.mergeClusters(firstCluster, secondCluster);

        updateGraphOnMerge(firstCluster, secondCluster, newMergedCluster, currentClusters);
        updateDistanceOnMerge(firstCluster, secondCluster, newMergedCluster, currentDistances,
                neighbourDistances);

        runClustering(neighbourClusters, currentClusters, neighbourDistances, currentDistances);
    }

    private void initState(Set<Query> queries) {
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
                int weight = q.getLinksCount(d);
                queryCluster.addNeighbour(documentCluster, weight);
                documentCluster.addNeighbour(queryCluster, weight);
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
        DistanceMatrix<Cluster<CType, NType>> distances = new DistanceMatrixImpl<>();

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
        long commonLinksCount = 0;
        long totalCountOfLinks = firstCluster.totalCountOfLinks + secondCluster.totalCountOfLinks;

        // handle possible divide by zero exc
        if (totalCountOfLinks == 0) {
            return 0;
        }

        // cluster should be smallest in terms of count of neighbours
        for (Cluster<NType, CType> c : neighC1) {
            if (neighC2.contains(c)) {
                commonLinksCount += c.getLinksCountToNeighbour(firstCluster);
                commonLinksCount += c.getLinksCountToNeighbour(secondCluster);
            }
        }
        return commonLinksCount / totalCountOfLinks;
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
     * To do this new links to the mergeResult should be added to its neighbours and old links which lead to
     * two old clusters should be removed. Also these two cluster should be removed from corresponding set
     *
     * @param firstCluster
     * @param secondCluster Should be mentioned that the order of fisrtCluster and secondCluster are passed to the method
     *                      is not important
     * @param mergeResult   cluster which is the result of merging firstCluster and secondCluster
     * @param clusterSet    set of cluster which contains firstCluster and secondCluster and will contain
     *                      mergeResult
     * @param <CType>       type of elements which are stored in clusters
     * @param <NType>       type of elements which are stored in neighbour clusters
     */
    private <CType, NType> void
    updateGraphOnMerge(Cluster<CType, NType> firstCluster,
                       Cluster<CType, NType> secondCluster,
                       Cluster<CType, NType> mergeResult,
                       Set<Cluster<CType, NType>> clusterSet) {
        for (Cluster<NType, CType> n : firstCluster.getNeighbours()) {
            int linksCount = mergeResult.getLinksCountToNeighbour(n);
            n.addNeighbour(mergeResult, linksCount);
            n.deleteNeighbour(firstCluster);
        }

        for (Cluster<NType, CType> n : secondCluster.getNeighbours()) {
            int linksCount = mergeResult.getLinksCountToNeighbour(n);
            n.addNeighbour(mergeResult, linksCount);
            n.deleteNeighbour(secondCluster);
        }

        clusterSet.add(mergeResult);
        clusterSet.remove(firstCluster);
        clusterSet.remove(secondCluster);
    }

    /**
     * We assume that mergeResult cluster already properly added to the graph. So, the next step is to update
     * distances between some clusters. Obviously, we don't know distances from mergeResult, new cluster, to its
     * siblings, also some distances between its neighbours have been changed. Method updates some values in
     * appropriate distance matrices
     *
     * @param firstCluster
     * @param secondCluster
     * Should be mentioned that the order of first and second clusters is not important
     * @param mergerResult      cluster which is the result of merging firstCluster and secondCluster
     * @param siblingsDistance  distance between nodes of current part of the graph
     * @param neighbourDistance distance between neighbour nodes
     * @param <CType>           type of elements which are stored in clusters
     * @param <NType>           type of elements which are stored in neighbour clusters
     */
    private <CType, NType> void
    updateDistanceOnMerge(Cluster<CType, NType> firstCluster,
                          Cluster<CType, NType> secondCluster,
                          Cluster<CType, NType> mergerResult,
                          DistanceMatrix<Cluster<CType, NType>> siblingsDistance,
                          DistanceMatrix<Cluster<NType, CType>> neighbourDistance) {
        siblingsDistance.deleteRow(firstCluster);
        siblingsDistance.deleteRow(secondCluster);

        updateDistanceBetweenClusterAndSiblings(mergerResult,
                getSiblings(mergerResult),
                siblingsDistance);

        updateDistancesForSubsetOfClusters(mergerResult.getNeighbours(), neighbourDistance);
    }

    private <CType, NType> void
    updateDistanceBetweenClusterAndSiblings(Cluster<CType, NType> cluster,
                                            Set<Cluster<CType, NType>> siblings,
                                            DistanceMatrix<Cluster<CType, NType>> distances) {
        for (Cluster<CType, NType> sibling : siblings) {
            distances.addDistance(cluster, sibling, computeDistanceBetweenClusters(cluster, sibling));
        }
    }

    /**
     * This method recalculates distances only for clusters in the subset
     *
     * @param subset    of set of clusters: query clusters or document clusters
     * @param distances is distance matrix which stores all distances between clusters of corresponding
     *                  set of clusters
     * @param <CType>   type of elements which are stored in clusters
     * @param <NType>   type of elements which are stored in neighbour clusters
     */
    private <CType, NType> void
    updateDistancesForSubsetOfClusters(Set<Cluster<CType, NType>> subset,
                                       DistanceMatrix<Cluster<CType, NType>> distances) {
        Set<Cluster<CType, NType>> restOfTheClusters = new HashSet<>(subset);
        for (Cluster<CType, NType> cluster : subset) {
            restOfTheClusters.remove(cluster);
            for (Cluster<CType, NType> sibling : restOfTheClusters) {
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
    static class Cluster<CType, NType> {
        private Set<CType> clusteredElements;
        private HashMap<Cluster<NType, CType>, Integer> neighbours;
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
        mergeClusters(Cluster<CType, NType> c1, Cluster<CType, NType> c2) {
            Set<CType> newSetOfClusteredElements = new HashSet<>();
            newSetOfClusteredElements.addAll(c1.clusteredElements);
            newSetOfClusteredElements.addAll(c2.clusteredElements);
            HashMap<Cluster<NType, CType>, Integer> newNeighbours = new HashMap<>(c1.neighbours);

            for (Map.Entry<Cluster<NType, CType>, Integer> c2Neighbour : c2.neighbours.entrySet()) {
                int newLinksCount = c2Neighbour.getValue();
                newLinksCount += newNeighbours.getOrDefault(c2Neighbour.getKey(), 0);
                newNeighbours.put(c2Neighbour.getKey(), newLinksCount);
            }

            Cluster<CType, NType> newMergedCluster = new Cluster<>();
            newMergedCluster.clusteredElements = newSetOfClusteredElements;
            newMergedCluster.neighbours = newNeighbours;
            newMergedCluster.totalCountOfLinks = c1.totalCountOfLinks + c2.totalCountOfLinks;
            return newMergedCluster;
        }

        void addNeighbour(Cluster<NType, CType> c, int linksCount) throws IllegalArgumentException {
            if (linksCount < 0) {
                throw new IllegalArgumentException("weight must be greater or equal to zero");
            }
            Integer oldLinksCount = neighbours.put(c, linksCount);
            totalCountOfLinks -= oldLinksCount == null ? 0 : oldLinksCount;
            totalCountOfLinks += linksCount;
        }

        int deleteNeighbour(Cluster<NType, CType> neighbour) {
            Integer linksCount = neighbours.remove(neighbour);
            return linksCount == null ? 0 : linksCount;
        }

        int getLinksCountToNeighbour(Cluster<NType, CType> neighbour) {
            return neighbours.getOrDefault(neighbour, 0);
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
    }
}