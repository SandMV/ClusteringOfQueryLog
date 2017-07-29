import java.util.*;

public class Algo {

    // bipartite weighted graph
    // groups describe query clusters and document clusters respectively
    Set<Cluster<Query, Document>> queryClusters;
    Set<Cluster<Document, Query>> documentClusters;

    // distances
    IDistanceMatrix<Cluster<Query, Document>> distancesBetweenQueries;
    IDistanceMatrix<Cluster<Document, Query>> distancesBetweenDocuments;

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

    private <CType, NType> double computeDistanceBetweenClusters
            (Cluster<CType, NType> c1, Cluster<CType, NType> c2) {
        Set<Cluster<NType, CType>> neighC1 = c1.getNeighbours();
        Set<Cluster<NType, CType>> neighC2 = c2.getNeighbours();
        long commonLinksCount = 0;
        long totalCountOfLinks = c1.totalCountOfLinks + c2.totalCountOfLinks;

        // handle possible divide by zero exc
        if (totalCountOfLinks == 0) {
            return 0;
        }

        // cluster should be smallest in terms of count of neighbours
        for (Cluster<NType, CType> c : neighC1) {
            if (neighC2.contains(c)) {
                commonLinksCount += c.getLinksCountToNeighbour(c1);
                commonLinksCount += c.getLinksCountToNeighbour(c2);
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
     * describes the cluster which is also node in bipartite graph
     *
     * @param <CType> is the type of elements which are stored in current cluster
     * @param <NType> is the type of elements which are stored in newighbour cluster
     */
    class Cluster<CType, NType> {
        Set<CType> clusteredElements;
        HashMap<Cluster<NType, CType>, Integer> neighbours;
        long totalCountOfLinks;

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

        void addNeighbour(Cluster<NType, CType> c, int weight)
                throws IllegalArgumentException {
            if (weight < 0) {
                throw new IllegalArgumentException("weight must be greater or equal to zero");
            }
            neighbours.put(c, weight);
            totalCountOfLinks += weight;
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

        Cluster<CType, NType> mergeWith(Cluster<CType, NType> other) {
            Set<CType> mergedElements = new HashSet<>(clusteredElements);
            mergedElements.addAll(other.clusteredElements);
            return new Cluster<>(mergedElements);
        }
    }
}