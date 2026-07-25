package com.q4magic.common.repository;

import com.q4magic.common.models.Customers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface CustomersRepository extends JpaRepository<Customers, Integer> {
    @Query(value = "SELECT c FROM Customers c WHERE c.customers.id = :id")
    List<Customers> getAllSubUsers(Integer id);

    @Query(value = "SELECT c FROM Customers c WHERE c.customers.id = :id AND c.subUserType.name = 'Sales Representative'")
    List<Customers> getAllSubUsersWithRepRole(Integer id);

    @Query("SELECT c FROM Customers c " +
            "WHERE c.emailAddress = :email " +
            "AND (:id IS NULL OR c.id <> :id)")
    Customers findByEmail(@Param("email") String email, @Param("id") Integer id);

    @Query("SELECT c FROM Customers c " +
            "WHERE c.username = :username " +
            "AND (:id IS NULL OR c.id <> :id)")
    Customers findByUserName(@Param("username") String username, @Param("id") Integer id);


    @Query(value = "SELECT c FROM Customers c WHERE c.emailAddress = :email AND c.password = :password")
    Customers findByEmailAndPassword(@Param("email") String email, @Param("password") String password);

    @Query(value = "SELECT c FROM Customers c WHERE c.username = :username AND c.password = :password")
    Customers findByUserNameAndPassword(@Param("username") String username, @Param("password") String password);

}
