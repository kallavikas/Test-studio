package com.example.backend.repository;

import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by username
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email
     * @param username the username to search for
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    /**
     * Check if username exists
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find users by role
     * @param role the role to search for
     * @param pageable pagination information
     * @return Page of users with the specified role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    Page<User> findByRolesContaining(@Param("role") Role role, Pageable pageable);

    /**
     * Find enabled users
     * @param enabled the enabled status
     * @param pageable pagination information
     * @return Page of users with the specified enabled status
     */
    Page<User> findByEnabled(Boolean enabled, Pageable pageable);

    /**
     * Search users by username, email, first name, or last name
     * @param searchTerm the term to search for
     * @param pageable pagination information
     * @return Page of users matching the search term
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find users by first name and last name
     * @param firstName the first name
     * @param lastName the last name
     * @return List of users with the specified first and last name
     */
    List<User> findByFirstNameAndLastName(String firstName, String lastName);

    /**
     * Count users by role
     * @param role the role to count
     * @return number of users with the specified role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") Role role);

    /**
     * Find users created after a certain date
     * @param pageable pagination information
     * @return Page of recently created users
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= CURRENT_DATE - 30")
    Page<User> findRecentUsers(Pageable pageable);

    /**
     * Find users with multiple roles
     * @param pageable pagination information
     * @return Page of users with multiple roles
     */
    @Query("SELECT u FROM User u WHERE SIZE(u.roles) > 1")
    Page<User> findUsersWithMultipleRoles(Pageable pageable);
}
