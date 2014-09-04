package adhoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class SequentialMain {

  // maven-dependencies.lst http://repo.maven.apache.org/maven2/
  // length 41336013 bytes, time 88286 ms, rate 457.23 KiB/s
  // length 41336013 bytes, time 118904 ms, rate 339.49 KiB/s
  // length 41336013 bytes, time 69757 ms, rate 578.68 KiB/s

  // maven-dependencies.lst https://repo.maven.apache.org/maven2/
  // length 41336013 bytes, time 80001 ms, rate 504.58 KiB/s

  public static void main(String[] args) throws IOException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    long length = new SequentialMain().downloadAll(Fixture.load("maven-dependencies.lst"));
    long time = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
    float rate = (1000f / 1024f) * ((float) length) / ((float) time);
    System.out.format("length %d bytes, time %d ms, rate %.2f KiB/s", length, time, rate);
  }

  private long downloadAll(List<Collection<String>> batches) throws IOException {
    final OkHttpClient client = new OkHttpClient();

    long length = 0;
    for (Collection<String> batch : batches) {
      length += downloadSync(client, batch);
    }

    return length;
  }

  private long downloadSync(OkHttpClient client, Collection<String> batch) throws IOException {
    long length = 0;
    for (String resource : batch) {
      length += downloadSync(client, Fixture.BASEURL + resource);
      length += downloadSync(client, Fixture.BASEURL + resource + ".sha1");
    }
    return length;
  }

  private long downloadSync(OkHttpClient client, String url) throws IOException {
    Request request = new Request.Builder() //
        .url(url) //
        .build();
    Response response = client.newCall(request).execute();
    if (!response.isSuccessful()) {
      throw new IOException("HTTP " + response.code() + "/" + response.message());
    }
    long length;
    try (InputStream is = response.body().byteStream()) {
      length = ByteStreams.copy(is, ByteStreams.nullOutputStream());
    }
    System.out.format("%s %d\n", url, length);
    return length;
  }
}
