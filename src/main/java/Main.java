/**
 * Created by sandulmv on 28.07.17.
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.Handler;


public class Main {
    static String pathToLogs = ".$src$main$queryLogs$".replaceAll("\\$", File.separator);
    static String pathToResults = ".$src$main$clusteringResults$".replaceAll("\\$", File.separator);
    static String pathToProgramLogs = ".$src$main$programLogs".replaceAll("\\$", File.separator);

    public static void main(String[] args) throws IOException {
        Path queryLogsDirectory = Paths.get(pathToLogs);
        Set<Query> queries = null;
        for(Path logFile : Files.newDirectoryStream(queryLogsDirectory)) {
            queries = (new QueryLogReader()).readQueryLog(logFile.toString(), (int)1e4);
            break;
        }
        Set<Set<Query>> clusters = (new Algo()).clusterQueries(queries);
        for(Set<Query> cluster : clusters) {
            if(cluster.size() >= 5) {
                System.out.println(cluster);
                System.out.println("========================");
            }
        }
    }
}
