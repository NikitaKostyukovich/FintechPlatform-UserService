package com.innowise.userservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void addCard_validBody_returns201() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(1), "IVAN PETROV")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.number").value(cardNumber(1)))
                .andExpect(jsonPath("$.holder").value("IVAN PETROV"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void addCard_unknownUser_returns404() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/cards", 999999)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(1), "IVAN PETROV")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void addCard_blankHolder_returns400() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(1), "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addCard_sixthActiveCard_returns400() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");
        for (int i = 0; i < 5; i++) {
            createCard(userId, cardNumber(i));
        }

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(5), "IVAN PETROV")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void addCard_afterDeactivatingOne_limitIsFreedUp() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");
        long firstCardId = createCard(userId, cardNumber(0));
        for (int i = 1; i < 5; i++) {
            createCard(userId, cardNumber(i));
        }

        mockMvc.perform(delete("/api/cards/{id}", firstCardId)
                        .with(adminJwt()))
                .andExpect(status().isNoContent());

        createCard(userId, cardNumber(5));
    }

    @Test
    void addCard_limitIsPerUser() throws Exception {
        long first = createUser("Ivan", "Petrov", "ivan@test.com");
        long second = createUser("Anna", "Sidorova", "anna@test.com");
        for (int i = 0; i < 5; i++) {
            createCard(first, cardNumber(i));
        }

        createCard(second, cardNumber(10));
    }

    @Test
    void getCardById_existing_returns200() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");
        long cardId = createCard(userId, cardNumber(1));

        mockMvc.perform(get("/api/cards/{id}", cardId)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.number").value(cardNumber(1)));
    }

    @Test
    void getCardById_unknown_returns404() throws Exception {
        mockMvc.perform(get("/api/cards/{id}", 999999)
                        .with(adminJwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getCardsByUser_returnsOnlyThatUsersCards() throws Exception {
        long first = createUser("Ivan", "Petrov", "ivan@test.com");
        long second = createUser("Anna", "Sidorova", "anna@test.com");
        createCard(first, cardNumber(1));
        createCard(first, cardNumber(2));
        createCard(second, cardNumber(3));

        mockMvc.perform(get("/api/users/{userId}/cards", first)
                        .with(adminJwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getCardsByUser_unknownUser_returns404() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/cards", 999999)
                        .with(adminJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCardsByUser_listIsNotStaleAfterAddingCard() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(get("/api/users/{userId}/cards", userId)
                        .with(adminJwt()))
                .andExpect(jsonPath("$.length()").value(0));

        createCard(userId, cardNumber(1));

        mockMvc.perform(get("/api/users/{userId}/cards", userId)
                        .with(adminJwt()))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void updateCard_existing_changesHolder() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");
        long cardId = createCard(userId, cardNumber(1));

        mockMvc.perform(put("/api/cards/{id}", cardId)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(1), "PETR IVANOV")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holder").value("PETR IVANOV"));

        mockMvc.perform(get("/api/cards/{id}", cardId)
                        .with(adminJwt()))
                .andExpect(jsonPath("$.holder").value("PETR IVANOV"));
    }

    @Test
    void updateCard_unknown_returns404() throws Exception {
        mockMvc.perform(put("/api/cards/{id}", 999999)
                        .with(adminJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(1), "PETR IVANOV")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_returns204_andCardBecomesInactive() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");
        long cardId = createCard(userId, cardNumber(1));

        mockMvc.perform(delete("/api/cards/{id}", cardId)
                        .with(adminJwt()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/{id}", cardId)
                        .with(adminJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_unknown_returns404() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", 999999)
                        .with(adminJwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addCard_asUser_createsCard() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(post("/api/cards")
                        .with(userJwt(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(1), "IVAN PETROV")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void getCards_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addCardByAdminPath_asRegularUser_returns403Forbidden() throws Exception {
        long userId = createUser("Ivan", "Petrov", "ivan@test.com");

        mockMvc.perform(post("/api/users/{userId}/cards", userId)
                        .with(userJwt(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cardJson(cardNumber(1), "IVAN PETROV")))
                .andExpect(status().isBadRequest());
    }
}
