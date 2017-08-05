/**
 * Created by sandulmv on 28.07.17.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Main {
    static String pathToLogs = ".$src$main$queryLogs$".replaceAll("\\$", File.separator);
    static String pathToResults = ".$src$main$clusteringResults$".replaceAll("\\$", File.separator);
    static String pathToProgramLogs = ".$src$main$programLogs".replaceAll("\\$", File.separator);
    static BufferedWriter output = null;
    static double threshold = 0.01;
    static int lineLimit = (int) 1e4;
    static int minSizeOfCluster = 5;

    public static void main(String[] args) throws IOException {
        Path queryLogsDirectory = Paths.get(pathToLogs);
        Set<Query> queries = null;
        QueryLogReader queryLogReader = new QueryLogReader();
        Algo algo = new Algo(threshold);
        for (Path logFile : Files.newDirectoryStream(queryLogsDirectory)) {
            queries = queryLogReader.readQueryLog(logFile.toString(), lineLimit);
            File outputFile = new File(pathToResults, logFile.getFileName().toString());
            output = new BufferedWriter(new FileWriter(outputFile));
            makeDescription(output);
            Set<Set<Query>> clusters = algo.clusterQueries(queries);
            int maxSize = -1;
            for (Set<Query> cluster : clusters) {
                if (cluster.size() >= minSizeOfCluster) {
                    if (cluster.size() > maxSize) {
                        maxSize = cluster.size();
                    }
                    output.write(cluster.toString());
                    output.newLine();
                    output.write("----------------------------\n");
                }
            }
            output.write("Maximal size of cluster: " + maxSize);
            output.close();
        }
    }

    static void makeDescription(BufferedWriter output) throws IOException {
        output.write("First " + lineLimit + " clustered");
        output.newLine();
        output.write("Threshold " + threshold);
        output.newLine();
        output.write("Minimal size of cluster " + minSizeOfCluster);
        output.newLine();
        output.newLine();
    }
}
