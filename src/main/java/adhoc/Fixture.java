package adhoc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

public class Fixture {
  public static final String BASEURL = "http://repo.maven.apache.org/maven2/";

  public static List<Collection<String>> load(String filename) throws IOException {
    File file = new File(filename);
    return Files.readLines(file, Charsets.UTF_8, new LineProcessor<List<Collection<String>>>() {
      List<Collection<String>> batches = new ArrayList<>();

      public boolean processLine(String line) throws IOException {
        List<String> batch = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(line, " ");
        while (st.hasMoreTokens()) {
          batch.add(st.nextToken());
        }
        batches.add(batch);
        return true;
      }

      public List<Collection<String>> getResult() {
        return batches;
      }
    });
  }
}
