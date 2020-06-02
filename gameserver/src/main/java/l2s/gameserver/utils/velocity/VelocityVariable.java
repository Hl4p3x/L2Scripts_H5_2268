package l2s.gameserver.utils.velocity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author VISTALL
 * @date 15:06/26.04.2012
 *
 * Аннотация - которая определяет ли будет переменная использоватся в диалогах при парсе Velocity
 *
 * ВНИМАНИЯ: переименовавывать переменную нельзя, пока непроверится датапак
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VelocityVariable
{

}
