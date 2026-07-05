package org.framefork.typedIds.uuid;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UuidEntityRepository extends JpaRepository<UuidEntity, UuidEntity.Id>
{

}
