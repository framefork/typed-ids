package org.framefork.typedIds.bigint;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BigIntEntityRepository extends JpaRepository<BigIntEntity, BigIntEntity.Id>
{

}
