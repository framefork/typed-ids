package org.framefork.typedIds.bigint.springData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BigIntDbSequenceGeneratedExplicitMappingEntityRepository extends JpaRepository<BigIntDbSequenceGeneratedExplicitMappingEntity, BigIntDbSequenceGeneratedExplicitMappingEntity.Id>
{

    BigIntDbSequenceGeneratedExplicitMappingEntity findByTitle(String title);

}
