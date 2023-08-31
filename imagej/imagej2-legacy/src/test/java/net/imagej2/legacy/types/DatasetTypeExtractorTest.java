
package net.imagej2.legacy.types;

import java.lang.reflect.Type;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.discovery.Discoverer;
import org.scijava.log2.StderrLoggerFactory;
import org.scijava.types.Any;
import org.scijava.types.DefaultTypeReifier;
import org.scijava.types.Types;

import net.imagej.DefaultDataset;
import net.imagej.ImgPlus;
import net.imglib2.img.array.ArrayImgs;

/**
 * Tests {@link DatasetTypeExtractor}
 * 
 * @author Gabriel Selzer
 */
public class DatasetTypeExtractorTest {

	@Test
	public void testDatasetTypeExtractor() {
		// Create a TypeReifier
		var log = new StderrLoggerFactory().create();
		var reifier = new DefaultTypeReifier(log, Discoverer.all(
			ServiceLoader::load));
		// Create a Dataset
		var ctx = new Context();
		var img = ArrayImgs.unsignedBytes(10, 10);
		var imgPlus = new ImgPlus<>(img);
		var ds = new DefaultDataset(ctx, imgPlus);
		// Assert correct reification
		// NB without a dependency on imagej-ops2 we cannot reify
		// the ImgPlus, but asserting an ImgPlus is the reified type
		// is enough to unit test the TypeExtractor
		var actual = new DatasetTypeExtractor().reify(reifier, ds);
		var expected = Types.parameterize(ImgPlus.class, new Type[] { new Any() });
		Assertions.assertEquals(expected, actual);
	}

}
