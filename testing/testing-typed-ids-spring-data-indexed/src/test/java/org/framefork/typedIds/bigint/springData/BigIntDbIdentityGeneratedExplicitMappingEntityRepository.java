package org.framefork.typedIds.bigint.springData;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BigIntDbIdentityGeneratedExplicitMappingEntityRepository extends JpaRepository<BigIntDbIdentityGeneratedExplicitMappingEntity, BigIntDbIdentityGeneratedExplicitMappingEntity.Id>
{

    BigIntDbIdentityGeneratedExplicitMappingEntity findByTitle(String title);

}
