package com.tech10x.ukm.repository;

import com.tech10x.ukm.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

    List<PropertyImage> findByProperty_IdOrderByCoverDescIdAsc(Long propertyDbId);

    Optional<PropertyImage> findByImageIdAndProperty_Id(String imageId, Long propertyDbId);

    long countByProperty_Id(Long propertyDbId);

    List<PropertyImage> findByProperty_IdInAndCoverTrue(Collection<Long> propertyDbIds);
}
