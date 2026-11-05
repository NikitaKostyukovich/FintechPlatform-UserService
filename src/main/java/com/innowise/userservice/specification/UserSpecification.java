package com.innowise.userservice.specification;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> findByName(String name){
            return (root, query, criteriaBuilder) -> name == null || name.isBlank() ? criteriaBuilder.conjunction() : criteriaBuilder.equal(criteriaBuilder.lower(root.get("name")), name.toLowerCase());
    }
    public static Specification<User> findBySurname(String surname){
        return ((root, query, criteriaBuilder) -> surname == null || surname.isBlank() ? criteriaBuilder.conjunction() : criteriaBuilder.equal(criteriaBuilder.lower(root.get("surname")), surname.toLowerCase()));
    }

}
