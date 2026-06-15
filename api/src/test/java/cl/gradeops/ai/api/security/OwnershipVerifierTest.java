package cl.gradeops.ai.api.security;

import cl.gradeops.ai.api.common.GlobalExceptionHandler;
import cl.gradeops.ai.api.common.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OwnershipVerifierTest {

    private final OwnershipVerifier verifier = new OwnershipVerifier();

    @Test
    void matching_uids_do_not_throw() {
        assertThatCode(() -> verifier.verify("uid-alice", "uid-alice", "resource-1"))
                .doesNotThrowAnyException();
    }

    @Test
    void different_uids_throw_resource_not_found_exception() {
        assertThatThrownBy(() -> verifier.verify("uid-alice", "uid-bob", "resource-1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .satisfies(ex -> {
                    ResourceNotFoundException rnfe = (ResourceNotFoundException) ex;
                    assertThatCode(() -> {
                        assert rnfe.getResourceId().equals("resource-1");
                    }).doesNotThrowAnyException();
                });
    }

    /**
     * Integration slice: verifies that ResourceNotFoundException thrown from any controller
     * produces HTTP 404 with {"error":"NOT_FOUND","resource":"<id>"}.
     */
    @WebMvcTest(controllers = OwnershipVerifierTest.ThrowingController.class,
                excludeAutoConfiguration = {
                    org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
                    org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration.class
                })
    @Import(GlobalExceptionHandler.class)
    static class GlobalExceptionHandlerNotFoundTest {

        @Autowired MockMvc mockMvc;

        @Test
        void resource_not_found_exception_maps_to_404_with_body() throws Exception {
            mockMvc.perform(get("/test/not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.resource").value("test-resource-id"));
        }
    }

    @RestController
    static class ThrowingController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("test-resource-id");
        }
    }
}
