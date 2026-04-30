---
title: "A brief introduction to Quarkus Flow"
date: 2026-04-28 19:00:00 +0000
tags: [blogging, quarkus-flow, flow, workflow]
description: "Take your first look at Quarkus Flow and discover how it helps orchestrate complex workflows with simplicity and flexibility"
toc: true
author: quarkusguru
image: https://images.unsplash.com/photo-1580203784276-6ded72fea88a?q=80&w=2148&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D
---

__This post was originally published on the [Quarkus Guru blog](https://quarkusguru.github.io/posts/a-brief-introduction-to-quarkus-flow/)__

## 1. Overview

In modern cloud-native applications, orchestrating complex business processes across multiple services is a common challenge. Traditional workflow engines often come with vendor lock-in, heavyweight runtimes, and limited cloud-native capabilities.

Quarkus Flow addresses these challenges by providing a lightweight, vendor-neutral workflow engine built on the CNCF Serverless Workflow specification. In this tutorial, we'll explore how to create, execute, and manage workflows using both YAML and Java DSLs.

By the end of this tutorial, you'll understand:
- How to set up Quarkus Flow in your project
- Creating workflows with YAML and Java DSLs
- Implementing REST endpoints to trigger workflows
- Handling errors
- Configuring resilience for HTTP calls

## 2. Prerequisites

Before starting this tutorial, ensure you have:

- **Java 17 or later** installed
- **Maven 3.8+** for dependency management
- **Quarkus CLI** (optional but recommended) [installation guide](https://quarkus.io/guides/cli-tooling)

## 4. CNCF Serverless Workflow

Quarkus Flow, unlike other workflow engines (e.g., Camunda, Temporal, jBPM), is based on the CNCF Serverless Workflow Specification (Serverless Workflow). This approach helps you escape vendor lock-in. The **Serverless Workflow** has a [rich DSL](https://github.com/serverlessworkflow/specification/blob/main/dsl.md) that you can use to write your workflows.

The specification provides a vendor-neutral way to define workflows that can be executed across different platforms and cloud providers. This portability is crucial for organizations that want to avoid being tied to a specific vendor's ecosystem.

## 5. Setup

Let's create a project with [Quarkus CLI](https://quarkus.io/guides/cli-tooling):

```shell
quarkus create app guru.quarkus:qflow -xrest-jackson
```

This command creates a Quarkus project with the [REST Jackson](https://quarkus.io/guides/rest#what-is-quarkus-rest) extension installed.

If you prefer, you can use [Code QuarkusIO](https://code.quarkus.io) to generate a Quarkus project with a visual interface.

Next, add the `io.quarkiverse.flow:quarkus-flow` dependency into the `pom.xml` file:

```xml
<dependency>
    <groupId>io.quarkiverse.flow</groupId>
    <artifactId>quarkus-flow</artifactId>
    <version>0.9.0</version>
</dependency>
```

**NOTE:** At this moment, the current Quarkus Flow version is 0.9.0!

After adding the dependency, your project structure should look like this:

```
qflow/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── guru/quarkus/
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/
└── pom.xml
```

Create the `src/main/flow` directory where we'll place our YAML workflow definitions:

```shell
mkdir -p src/main/flow
```

## 6. First Workflow with YAML DSL

Let's create a workflow using the YAML DSL called `jediWorkflow.yaml` under the `src/main/flow` directory:

```yaml
document:
  dsl: 1.0.0
  namespace: quarkus.guru
  name: jediWorkflow
  version: 0.1.0
do:
  - getStarWarsPeople:
      call: http
      with:
        method: GET
        endpoint: 'https://swapi.info/api/people/\{peopleId}'
```

### 6.1. Understanding the YAML DSL

#### Workflow

A [Workflow](https://github.com/serverlessworkflow/specification/blob/main/dsl.md#workflow) is a sequence of tasks that are executed in a defined order. A workflow has [status](https://github.com/serverlessworkflow/specification/blob/main/dsl.md#status-phases) and [lifecycle events](https://github.com/serverlessworkflow/specification/blob/main/dsl.md#lifecycle-events).

The `document` section defines metadata about the workflow:
- `dsl`: The version of the Serverless Workflow DSL being used
- `namespace`: A logical grouping for workflows (similar to Java packages)
- `name`: The unique identifier for this workflow
- `version`: The version of this workflow definition

#### Tasks

[Tasks](https://github.com/serverlessworkflow/specification/blob/main/dsl.md#task) are the fundamental units of work within a **Workflow**. They allow you to perform actions, manipulate data, and control execution flow. The Serverless Workflow DSL defines several built-in task types, such as `call`, `do`, `emit`, `for`, `try`, and `raise`, among others.

In our example:
- `getStarWarsPeople` is the task name
- `call: http` indicates we are using the [HTTP](https://github.com/serverlessworkflow/specification/blob/main/dsl-reference.md#http-call) call task
- `with` contains the parameters for the HTTP call task
- The `\{peopleId}` placeholder will be replaced with the workflow's input value at runtime

### 6.2. Creating the REST Endpoint

Now, let's create a REST endpoint to trigger our workflow. Create a `JediResource` class:

```java
package guru.quarkus.qflow;

import io.quarkiverse.flow.Flow;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/jedi")
@Produces(MediaType.APPLICATION_JSON)
public class JediResource {

    private static final Logger LOG = Logger.getLogger(JediResource.class);

    @Inject
    @Identifier("quarkusguru.JediWorkflow")
    Flow yamlWorkflow;

    @GET
    @Path("/\{id}")
    public Uni<Response> getJedi(@PathParam("id") String jediId) {
        LOG.infof("Fetching Jedi with ID: %s", jediId);

        return yamlWorkflow.startInstance(Map.of("peopleId", jediId))
                .onItem().transform(model -> {
                    String result = model.asText().orElse("{}");
                    return Response.ok(result).build();
                });
    }
}
```

Let's break down this implementation:

1. **Dependency Injection**: The `@Inject Flow yamlWorkflow` automatically injects the workflow instance generated from our YAML file. Quarkus Flow automatically discovers YAML files in the `src/main/flow` directory and creates corresponding CDI beans.

2. **Reactive Programming**: The endpoint returns a reactive response using Uni<Response>. Internally, the Flow API operates with CompletionStage<WorkflowModel>, exposed through `flow.instance(...).start()`.

3. **Starting the Workflow**: `jediWorkflow.startInstance(Map.of("peopleId", jediId))` starts a new workflow instance with the provided input parameters.

4. **Error Handling**: If the workflow execution encounters an error, Quarkus Flow automatically translates the internal `WorkflowException` into an [RFC 7807 Problem Details](https://datatracker.ietf.org/doc/html/rfc7807) HTTP response.

### 6.3. Testing the Workflow

Start your Quarkus application in dev mode:

```shell
quarkus dev
```

Or using Maven:

```shell
./mvnw quarkus:dev
```

Let's execute an HTTP request to call the `jediWorkflow`:

```shell
curl http://localhost:8080/jedi/1
```

The output should look something like this:

```json
{
  "name": "Luke Skywalker",
  "height": "172",
  "mass": "77",
  "hair_color": "blond",
  "skin_color": "fair",
  "eye_color": "blue",
  "birth_year": "19BBY",
  "gender": "male",
  "homeworld": "https://swapi.info/api/planets/1",
  "films": [
    "https://swapi.info/api/films/1",
    "https://swapi.info/api/films/2",
    "https://swapi.info/api/films/3",
    "https://swapi.info/api/films/6"
  ],
  "species": [],
  "vehicles": [
    "https://swapi.info/api/vehicles/14",
    "https://swapi.info/api/vehicles/30"
  ],
  "starships": [
    "https://swapi.info/api/starships/12",
    "https://swapi.info/api/starships/22"
  ],
  "created": "2014-12-09T13:50:51.644000Z",
  "edited": "2014-12-20T21:17:56.891000Z",
  "url": "https://swapi.info/api/people/1"
}
```

## 7. First Workflow with Java DSL

In most cases, you will be writing Java code rather than YAML. YAML remains simpler and can cover a wide range of workflow use cases. However, the Serverless Workflow DSL for Java is expressive and, when combined with Quarkus Flow, leverages several Quarkus and Java specifications, including Arc, MicroProfile Config, and MicroProfile Reactive Messaging, among others.

It is possible to create a workflow with the Java DSL. A workflow is implemented as a simple CDI bean that extends the `Flow` class:

```java
package guru.quarkus.qflow;

import io.quarkiverse.flow.Flow;
import io.quarkus.logging.Log;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.fluent.func.FuncWorkflowBuilder;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.function;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.http;

@ApplicationScoped
public class JediWorkflow extends Flow {

    @Override
    public Workflow descriptor() {
        return FuncWorkflowBuilder.workflow("jediWorkflow", "quarkusguru")
                .tasks(
                        http("getStarWarsPeople")
                                .GET()
                                .endpoint("https://swapi.info/api/people/\{peopleId}"),
                        function("processPerson", people -> {
                            Log.infof("We got %s from getStarWarsPeople", people);
                            return people;
                        }, People.class)
                )
                .build();
    }

    public record People(String name, List<String> films) {
    }
}
```

### 7.1. Understanding the Java DSL

Similarly to the YAML DSL, we define a workflow with the name `jediWorkflow` and the namespace `quarkusguru`.

This workflow contains tasks, but unlike the YAML version, we explicitly define two tasks:

1. **HTTP Task**: The first task performs the same operation as `getStarWarsPeople` in the YAML DSL. It makes a GET request to the Star Wars API.

2. **Function Task**: The second is a functional task that processes the output of `getStarWarsPeople` and logs the result. This demonstrates how you can chain tasks together in a workflow.

If you noticed, the response body (the output from the `getStarWarsPeople` task) is automatically serialized into the `People` record. This approach is particularly useful when you need type safety across your workflow and its tasks. The Java DSL provides compile-time type checking, IDE support, and refactoring capabilities that YAML cannot offer.

### 7.2. Using the JediWorkflow

Similarly to what we did for the generated bean from the `jediWorkflow.yaml` file, we can inject the `JediWorkflow` bean inside the `JediResource`.

Let's update the `JediResource` to use our Java-based workflow:

```java
package guru.quarkus.qflow;

import io.quarkiverse.flow.runtime.FlowInstance;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/jedi")
@Produces(MediaType.APPLICATION_JSON)
public class JediResource {

    private static final Logger LOG = Logger.getLogger(JediResource.class);

    @Inject
    @Identifier("quarkusguru.JediWorkflow")
    Flow yamlWorkflow;

    @Inject
    JediWorkflow jediWorkflowJava;

    @GET
    @Path("/yaml/\{id}")
    public Uni<Response> getJedi(@PathParam("id") String jediId) {
        LOG.infof("Fetching Jedi with ID: %s", jediId);

        return jediWorkflow.startInstance(Map.of("peopleId", jediId))
                .onItem().transform(model -> {
                    String result = model.asText().orElse("{}");
                    return Response.ok(result).build();
                });
    }

    @GET
    @Path("/java/\{id}")
    public Uni<Response> getJediJava(@PathParam("id") String jediId) {
        LOG.infof("Fetching Jedi with ID using Java DSL: %s", jediId);

        return jediWorkflowJava.startInstance(Map.of("peopleId", jediId))
                .onItem().transform(model -> {
                    String result = model.asText().orElse("{}");
                    return Response.ok(result).build();
                });
    }
}
```

### 7.3. Testing the Java DSL Workflow

Let's execute an HTTP request to call the Java-based `jediWorkflow`:

```shell
curl http://localhost:8080/jedi/java/1
```

The output should look something like this:

```json
{
  "name": "Luke Skywalker",
  "films": [
    "https://swapi.info/api/films/1",
    "https://swapi.info/api/films/2",
    "https://swapi.info/api/films/3",
    "https://swapi.info/api/films/6"
  ]
}
```

Notice that the output is different from the YAML version. This is because our `People` record only includes the `name` and `films` fields, demonstrating how the Java DSL allows you to control data transformation and filtering within your workflow.

## 8. Error Handling

Robust error handling is crucial for production workflows. Quarkus Flow provides several mechanisms to handle errors gracefully.

### 8.1. Try-Catch in Workflows

You can add error handling directly in your workflow using the `try` task. Here's an example with YAML:

```yaml
document:
  dsl: 1.0.0
  namespace: quarkus.guru
  name: jediWorkflowWithErrorHandling
  version: 0.1.0
do:
  - tryGetJedi:
      try:
        - getStarWarsPeople:
            call: http
            with:
              method: GET
              endpoint: 'https://swapi.info/api/people/\{peopleId}'
      catch:
        errors:
          with:
            type: https://example.com/errors/transient
            status: 503
        do:
          - handleError:
              set:
                recovered: true
```

And the equivalent in Java DSL:

```java
package guru.quarkus.qflow;

import io.quarkiverse.flow.Flow;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.fluent.spec.WorkflowBuilder;
import io.serverlessworkflow.fluent.spec.dsl.DSL;
import jakarta.enterprise.context.ApplicationScoped;

import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.http;

@ApplicationScoped
public class JediWorkflowWithErrorHandling extends Flow {

    @Override
    public Workflow descriptor() {
        return WorkflowBuilder.workflow("jediWorkflowWithErrorHandling", "quarkusguru")
                .tasks(
                        DSL.tryCatch("tryGetJedi", t ->
                            t.tryHandler(_ -> http("getStarWarsPeople")
                                .GET()
                                .endpoint("https://swapi.info/api/people/\{peopleId}"))
                                .catchHandler(catchHandler -> {
                            catchHandler.errorsWith(error -> error.type("https://example.com/errors/transient"))
                                    .doTasks(tasks -> tasks.set("handleError", s -> s.put("recovered", true)));
                        }))
                ).build();
    }
}
```

### 8.2. Retry Strategies

Implement retry logic for transient failures. This is particularly useful when dealing with external APIs that might have temporary issues, you can [configure retry strategies to HTTP Client](https://docs.quarkiverse.io/quarkus-flow/dev/fault-tolerance.html):

```properties
quarkus.flow.http.client.resiliency.retry.max-retries=3
quarkus.flow.http.client.resiliency.retry.delay=2s
quarkus.flow.http.client.resiliency.retry.jitter=200ms
```

This configuration will:
- Retry up to 3 times if the HTTP call fails
- Wait 2 seconds before the first retry
- Add up to 200ms of jitter between retries to reduce contention

### 8.3. Timeout Handling

Set timeouts to prevent workflows from hanging indefinitely:

```java
http("getStarWarsPeople")
    .GET()
    .endpoint("https://swapi.info/api/people/\{peopleId}")
    .timeout("PT2S")
```

This ensures that if the HTTP call takes longer than 2 seconds, it will be terminated and an error will be raised.


## 9. Monitoring and Debugging

Understanding workflow execution is crucial for troubleshooting and optimization.

### 9.1. Metrics and Observability

Quarkus Flow integrates with **Quarkus Micrometer** to provide comprehensive observability for workflow executions. It exposes metrics for execution counts, durations, and runtime states that can be visualized with Prometheus and Grafana.

#### 9.1.1. Enabling Metrics

Add the Micrometer Prometheus dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-micrometer-registry-prometheus</artifactId>
</dependency>
```

No additional configuration is required, metrics are automatically exposed through the Prometheus endpoint at:

```
http://localhost:8080/q/metrics
```

To disable metrics, either remove the dependency or set:

```properties
quarkus.flow.metrics.enabled=false
```

#### 9.1.2. Available Metrics

Quarkus Flow provides several metrics aligned with the CNCF Serverless Workflow specification. These metrics allow you to track workflow events, understand what is happening during execution, and measure how long workflows and individual tasks take to complete.

For more information see the [Observability with Prometheus and Micrometer](https://docs.quarkiverse.io/quarkus-flow/dev/metrics-prometheus.html) documentation.


### 9.2. Tracing

Quarkus Flow provides lightweight tracing to monitor and debug workflows from start to finish, tracking instances through internal tasks and across network calls to external services.

**Local Tracing (MDC Logging):**
Emits structured lifecycle logs with Mapped Diagnostic Context (MDC) fields for filtering by instance ID in log aggregation tools like Kibana, Datadog, or Loki.

#### 9.2.2. Configuration

Enable tracing in production (enabled by default in dev/test modes):

```properties
quarkus.flow.tracing.enabled=true
```

#### 9.2.3. HTTP Header Propagation

Quarkus Flow automatically attaches correlation metadata to outgoing HTTP calls:

- **X-Flow-Instance-Id**: Matches `quarkus.flow.instanceId` from MDC logs
- **X-Flow-Task-Id**: Matches `quarkus.flow.taskPos` from MDC logs

**Benefits:**
- Enables idempotency keys in downstream services
- Ensures retry attempts are recognized as the same execution step
- Provides end-to-end traceability across distributed systems

To disable header propagation if needed:

```properties
quarkus.flow.http.client.enable-metadata-propagation=false
```

### 9.3. Structured Logging

Quarkus Flow supports structured logging, exporting detailed workflow execution data as JSON logs for easier querying and analysis:

```properties
quarkus.flow.structured-logging.enabled=true
```

For more details, see the [official Quarkus Flow structured logging documentation](https://docs.quarkiverse.io/quarkus-flow/dev/structured-logging.html).

## 10. Conclusion

In this tutorial, we explored Quarkus Flow, a lightweight and vendor-neutral workflow engine built on the CNCF Serverless Workflow specification. We covered:

- Setting up a Quarkus project with Quarkus Flow
- Creating workflows using both YAML and Java DSLs
- Implementing REST endpoints to trigger workflows
- Handling errors and configuring HTTP retry strategies
- Monitoring and debugging workflow execution

Quarkus Flow combines the power of the Serverless Workflow specification with Quarkus's cloud-native capabilities, making it an excellent choice for building modern, scalable workflow applications. Its vendor-neutral approach ensures you're not locked into a specific platform, while its integration with Quarkus provides native compilation, fast startup times, and low memory footprint.

### 10.1 Further Reading

- [Quarkus Flow Documentation](https://docs.quarkiverse.io/quarkus-flow/dev/index.html)
- [CNCF Serverless Workflow Specification](https://github.com/serverlessworkflow/specification)
- [Quarkus Guides](https://quarkus.io/guides)
