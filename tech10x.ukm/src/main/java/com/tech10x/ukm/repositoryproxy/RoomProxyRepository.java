package com.tech10x.ukm.repositoryproxy;

import com.tech10x.ukm.entity.RoomInventory;
import com.tech10x.ukm.entity.RoomType;
import com.tech10x.ukm.repository.RoomInventoryRepository;
import com.tech10x.ukm.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** ROOM_TYPE and ROOM_INVENTORY. */
@Component
@RequiredArgsConstructor
public class RoomProxyRepository {

    private final RoomTypeRepository roomTypeRepository;
    private final RoomInventoryRepository inventoryRepository;

    // ---- room type ----
    public List<RoomType> findRoomTypes(Long propertyDbId) {
        return roomTypeRepository.findByProperty_IdOrderByIdAsc(propertyDbId);
    }

    public Optional<RoomType> findRoomType(String roomTypeId, Long propertyDbId) {
        return roomTypeRepository.findByRoomTypeIdAndProperty_Id(roomTypeId, propertyDbId);
    }

    public long countRoomTypes(Long propertyDbId) {
        return roomTypeRepository.countByProperty_Id(propertyDbId);
    }

    public boolean roomTypeNameTaken(Long propertyDbId, String name, Long excludeId) {
        return excludeId == null
                ? roomTypeRepository.existsByProperty_IdAndNameIgnoreCase(propertyDbId, name)
                : roomTypeRepository.existsByProperty_IdAndNameIgnoreCaseAndIdNot(propertyDbId, name, excludeId);
    }

    public RoomType saveRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    public void deleteRoomType(RoomType roomType) {
        roomTypeRepository.delete(roomType);
    }

    /** propertyDbId -> lowest normal tariff across its room types. */
    public Map<Long, BigDecimal> startingPrices(Collection<Long> propertyDbIds) {
        Map<Long, BigDecimal> prices = new HashMap<>();
        for (Object[] row : roomTypeRepository.minTariffByProperty(propertyDbIds)) {
            prices.put((Long) row[0], (BigDecimal) row[1]);
        }
        return prices;
    }

    // ---- inventory ----
    public List<RoomInventory> findInventory(Long roomTypeDbId, LocalDate from, LocalDate to) {
        return inventoryRepository.findByRoomType_IdAndDateBetweenOrderByDateAsc(roomTypeDbId, from, to);
    }

    public boolean hasInventory(Long roomTypeDbId) {
        return inventoryRepository.existsByRoomType_Id(roomTypeDbId);
    }

    public List<RoomInventory> saveAllInventory(List<RoomInventory> rows) {
        return inventoryRepository.saveAll(rows);
    }
}
