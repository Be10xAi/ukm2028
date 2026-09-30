package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.RoomInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    List<RoomInventory> findByRoomType_IdAndDateBetweenOrderByDateAsc(Long roomTypeDbId, LocalDate from, LocalDate to);

    boolean existsByRoomType_Id(Long roomTypeDbId);
}
