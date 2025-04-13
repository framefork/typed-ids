package org.framefork.typedIds;

import com.google.auto.service.AutoService;
import org.atteo.classindex.processor.ClassIndexProcessor;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.uuid.ObjectUuid;

import javax.annotation.processing.Processor;

@AutoService(Processor.class)
public class TypedIdsClassIndexProcessor extends ClassIndexProcessor
{

    @SuppressWarnings("rawtypes")
    public TypedIdsClassIndexProcessor()
    {
        indexSubclasses(ObjectUuid.class);
        indexSubclasses(ObjectBigIntId.class);
    }

}
