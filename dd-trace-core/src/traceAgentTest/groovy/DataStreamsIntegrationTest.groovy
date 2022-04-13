import datadog.communication.ddagent.DDAgentFeaturesDiscovery
import datadog.communication.ddagent.SharedCommunicationObjects
import datadog.communication.http.OkHttpUtils
import datadog.trace.api.Config
import datadog.trace.api.time.ControllableTimeSource
import datadog.trace.bootstrap.instrumentation.api.StatsPoint
import datadog.trace.common.metrics.EventListener
import datadog.trace.common.metrics.OkHttpSink
import datadog.trace.core.datastreams.DefaultDataStreamsCheckpointer
import datadog.trace.test.util.DDSpecification
import okhttp3.HttpUrl
import spock.lang.Ignore
import spock.lang.Requires
import spock.util.concurrent.PollingConditions

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

import static datadog.trace.api.Platform.isJavaVersionAtLeast
import static datadog.trace.common.metrics.EventListener.EventType.OK
import static datadog.trace.core.datastreams.DefaultDataStreamsCheckpointer.DEFAULT_BUCKET_DURATION_MILLIS

@Requires({
  "true" == System.getenv("CI") && isJavaVersionAtLeast(8)
})
@Ignore("The agent in CI doesn't have a valid API key. Unlike metrics and traces, data streams fails in this case")
class DataStreamsIntegrationTest extends DDSpecification {

  def "Sending stats bucket to agent should notify with OK event"() {
    given:
    def conditions = new PollingConditions(timeout: 1)

    def sharedCommunicationObjects = new SharedCommunicationObjects()
    sharedCommunicationObjects.createRemaining(Config.get())

    OkHttpSink sink = new OkHttpSink(
      OkHttpUtils.buildHttpClient(HttpUrl.parse(Config.get().getAgentUrl()), 5000L),
      Config.get().getAgentUrl(),
      DDAgentFeaturesDiscovery.V01_DATASTREAMS_ENDPOINT,
      false,
      true,
      [:])

    def listener = new BlockingListener()
    sink.register(listener)

    def timeSource = new ControllableTimeSource()

    when:
    def checkpointer = new DefaultDataStreamsCheckpointer(sink, sharedCommunicationObjects.featuresDiscovery, timeSource, Config.get().getEnv())
    checkpointer.start()
    checkpointer.accept(new StatsPoint("testType", "testGroup", "testTopic", 1, 2, timeSource.currentTimeMillis, 0, 0))
    timeSource.advance(TimeUnit.MILLISECONDS.toNanos(DEFAULT_BUCKET_DURATION_MILLIS))
    checkpointer.report()

    then:
    sharedCommunicationObjects.featuresDiscovery.supportsDataStreams()
    conditions.eventually {
      assert listener.events.size() == 1
    }
    listener.events[0] == OK

    cleanup:
    checkpointer.close()
  }

  static class BlockingListener implements EventListener {
    List<EventType> events = new CopyOnWriteArrayList<>()

    @Override
    void onEvent(EventType eventType, String message) {
      events.add(eventType)
    }
  }
}
