package com.duoc.bff.service;

import com.duoc.bff.domain.CreateProjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/** Tests de los clients HTTP usando MockRestServiceServer sobre el RestClient.Builder. */
class ClientsTest {

    @Test
    void projectsClient_listAndCreate() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new ProjectsClient(builder);

        server.expect(requestTo("http://ms-projects/projects")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\":1,\"name\":\"P\",\"status\":\"PLANNING\"}]", MediaType.APPLICATION_JSON));
        assertThat(client.list()).hasSize(1);

        var builder2 = RestClient.builder();
        var server2 = MockRestServiceServer.bindTo(builder2).build();
        var client2 = new ProjectsClient(builder2);
        server2.expect(requestTo("http://ms-projects/projects")).andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":9,\"name\":\"P\"}", MediaType.APPLICATION_JSON));
        var created = client2.create(new CreateProjectRequest("P", "d", "PLANNING", null, null, null));
        assertThat(created.id()).isEqualTo(9L);
    }

    @Test
    void resourcesClient_listAndByEmail() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new ResourcesClient(builder);
        server.expect(requestTo("http://ms-resources/resources")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\":1,\"name\":\"Ana\",\"email\":\"ana@x.cl\",\"role\":\"DEV\"}]", MediaType.APPLICATION_JSON));
        assertThat(client.list()).hasSize(1);

        var b2 = RestClient.builder();
        var s2 = MockRestServiceServer.bindTo(b2).build();
        var c2 = new ResourcesClient(b2);
        s2.expect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1,\"email\":\"ana@x.cl\",\"role\":\"DEV\"}", MediaType.APPLICATION_JSON));
        assertThat(c2.byEmail("ana@x.cl").email()).isEqualTo("ana@x.cl");

        var b3 = RestClient.builder();
        var s3 = MockRestServiceServer.bindTo(b3).build();
        var c3 = new ResourcesClient(b3);
        s3.expect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThat(c3.byEmail("nadie@x.cl")).isNull();
    }

    @Test
    void kpisClient_getAndHistory() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new KpisClient(builder);
        server.expect(requestTo("http://ms-analytics/analytics/kpis")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));
        assertThat(client.get()).containsEntry("status", "ok");

        var b2 = RestClient.builder();
        var s2 = MockRestServiceServer.bindTo(b2).build();
        var c2 = new KpisClient(b2);
        s2.expect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status\":\"ok\"}", MediaType.APPLICATION_JSON));
        assertThat(c2.history(12, null, null)).containsEntry("status", "ok");
    }

    @Test
    void tasksClient_listAndCreate() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var client = new TasksClient(builder);
        server.expect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\":1,\"projectId\":1,\"status\":\"TODO\"}]", MediaType.APPLICATION_JSON));
        assertThat(client.list(null, 7L, "TODO")).hasSize(1);
    }
}
