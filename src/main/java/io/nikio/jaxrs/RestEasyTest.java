package io.nikio.jaxrs;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.junit.After;
import org.junit.Before;

import io.undertow.Undertow;
import io.undertow.servlet.api.DeploymentInfo;

public class RestEasyTest {

    private static final Logger logger = Logger.getLogger(RestEasyTest.class.getName());

    private Client testClient;
    private UndertowJaxrsServer testContainer;
    private int bindPort;
    private String bindUrl = "localhost";

    public RestEasyTest() {
    }

    @Before
    public void restEasyTestSetUp() throws IOException {
        this.bindPort = findFreePort();
        logger.info("Staring test container at " + getBaseUri());
        this.testContainer = createTestContainer(bindPort, bindUrl);
        this.testClient = new ResteasyClientBuilder().build();
    }

    @After
    public void restEasyTestTearDown() {
        if (testClient != null) {
            testClient.close();
            testClient = null;
        }
        if (testContainer != null) {
            testContainer.stop();
            testContainer = null;
        }
    }

    public List<Object> configureResources() {
        return new ArrayList<>();
    }

    public List<Object> configureProvider() {
        return new ArrayList<>();
    }

    public Application configureApplication() {
        return null;
    }

    public String getBaseUri() {
        return "http://" + bindUrl + ":" + bindPort;
    }

    private UndertowJaxrsServer createTestContainer(int bindPort, String bindUrl) {
        UndertowJaxrsServer server = new UndertowJaxrsServer();
        Undertow.Builder serverBuilder = Undertow.builder().addHttpListener(bindPort, bindUrl);
        server.start(serverBuilder);

        ResteasyDeployment resteasyDeployment = new ResteasyDeployment();
        resteasyDeployment.setApplication(configureApplication());
        resteasyDeployment.setResources(configureResources());
        resteasyDeployment.setProviders(configureProvider());

        DeploymentInfo deploymentInfo = server.undertowDeployment(resteasyDeployment);
        deploymentInfo.setClassLoader(RestEasyTest.class.getClassLoader());
        deploymentInfo.setDeploymentName(this.getClass().getName());
        deploymentInfo.setContextPath("/");

        server.deploy(deploymentInfo);
        logger.info("Undertow with resteasy and jackson is running.");

        return server;
    }

    public <T> T get(String uri, Class<T> responseType) {
        return request(uri).request().get(responseType);
    }

    public <T, E> T post(String uri, Object entity, Class<T> responseType) {
        Entity<Object> entityObject = Entity.entity(entity, MediaType.APPLICATION_JSON_TYPE);
        return request(uri).request().post(entityObject, responseType);
    }

    public WebTarget request(String uri) {
        return testClient.target(getBaseUri() + uri);
    }

    /**
     * Find a free server port.
     *
     * @return port number.
     * @throws IOException
     */
    public static int findFreePort() throws IOException {
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }

}
