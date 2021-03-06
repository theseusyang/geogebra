package geogebra.common.kernel.commands;

import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.algos.AlgoBarChart;
import geogebra.common.kernel.algos.AlgoSequence;
import geogebra.common.kernel.arithmetic.Command;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoList;
import geogebra.common.kernel.geos.GeoNumberValue;
import geogebra.common.kernel.geos.GeoNumeric;
import geogebra.common.main.MyError;

/**
 * BarChart[ <Number>, <Number>, <List> ]
 */
public class CmdBarChart extends CommandProcessor {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdBarChart(Kernel kernel) {
		super(kernel);
	}

	@Override
	final public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;

		switch (n) {
		case 2:
			arg = resArgs(c);
			if ((ok[0] = (arg[0].isGeoList()))
					&& (ok[1] = (arg[1].isGeoNumeric()))) {

				AlgoBarChart algo = new AlgoBarChart(cons, c.getLabel(),
						(GeoList) arg[0], (GeoNumeric) arg[1]);

				GeoElement[] ret = { algo.getSum() };
				return ret;
			} else if ((ok[0] = (arg[0].isGeoList()))
					&& (ok[1] = (arg[1].isGeoList()))) {

				AlgoBarChart algo = new AlgoBarChart(cons, c.getLabel(),
						(GeoList) arg[0], (GeoList) arg[1]);

				GeoElement[] ret = { algo.getSum() };
				return ret;
			} else
				throw argErr(app, c.getName(), null);
		case 3:
			arg = resArgs(c);
			if ((ok[0] = (arg[0] instanceof GeoNumberValue))
					&& (ok[1] = (arg[1] instanceof GeoNumberValue))
					&& (ok[2] = (arg[2].isGeoList()))) {

				AlgoBarChart algo = new AlgoBarChart(cons, c.getLabel(),
						(GeoNumberValue) arg[0], (GeoNumberValue) arg[1],
						(GeoList) arg[2]);

				GeoElement[] ret = { algo.getSum() };
				return ret;
			} else if ((ok[0] = (arg[0].isGeoList()))
					&& (ok[1] = (arg[1].isGeoList()))
					&& (ok[2] = (arg[2] instanceof GeoNumberValue))) {

				AlgoBarChart algo = new AlgoBarChart(cons, c.getLabel(),
						(GeoList) arg[0], (GeoList) arg[1],
						(GeoNumberValue) arg[2]);

				GeoElement[] ret = { algo.getSum() };
				return ret;
			}
			if ((ok[0] = (arg[0].isGeoList()))
					&& (ok[1] = (arg[1].isGeoNumeric()))
					&& (ok[2] = (arg[2].isGeoNumeric()))) {

				AlgoBarChart algo = new AlgoBarChart(cons, c.getLabel(),
						(GeoList) arg[0], (GeoNumeric) arg[1],
						(GeoNumeric) arg[2]);

				GeoElement[] ret = { algo.getSum() };
				return ret;
			}

			throw argErr(app, c.getName(), null);

		case 6:
			// create local variable at position 3 and resolve arguments
			arg = resArgsLocalNumVar(c, 3, 4);
			if ((ok[0] = (arg[0] instanceof GeoNumberValue))
					&& (ok[1] = arg[1] instanceof GeoNumberValue)
					&& (ok[2] = arg[2].isGeoElement())
					&& (ok[3] = arg[3].isGeoNumeric())
					&& (ok[4] = arg[4] instanceof GeoNumberValue)
					&& (ok[5] = arg[5] instanceof GeoNumberValue)) {
				GeoElement[] ret = { BarChart(c.getLabel(),
						(GeoNumberValue) arg[0], (GeoNumberValue) arg[1],
						arg[2], (GeoNumeric) arg[3], (GeoNumberValue) arg[4],
						(GeoNumberValue) arg[5], null) };
				return ret;
			}
			throw argErr(app, c.getName(), null);

		case 7:
			// create local variable at position 3 and resolve arguments
			arg = resArgsLocalNumVar(c, 3, 4);
			if ((ok[0] = (arg[0] instanceof GeoNumberValue))
					&& (ok[1] = (arg[1] instanceof GeoNumberValue))
					&& ((ok[2] = arg[2].isGeoElement()))
					&& (ok[3] = arg[3].isGeoNumeric())
					&& (ok[4] = arg[4] instanceof GeoNumberValue)
					&& (ok[5] = arg[5] instanceof GeoNumberValue)
					&& (ok[6] = arg[6] instanceof GeoNumberValue)) {
				GeoElement[] ret = { BarChart(c.getLabel(),
						(GeoNumberValue) arg[0], (GeoNumberValue) arg[1],
						arg[2], (GeoNumeric) arg[3], (GeoNumberValue) arg[4],
						(GeoNumberValue) arg[5], (GeoNumberValue) arg[6]) };
				return ret;
			}
			throw argErr(app, c.getName(), null);

		default:
			throw argNumErr(app, c.getName(), n);
		}
	}

	/**
	 * BarChart
	 */
	final private GeoNumeric BarChart(String label, NumberValue a,
			NumberValue b, GeoElement expression, GeoNumeric localVar,
			NumberValue from, NumberValue to, NumberValue step) {

		AlgoSequence seq = new AlgoSequence(cons, expression, localVar, from,
				to, step);
		cons.removeFromConstructionList(seq);

		AlgoBarChart algo = new AlgoBarChart(cons, label, a, b,
				(GeoList) seq.getOutput()[0]);
		GeoNumeric sum = algo.getSum();
		return sum;
	}
}
