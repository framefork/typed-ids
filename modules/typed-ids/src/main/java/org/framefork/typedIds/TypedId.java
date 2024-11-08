package org.framefork.typedIds;

import java.io.Serializable;

public interface TypedId<IdType extends TypedId<IdType>> extends Comparable<IdType>, Serializable
{

}
