package ecom.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import ecom.user.dto.request.CreateUserRequest;
import ecom.user.dto.request.UpdateUserRequest;
import ecom.user.dto.response.UserResponse;
import ecom.user.exception.DuplicateResourceException;
import ecom.user.exception.ResourceNotFoundException;
import ecom.user.service.UserService;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
class UserControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private UserResponse sampleResponse() {
        Date now = new Date();
        return new UserResponse(1L, "Ada", "+911234567890", "ada@example.com", now, now, true);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void createUser_returns201WithLocationAndNoPassword() throws Exception {
        when(userService.createUser(any())).thenReturn(sampleResponse());
        CreateUserRequest request =
                new CreateUserRequest("Ada", "+911234567890", "ada@example.com", "password1");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/users/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUser_returns400OnInvalidBody() throws Exception {
        CreateUserRequest request =
                new CreateUserRequest("", "abc", "not-an-email", "short");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void createUser_returns409WhenServiceReportsDuplicate() throws Exception {
        when(userService.createUser(any())).thenThrow(DuplicateResourceException.email("ada@example.com"));
        CreateUserRequest request =
                new CreateUserRequest("Ada", "+911234567890", "ada@example.com", "password1");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getUserById_returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUserById_returns404WhenMissing() throws Exception {
        when(userService.getUserById(99L)).thenThrow(ResourceNotFoundException.forUser(99L));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/users/99"));
    }

    @Test
    void getUserById_returns400OnNonNumericId() throws Exception {
        mockMvc.perform(get("/api/users/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_returns200WithArray() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void updateUser_returns200() throws Exception {
        when(userService.updateUser(eq(1L), any())).thenReturn(sampleResponse());
        UpdateUserRequest request =
                new UpdateUserRequest("Ada", "+911234567890", "ada@example.com", "password1");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateUser_returns404WhenMissing() throws Exception {
        when(userService.updateUser(eq(1L), any())).thenThrow(ResourceNotFoundException.forUser(1L));
        UpdateUserRequest request =
                new UpdateUserRequest("Ada", "+911234567890", "ada@example.com", "password1");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_returns400OnInvalidBody() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("", "x", "bad", "s");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_returns204() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_returns404WhenMissing() throws Exception {
        doThrow(ResourceNotFoundException.forUser(1L)).when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound());
    }
}
