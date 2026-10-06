package com.tech10x.ukm.repositoryproxy;

import com.tech10x.ukm.entity.Room;
import com.tech10x.ukm.entity.RoomStatus;
import com.tech10x.ukm.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** ROOM - the individual numbered rooms of a property. */
@Component
@RequiredArgsConstructor
public class RoomUnitProxyRepository {

    private final RoomRepository roomRepository;

    public List<Room> findByProperty(Long propertyDbId) {
        return roomRepository.findByProperty_IdOrderByIdAsc(propertyDbId);
    }

    public List<Room> findByPropertyAndType(Long propertyDbId, Long roomTypeDbId) {
        return roomRepository.findByProperty_IdAndRoomType_IdOrderByIdAsc(propertyDbId, roomTypeDbId);
    }

    public List<Room> findActiveByType(Long roomTypeDbId) {
        return roomRepository.findByRoomType_IdAndStatusOrderByIdAsc(roomTypeDbId, RoomStatus.ACTIVE);
    }

    public Optional<Room> findRoom(String roomId, Long propertyDbId) {
        return roomRepository.findByRoomIdAndProperty_Id(roomId, propertyDbId);
    }

    public List<Room> findByNumbers(Long propertyDbId, Collection<String> roomNumbers) {
        return roomNumbers.isEmpty() ? List.of() : roomRepository.findByProperty_IdAndRoomNumberIn(propertyDbId, roomNumbers);
    }

    public long countByProperty(Long propertyDbId) {
        return roomRepository.countByProperty_Id(propertyDbId);
    }

    public boolean existsForRoomType(Long roomTypeDbId) {
        return roomRepository.existsByRoomType_Id(roomTypeDbId);
    }

    public Room save(Room room) {
        return roomRepository.save(room);
    }

    public List<Room> saveAll(List<Room> rooms) {
        return roomRepository.saveAll(rooms);
    }

    public void delete(Room room) {
        roomRepository.delete(room);
    }
}
