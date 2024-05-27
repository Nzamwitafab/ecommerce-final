package com.ecommerce.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entities.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    static boolean existsByProductId(int productId) {
        return false;
    }

	@Query(value = "select * from orders where product_id = ?", nativeQuery = true)
	public Order findByProductId(@Param("id") Integer id);


    List<Order> findByOrderBy(Integer userId); // Assuming 'Order' has 'orderBy' field for user ID

}
