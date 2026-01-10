package com.barrelbeczka.currency.repository;

import com.barrelbeczka.currency.model.ConversionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversionHistoryRepository extends JpaRepository<ConversionHistory, Long> {
    // Add custom queries if needed, e.g. finding top N latest
    List<ConversionHistory> findAllByOrderByTimestampDesc();
}
