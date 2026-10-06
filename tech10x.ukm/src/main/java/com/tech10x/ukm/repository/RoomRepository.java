package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.Room;
import com.tech10x.ukm.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByProperty_IdOrderByIdAsc(Long propertyDbId);

    List<Room> findByProperty_IdAndRoomType_IdOrderByIdAsc(Long propertyDbId, Long roomTypeDbId);

    List<Room> findByRoomType_IdAndStatusOrderByIdAsc(Long roomTypeDbId, RoomStatus status);

    Optional<Room> findByRoomIdAndProperty_Id(String roomId, Long propertyDbId);

    List<Room> findByProperty_IdAndRoomNumberIn(Long propertyDbId, Collection<String> roomNumbers);

    long countByProperty_Id(Long propertyDbId);

    boolean existsByRoomType_Id(Long roomTypeDbId);
}
