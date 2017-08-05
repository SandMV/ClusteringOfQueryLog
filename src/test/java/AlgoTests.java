/**
 * Created by sandulmv on 31.07.17.
 */

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class AlgoTests {

  private final String testLogsDirectory = ".$src$test$testLogs$".replaceAll("\\$", File.separator);
  private final QueryTestLogReader input = new QueryTestLogReader();

  @Test
  public void testSingleQuery() throws IOException {
    Set<Query> queries = input.readQueryLog(testLogsDirectory + "singleQueryTest");
    Algo algo = new Algo();
    Set<Set<Query>> qClusters = algo.clusterQueries(queries);
    Assert.assertEquals(1, qClusters.size());
  }

  @Test
  public void testSingleQueryAndDocument() throws IOException {
    Set<Query> queries = input.readQueryLog(testLogsDirectory + "singleQueryAndDocumentTest");
    Algo algo = new Algo();
    Set<Set<Query>> qClusters = algo.clusterQueries(queries);
    Assert.assertEquals(1, qClusters.size());
  }

  @Test
  public void testQueriesRefersToSingleDocument() throws IOException {
    Set<Query> queries = input.readQueryLog(testLogsDirectory + "queriesAndDocumentTest");
    Algo algo = new Algo();
    Set<Set<Query>> qClusters = algo.clusterQueries(queries);
    Assert.assertEquals(1, qClusters.size());
  }

  @Test
  public void testOnlyQueries() throws IOException {
    Set<Query> queries = input.readQueryLog(testLogsDirectory + "lotsQueriesTest");
    Algo algo = new Algo();
    Set<Set<Query>> qClusters = algo.clusterQueries(queries);
    Assert.assertEquals(queries.size(), qClusters.size());
  }

  @Test
  public void testQueryAndDocuments() throws IOException {
    Set<Query> queries = input.readQueryLog(testLogsDirectory + "queryAndDocumentsTest");
    Algo algo = new Algo();
    Set<Set<Query>> qClusters = algo.clusterQueries(queries);
    Assert.assertEquals(1, qClusters.size());
  }

  @Test
  public void testEmptySet() {
    Set<Query> queries = new HashSet<>();
    Algo algo = new Algo();
    Set<Set<Query>> qCluster = algo.clusterQueries(queries);
    Assert.assertEquals(0, qCluster.size());
  }
}