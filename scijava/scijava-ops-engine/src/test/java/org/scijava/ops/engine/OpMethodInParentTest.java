
package org.scijava.ops.engine;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;

public class OpMethodInParentTest extends AbstractTestEnvironment {

	@BeforeClass
	public static void addNeededOps() {
		discoverer.register(new SuperOpMethodHousingClass());
		discoverer.register(new SuperOpMethodHousingInterface());
	}

	@Test
	public void testFMethodInSuperclass() {
		String actual = ops.op("test.superMethod").input("Foo").outType(
			String.class).apply();
		String expected = "This string came from " +
			SuperOpMethodHousingClass.class;
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void testFMethodInInterface() {
		String actual = ops.op("test.superMethodIface").input("Foo").outType(
			String.class).apply();
		String expected = "This string came from " +
			SuperOpMethodHousingInterface.class;
		Assert.assertEquals(expected, actual);
	}

}

abstract class OpMethodHousingClass<T> implements Function<T, T> {

	abstract T getT();

	@Override
	public T apply(T t) {
		return getT();
	}

}

@OpClass(names = "test.superMethod")
class SuperOpMethodHousingClass //
	extends OpMethodHousingClass<String> //
	implements Op
{

	@Override
	String getT() {
		return "This string came from " + this.getClass();
	}

}

interface OpMethodHousingInterface<T> extends Function<T, T> {

	T getT();

	@Override
	default T apply(T t) {
		return getT();
	}

}

@OpClass(names = "test.superMethodIface")
class SuperOpMethodHousingInterface implements
	OpMethodHousingInterface<String>, Op
{

	@Override
	public String getT() {
		return "This string came from " + this.getClass();
	}

}
