package geogebra.common.kernel.statistics;

import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.kernel.arithmetic.BooleanValue;
import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.commands.CommandProcessor;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoFunction;
import geogebra.common.kernel.geos.GeoNumberValue;
import geogebra.common.main.MyError;

/**
 * Weibull Distribution
 */
public class CmdWeibull extends CommandProcessor {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdWeibull(Kernel kernel) {
		super(kernel);
	}

	@Override
	public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;

		arg = resArgs(c);

		BooleanValue cumulative = null; // default for n=3 (false)
		switch (n) {
		case 4:
			if (!arg[2].isGeoFunction()
					|| !((GeoFunction) arg[2]).toString(
							StringTemplate.defaultTemplate).equals("x")) {
				throw argErr(app, c.getName(), arg[2]);
			}

			if (arg[3].isGeoBoolean()) {
				cumulative = (BooleanValue) arg[3];
			} else
				throw argErr(app, c.getName(), arg[3]);

			// fall through
		case 3:
			if ((ok[0] = arg[0] instanceof GeoNumberValue)
					&& (ok[1] = arg[1] instanceof GeoNumberValue)) {
				if (arg[2].isGeoFunction()
						&& ((GeoFunction) arg[2]).toString(
								StringTemplate.defaultTemplate).equals("x")) {

					AlgoWeibullDF algo = new AlgoWeibullDF(cons, c.getLabel(),
							(GeoNumberValue) arg[0], (GeoNumberValue) arg[1],
							cumulative);
					return algo.getGeoElements();

				} else if (arg[2] instanceof GeoNumberValue) {

					AlgoWeibull algo = new AlgoWeibull(cons, c.getLabel(),
							(GeoNumberValue) arg[0], (GeoNumberValue) arg[1],
							(GeoNumberValue) arg[2]);

					GeoElement[] ret = { algo.getResult() };
					return ret;
				} else
					throw argErr(app, c.getName(), arg[2]);

			} else if (!ok[0])
				throw argErr(app, c.getName(), arg[0]);
			else if (!ok[1])
				throw argErr(app, c.getName(), arg[1]);

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}
}
