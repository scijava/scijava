package org.scijava.ops;

public final class Ops {
	
	public interface OpIdentifier {
		String getNames();
		
		default String getName() {
			return OpUtils.parseOpNames(getNames())[0];
		}
	}
	
	public enum Math implements OpIdentifier{
		ADD(org.scijava.ops.math.Add.NAMES),
		POW(org.scijava.ops.math.Power.NAMES),
		SQRT(org.scijava.ops.math.Sqrt.NAMES),
		ZERO(org.scijava.ops.math.Zero.NAMES);
		
		private Math(String names) {
			this.names = names;
		}
		
		private final String names;
		
		public String getNames() {
			return names;
		}
	}
}
