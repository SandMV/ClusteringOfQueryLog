/**
 * Created by sandulmv on 28.07.17.
 */
import java.util.*;
import java.util.stream.Stream;

public class Query {
    private String query;
    private HashMap<Document, Integer> relatedDocuments;

    public Set<Document> getRelatedDocuments() {
        return relatedDocuments.keySet();
    }

    public int getLinksCount(Document doc) {
        return relatedDocuments.getOrDefault(doc, 0);
    }

    @Override
    public String toString() {
        return query;
    }
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Query))
            return false;
        if(other == this) return true;

        Query otherQuery = (Query)other;
        return query.equals(otherQuery.query);
    }
    @Override
    public int hashCode() {
        return query.hashCode();
    }
}
