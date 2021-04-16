package datadog.trace.instrumentation.dropwizard.view;

import static datadog.trace.agent.tooling.ClassLoaderMatcher.hasClassesNamed;
import static datadog.trace.agent.tooling.bytebuddy.matcher.DDElementMatchers.implementsInterface;
import static datadog.trace.agent.tooling.bytebuddy.matcher.NameMatchers.named;
import static datadog.trace.agent.tooling.bytebuddy.matcher.NameMatchers.namedOneOf;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activateSpan;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.activeSpan;
import static datadog.trace.bootstrap.instrumentation.api.AgentTracer.startSpan;
import static java.util.Collections.singletonMap;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

import com.google.auto.service.AutoService;
import datadog.trace.agent.tooling.Instrumenter;
import datadog.trace.bootstrap.instrumentation.api.AgentScope;
import datadog.trace.bootstrap.instrumentation.api.AgentSpan;
import datadog.trace.bootstrap.instrumentation.api.Tags;
import io.dropwizard.views.View;
import java.util.Map;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

@AutoService(Instrumenter.class)
public final class DropwizardViewInstrumentation extends Instrumenter.Tracing {

  public DropwizardViewInstrumentation() {
    super(true, "dropwizard", "dropwizard-view");
  }

  @Override
  public ElementMatcher<ClassLoader> classLoaderMatcher() {
    // Optimization for expensive typeMatcher.
    return hasClassesNamed("io.dropwizard.views.ViewRenderer");
  }

  @Override
  public ElementMatcher<TypeDescription> shortCutMatcher() {
    return namedOneOf(
        "io.dropwizard.views.freemarker.FreemarkerViewRenderer",
        "io.dropwizard.views.mustache.MustacheViewRenderer");
  }

  @Override
  public ElementMatcher<TypeDescription> hierarchyMatcher() {
    return implementsInterface(named("io.dropwizard.views.ViewRenderer"));
  }

  @Override
  public Map<? extends ElementMatcher<? super MethodDescription>, String> transformers() {
    return singletonMap(
        isMethod()
            .and(named("render"))
            .and(takesArgument(0, named("io.dropwizard.views.View")))
            .and(isPublic()),
        DropwizardViewInstrumentation.class.getName() + "$RenderAdvice");
  }

  public static class RenderAdvice {

    @Advice.OnMethodEnter(suppress = Throwable.class)
    public static AgentScope onEnter(
        @Advice.This final Object obj, @Advice.Argument(0) final View view) {
      if (activeSpan() == null) {
        return null;
      }
      final AgentSpan span = startSpan("view.render").setTag(Tags.COMPONENT, "dropwizard-view");
      span.setResourceName("View " + view.getTemplateName());
      return activateSpan(span);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
    public static void stopSpan(
        @Advice.Enter final AgentScope scope, @Advice.Thrown final Throwable throwable) {
      if (scope == null) {
        return;
      }
      final AgentSpan span = scope.span();
      if (throwable != null) {
        span.setError(true);
        span.addThrowable(throwable);
      }
      scope.close();
      span.finish();
    }
  }
}
