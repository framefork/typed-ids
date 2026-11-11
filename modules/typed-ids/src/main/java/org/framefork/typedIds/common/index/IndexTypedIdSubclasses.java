package org.framefork.typedIds.common.index;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a base class whose concrete subclasses the <code>typed-ids-index-java-classes-processor</code> writes into a compile-time index.
 * <p>
 * The annotation is {@link Inherited}, so the compiler reports every subclass as annotated, including nested and indirect ones.
 * That lets the processor declare this single annotation as its trigger, which Gradle needs to compile incrementally.
 * The index of a class is <code>META-INF/services/&lt;canonical name of the nearest superclass that declares this annotation&gt;</code>.
 * <p>
 * It is already present on {@link org.framefork.typedIds.uuid.ObjectUuid} and {@link org.framefork.typedIds.bigint.ObjectBigIntId},
 * so id classes do not need to declare it.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface IndexTypedIdSubclasses
{

}
