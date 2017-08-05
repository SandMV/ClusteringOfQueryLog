/**
 * Created by sandulmv on 28.07.17.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Query {
    private String query;
    private Map<Document, Long> relatedDocuments;

    public Query(String query) {
        this.query = query;
        relatedDocuments = new HashMap<>();
    }

    public Set<Document> getRelatedDocuments() {
        return relatedDocuments.keySet();
    }

    public long getLinksCount(Document doc) {
        return relatedDocuments.getOrDefault(doc, 0L);
    }

    public void addRelatedDocument(Document doc) {
        addRelatedDocument(doc, 1);
    }

    public void addRelatedDocument(Document doc, long linksCount) {
        long newLinksCount = relatedDocuments.getOrDefault(doc, 0L) + linksCount;
        relatedDocuments.put(doc, newLinksCount);
    }

    @Override
    public String toString() {
        return query;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Query)) {
            return false;
        }
        if (other == this) {
            return true;
        }

        Query otherQuery = (Query) other;
        return query.equals(otherQuery.query);
    }

    @Override
    public int hashCode() {
        return query.hashCode();
    }
}
