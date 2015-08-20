package com.outbrain.ob1k.example.randomcommitmessage.server;

import com.outbrain.ob1k.concurrent.ComposableFuture;
import com.outbrain.ob1k.concurrent.ComposableFutures;
import com.outbrain.ob1k.concurrent.Try;
import com.outbrain.ob1k.concurrent.handlers.FutureProvider;
import com.outbrain.ob1k.example.randomcommitmessage.common.RandomCommitMessageService;
import com.outbrain.ob1k.http.HttpClient;
import com.outbrain.ob1k.http.RequestBuilder;
import com.outbrain.ob1k.http.Response;
import rx.Observable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.outbrain.ob1k.concurrent.ComposableFutures.all;
import static com.outbrain.ob1k.concurrent.ComposableFutures.fromError;
import static com.outbrain.ob1k.concurrent.ComposableFutures.fromValue;

/**
 * A service that will fetch random commit messages from http://whatthecommit.com/
 *
 * @author Eran Harel
 */
public class RandomCommitMessageServiceImpl implements RandomCommitMessageService {

  private final HttpClient httpClient = HttpClient.createDefault();
  private final RequestBuilder requestBuilder = httpClient.get("http://whatthecommit.com/index.txt");

  @Override
  public ComposableFuture<List<String>> multi(final int numMessages) {
    final List<ComposableFuture<String>> futureMessages = new ArrayList<>(numMessages);

    // fetching messages concurrently by the numMessages value
    for (int i = 0; i < numMessages; i++) {
      final ComposableFuture<String> messageFuture = fetchNewMessage();
      futureMessages.add(messageFuture);
    }

    // returning a future that will contain all values
    return all(futureMessages).materialize();
  }

  @Override
  public ComposableFuture<String> single() {
    return fetchNewMessage();
  }

  private ComposableFuture<String> fetchNewMessage() {
    return requestBuilder.asResponse().continueWith((Try<Response> responseTry) -> {
      if (responseTry.isSuccess()) {
        try {
          return fromValue(responseTry.getValue().getResponseBody().trim());
        } catch (IOException e) {
          return fromError(e);
        }
      } else {
        return fromError(responseTry.getError());
      }
    });
  }

  @Override
  public Observable<String> stream(final int numMessages) {
    return ComposableFutures.toObservable(new FutureProvider<String>() {
      private volatile int position = 0;

      @Override
      public boolean moveNext() {
        return ++position <= numMessages;
      }

      @Override
      public ComposableFuture<String> current() {
        return fetchNewMessage();
      }
    });
  }
}
