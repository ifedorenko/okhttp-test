package adhoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class AsyncMain {

  // maven-dependencies.lst https://repo.maven.apache.org/maven2/
  // length 41302452 bytes, time 76802 ms, rate 525.17 KiB/s
  
  // maven-dependencies.lst http://repo.maven.apache.org/maven2/
  // length 41302452 bytes, time 74148 ms, rate 543.97 KiB/s
  // length 41302452 bytes, time 123689 ms, rate 326.10 KiB/s
  // length 41302452 bytes, time 70723 ms, rate 570.32 KiB/s

  public static void main(String[] args) throws IOException, InterruptedException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    long length = new AsyncMain().downloadAll(Fixture.load("maven-dependencies.lst"));
    long time = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
    float rate = (1000f / 1024f) * ((float) length) / ((float) time);
    System.out.format("length %d bytes, time %d ms, rate %.2f KiB/s", length, time, rate);
  }

  private long downloadAll(List<Collection<String>> batches) throws IOException,
      InterruptedException {
    final OkHttpClient client = new OkHttpClient();

    long length = 0;
    for (Collection<String> batch : batches) {
      length += downloadAsync(client, batch);
    }

    return length;
  }

  private long downloadAsync(OkHttpClient client, Collection<String> batch)
      throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(batch.size() * 2);
    final AtomicLong totalLength = new AtomicLong();

    for (String resource : batch) {
      downloadAsync(client, latch, totalLength, Fixture.BASEURL + resource);
      downloadAsync(client, latch, totalLength, Fixture.BASEURL + resource + ".sha1");
    }

    latch.await();
    return totalLength.longValue();
  }

  private void downloadAsync(OkHttpClient client, final CountDownLatch latch,
      final AtomicLong totalLength, String url) {
    Request request = new Request.Builder() //
        .url(url) //
        .build();

    client.newCall(request).enqueue(new Callback() {

      @Override
      public void onResponse(Response response) throws IOException {
        latch.countDown();

        long length;
        try (InputStream is = response.body().byteStream()) {
          length = ByteStreams.copy(is, ByteStreams.nullOutputStream());
        }
        System.out.format("%s %s %d\n", response.protocol(), response.request().urlString(), length);
        totalLength.addAndGet(length);
      }

      @Override
      public void onFailure(Request request, IOException e) {
        latch.countDown();

        e.printStackTrace();
      }
    });
  }

}
