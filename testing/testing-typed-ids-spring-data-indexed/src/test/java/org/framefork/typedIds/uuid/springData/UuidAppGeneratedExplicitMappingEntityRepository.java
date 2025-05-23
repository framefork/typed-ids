package org.framefork.typedIds.uuid.springData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UuidAppGeneratedExplicitMappingEntityRepository extends JpaRepository<UuidAppGeneratedExplicitMappingEntity, UuidAppGeneratedExplicitMappingEntity.Id>
{

    UuidAppGeneratedExplicitMappingEntity findByTitle(String title);

}
