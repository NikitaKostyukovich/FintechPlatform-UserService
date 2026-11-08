package com.innowise.userservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createUser_validBody_returns201() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "Petrov", "ivan@test.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.surname").value("Petrov"))
                .andExpect(jsonPath("$.email").value("ivan@test.com"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createUser_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("", "Petrov", "ivan@test.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Ivan", "Petrov", "not-an-email")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_duplicateEmail_returns409() throws Exception {
        createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Petr", "Ivanov", "ivan@test.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_existing_returns200() throws Exception {
        long id = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(get("/api/users/{id}", id)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value("ivan@test.com"));
    }

    @Test
    void getUserById_unknown_returns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999999)
                        .with(adminJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getUsers_withoutFilters_returnsAll() throws Exception {
        createUser("Ivan", "Petrov", "ivan@test.com");
        createUser("Anna", "Sidorova", "anna@test.com");

        mockMvc.perform(get("/api/users")
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getUsers_filterByNameIgnoringCase_returnsOnlyMatching() throws Exception {
        createUser("Ivan", "Petrov", "ivan@test.com");
        createUser("Anna", "Sidorova", "anna@test.com");

        mockMvc.perform(get("/api/users")
                        .with(adminJwt())
                        .param("name", "ivan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].surname").value("Petrov"));
    }

    @Test
    void getUsers_filterByNameAndSurname_returnsOnlyMatching() throws Exception {
        createUser("Ivan", "Petrov", "ivan@test.com");
        createUser("Ivan", "Sidorov", "ivan2@test.com");

        mockMvc.perform(get("/api/users")
                        .with(adminJwt())
                        .param("name", "Ivan").param("surname", "Sidorov"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("ivan2@test.com"));
    }

    @Test
    void getUsers_pagination_respectsPageSize() throws Exception {
        createUser("A", "One", "a@test.com");
        createUser("B", "Two", "b@test.com");
        createUser("C", "Three", "c@test.com");

        mockMvc.perform(get("/api/users")
                        .with(adminJwt())
                        .param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void updateUser_existing_changesFields() throws Exception {
        long id = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(put("/api/users/{id}", id)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Petr", "Ivanov", "ivan@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Petr"))
                .andExpect(jsonPath("$.surname").value("Ivanov"));

        mockMvc.perform(get("/api/users/{id}", id)
                        .with(adminJwt()))
                .andExpect(jsonPath("$.name").value("Petr"));
    }

    @Test
    void updateUser_unknown_returns404() throws Exception {
        mockMvc.perform(put("/api/users/{id}", 999999)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("Petr", "Ivanov", "x@test.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_deactivatesUser_andCacheIsEvicted() throws Exception {
        long id = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(get("/api/users/{id}", id)
                        .with(adminJwt()))
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(patch("/api/users/{id}/status", id)
                        .with(adminJwt())
                        .param("status", "false"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{id}", id)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void updateStatus_unknownUser_returns404() throws Exception {
        mockMvc.perform(patch("/api/users/{id}/status", 999999)
                        .with(adminJwt())
                        .param("status", "false"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_existing_returns204_andUserIsGone() throws Exception {
        long id = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(get("/api/users/{id}", id)
                        .with(adminJwt()))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/{id}", id)
                        .with(adminJwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", id)
                        .with(adminJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_unknown_returns404() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 999999)
                        .with(adminJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_alsoRemovesTheirCards() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");
        createCard(userId, cardNumber(1));

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(adminJwt()))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertEquals(0, cardRepository.count());
    }

    @Test
    void addCard_evictsUserCache_andNewCardIsReturned() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.length()").value(0));

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(1))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].number").value(cardNumber(1)));
    }

    @Test
    void updateCard_evictsUserCache_andUpdatedCardIsReturned() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");
        long cardId = createCard(userId, cardNumber(1));

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards[0].number").value(cardNumber(1)));

        mockMvc.perform(put("/api/cards/{id}", cardId)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(2))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.length()").value(1))
                .andExpect(jsonPath("$.cards[0].number").value(cardNumber(2)));
    }

    @Test
    void deleteCard_evictsUserCache_andCardIsRemoved() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");
        long cardId = createCard(userId, cardNumber(1));

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.length()").value(1));

        mockMvc.perform(delete("/api/cards/{id}", cardId)
                        .with(adminJwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards.length()").value(0));
    }

    private String cardJson(String number) {
        return String.format("""
            {
                "number": "%s",
                "holder": "IVAN PETROV",
                "expirationDate": "2029-11-04"
            }
            """, number);
    }
}
