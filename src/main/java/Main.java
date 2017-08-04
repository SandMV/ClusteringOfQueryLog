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
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;


public class Main {
    static String pathToLogs = ".$src$main$queryLogs$".replaceAll("\\$", File.separator);
    static String pathToResults = ".$src$main$clusteringResults$".replaceAll("\\$", File.separator);
    static String pathToProgramLogs = ".$src$main$programLogs".replaceAll("\\$", File.separator);
    static BufferedWriter output = null;
    static double threshold = 0.001;
    static int lineLimit = (int) 1e4;
    static int minSizeOfCluster = 4;

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
            for (Set<Query> cluster : clusters) {
                if(cluster.size() >= minSizeOfCluster) {
                    output.write(cluster.toString());
                    output.newLine();
                    output.write("----------------------------\n");
                }
            }
            output.close();
        }
    }

    static void makeDescription(BufferedWriter output) throws IOException{
        output.write("First " + lineLimit + " clustered");
        output.newLine();
        output.write("Threshold " + threshold);
        output.newLine();
        output.write("Minimal size of cluster " + minSizeOfCluster);
        output.newLine();
        output.newLine();
    }
}
