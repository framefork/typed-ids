package org.framefork.typedIds.bigint.springData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BigIntAppGeneratedExplicitMappingEntityRepository extends JpaRepository<BigIntAppGeneratedExplicitMappingEntity, BigIntAppGeneratedExplicitMappingEntity.Id>
{

    BigIntAppGeneratedExplicitMappingEntity findByTitle(String title);

}
