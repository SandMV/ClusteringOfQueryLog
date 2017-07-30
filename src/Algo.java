import com.sun.xml.internal.bind.v2.model.core.ID;

import java.util.*;

public class Algo {

    // bipartite weighted graph
    // groups describe query clusters and document clusters respectively
    private Set<Cluster<Query, Document>> queryClusters;
    private Set<Cluster<Document, Query>> documentClusters;

    // distances
    private IDistanceMatrix<Cluster<Query, Document>> distancesBetweenQueries;
    private IDistanceMatrix<Cluster<Document, Query>> distancesBetweenDocuments;

    public Algo() {
        queryClusters = new HashSet<>();
        documentClusters = new HashSet<>();
    }

    public Set<Set<Query>> clusterQueries(Set<Query> qSet) {
        return null;
    }

    private void buildGraph(Set<Query> queries) {
        Map<Query, Cluster<Query, Document>> tmpQueryClusters = new HashMap<>();
        Map<Document, Cluster<Document, Query>> tmpDocumentClusters = new HashMap<>();
        for (Query q : queries) {
            Cluster<Query, Document> qCluster = new Cluster<>(q);
            tmpQueryClusters.put(q, qCluster);
            for (Document d : q.getRelatedDocuments()) {
                if (!tmpDocumentClusters.containsKey(d)) {
                    tmpDocumentClusters.put(d, new Cluster<>(d));
                }
                Cluster<Document, Query> dCluster = tmpDocumentClusters.get(d);
                int weight = q.getLinksCount(d);
                qCluster.addNeighbour(dCluster, weight);
                dCluster.addNeighbour(qCluster, weight);
            }
        }
        queryClusters.addAll(tmpQueryClusters.values());
        documentClusters.addAll(tmpDocumentClusters.values());
    }

    private void initDistances() {
        distancesBetweenDocuments = createDistanceMatrixForSetOfClusters(documentClusters);
        distancesBetweenQueries = createDistanceMatrixForSetOfClusters(queryClusters);
    }

    private <CType, NType> IDistanceMatrix<Cluster<CType, NType>>
    createDistanceMatrixForSetOfClusters(Set<Cluster<CType, NType>> clusters) {
        Set<Cluster<CType, NType>> alreadyDone = new HashSet<>();
        IDistanceMatrix<Cluster<CType, NType>> distances = new DistanceMatrix<>();

        for (Cluster<CType, NType> c : clusters) {
            alreadyDone.add(c);
            Set<Cluster<CType, NType>> siblings = getSiblings(c);
            for (Cluster<CType, NType> s : siblings) {
                if (!alreadyDone.contains(s)) {
                    double distance = computeDistanceBetweenClusters(c, s);
                    distances.addDistance(c, s, distance);
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

    private <CType, NType> void
    updateDistanceOnMerge(Cluster<CType, NType> c1,
                          Cluster<CType, NType> c2,
                          Cluster<CType, NType> mergerResult,
                          IDistanceMatrix<Cluster<CType, NType>> siblingsDistance,
                          IDistanceMatrix<Cluster<NType, CType>> neighbourDistance) {
        siblingsDistance.deleteRow(c1);
        siblingsDistance.deleteRow(c2);

        updateDistanceBetweenClusterAndSiblings(mergerResult,
                getSiblings(mergerResult),
                siblingsDistance);

        updateDistancesForSubsetOfClusters(mergerResult.getNeighbours(), neighbourDistance);
    }

    private <CType, NType> void
    updateDistanceBetweenClusterAndSiblings(Cluster<CType, NType> cluster,
                                            Set<Cluster<CType, NType>> siblings,
                                            IDistanceMatrix<Cluster<CType, NType>> distances) {
        for(Cluster<CType, NType> sibling : siblings) {
            double distance = computeDistanceBetweenClusters(cluster, sibling);
            distances.addDistance(cluster, sibling, distance);
        }
    }

    private <CType, NType> void
    updateDistancesForSubsetOfClusters(Set<Cluster<CType, NType>> subset,
                                       IDistanceMatrix<Cluster<CType, NType>> distances) {
        Set<Cluster<CType, NType>> restOfTheClusters = new HashSet<>(subset);
        for (Cluster<CType, NType> cluster : subset) {
            restOfTheClusters.remove(cluster);
            for (Cluster<CType, NType> sibling : restOfTheClusters) {
                double distance = computeDistanceBetweenClusters(cluster, sibling);
                distances.addDistance(cluster, sibling, distance);
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

        void addNeighbour(Cluster<NType, CType> c, int linksCount)
                throws IllegalArgumentException {
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