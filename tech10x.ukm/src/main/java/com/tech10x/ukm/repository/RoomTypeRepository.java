package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

    List<RoomType> findByProperty_IdOrderByIdAsc(Long propertyDbId);

    Optional<RoomType> findByRoomTypeIdAndProperty_Id(String roomTypeId, Long propertyDbId);

    long countByProperty_Id(Long propertyDbId);

    boolean existsByProperty_IdAndNameIgnoreCase(Long propertyDbId, String name);

    boolean existsByProperty_IdAndNameIgnoreCaseAndIdNot(Long propertyDbId, String name, Long id);

    /** Rows of [propertyDbId, lowest normal tariff] - used for "starting from" prices in listings. */
    @Query("select r.property.id, min(r.normalTariff) from RoomType r "
            + "where r.property.id in :ids group by r.property.id")
    List<Object[]> minTariffByProperty(@Param("ids") Collection<Long> ids);
}
