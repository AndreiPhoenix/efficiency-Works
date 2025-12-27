package com.example.repository;

import com.example.model.DataModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DataRepository extends JpaRepository<DataModel, Long> {

    @Query("SELECT d FROM DataModel d WHERE d.category = :category")
    List<DataModel> findByCategory(String category);

    @Query("SELECT AVG(d.value) FROM DataModel d WHERE d.category = :category")
    Double findAverageByCategory(String category);

    @Query(value = "SELECT COUNT(*) FROM parsed_data", nativeQuery = true)
    Long countAll();

    @Query("SELECT d FROM DataModel d WHERE d.timestamp >= CURRENT_TIMESTAMP - 1")
    List<DataModel> findRecentData();
}