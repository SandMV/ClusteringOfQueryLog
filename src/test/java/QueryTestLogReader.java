import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by sandulmv on 01.08.17.
 */

/**
 * Test log has the following format:
 * query    document    linksCount
 * or
 * query
 *
 * No caption
 */
public class QueryTestLogReader {
    private Map<String, Query> queries;

    public Set<Query> readQueryLog(String fileName) throws FileNotFoundException, IOException {
        queries = new HashMap<>();

        try (BufferedReader input = new BufferedReader(new FileReader(fileName))) {
            input.lines()
                    .map(this::extractQueryAndDocNameFromLine)
                    .forEach(this::addQuery);
        }

        return new HashSet<>(queries.values());
    }

    private String[] extractQueryAndDocNameFromLine(String line) {
        String[] tokens = line.split("\t");
        if (tokens.length < 3) {
            return new String[] {tokens[0]};
        }
        return new String[] {tokens[0], tokens[1], tokens[2]};
    }

    private void addQuery(String[] query) {
        query[0] = query[0].trim();
        if(!queries.containsKey(query[0])) {
            queries.put(query[0], new Query(query[0]));
        }
        if(query.length == 3) {
            query[1] = query[1].trim();
            Document document = new Document(query[1]);
            queries.get(query[0]).addRelatedDocument(document, Long.valueOf(query[2]));
        }
    }
}
