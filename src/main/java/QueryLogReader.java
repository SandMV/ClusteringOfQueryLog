/**
 * Created by sandulmv on 01.08.17.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * We assume files have structure as follows:
 * id   query   queryTime   itemRank    clickUrl
 * we only need query and clickUrl
 * <p>
 * Also we will treat lines which start with '#' as commentaries and ignore them
 */
public class QueryLogReader {

  private Map<String, Query> queries;
  private String lastQuery = "";

  public Set<Query> readQueryLog(String fileName, int nLines) throws IOException {
    if (fileName == null) {
      throw new IllegalArgumentException("fileName should not be null");
    }

    if (nLines < 0) {
      throw new IllegalArgumentException("Lines count should not be less than 0");
    }

    queries = new HashMap<>();

    try (BufferedReader input = new BufferedReader(new FileReader(fileName))) {
      input.lines()
          .limit(nLines)
          .map(this::extractQueryAndDocNameFromLine)
          .forEach(this::addQuery);
    }

    return new HashSet<>(queries.values());
  }

  private String[] extractQueryAndDocNameFromLine(String line) {
    String[] tokens = line.split("\t");
    // ignore 'comments'
    if (line.startsWith("#")) {
      return new String[]{};
    }
    if (!tokens[1].equals("-")) {
      lastQuery = tokens[1].toLowerCase().trim();
    }

    // ignore queries which have no clickthroughs
    // as they don't have any impact on clustering
    if (tokens.length < 5) {
      return new String[]{};
    }
    return new String[]{tokens[1], tokens[4]};
  }

  private void addQuery(String[] query) {
    if (query.length < 2) {
      return;
    }
    // some queries are "-" I treat them as refer to previous query
    if (query[0].equals("-")) {
      query[0] = lastQuery;
    }

    query[0] = query[0].toLowerCase().trim();
    if (query[0].equals("")) {
      return;
    }

    if (!queries.containsKey(query[0])) {
      queries.put(query[0], new Query(query[0]));
    }

    query[1] = query[1].toLowerCase().trim();
    Document doc = new Document(query[1]);
    queries.get(query[0]).addRelatedDocument(doc);
  }
}
