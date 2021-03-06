package geogebra.common.kernel.cas;

import geogebra.common.kernel.Construction;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.kernel.algos.AlgoCasBase;
import geogebra.common.kernel.arithmetic.MyArbitraryConstant;
import geogebra.common.kernel.commands.Commands;
import geogebra.common.kernel.geos.CasEvaluableFunction;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.geos.GeoFunction;

/**
 * Algorithm for TrigCombine
 */
public class AlgoTrigCombine extends AlgoCasBase {
	private GeoFunction target;

	/**
	 * @param cons
	 *            construction
	 * @param label
	 *            label for output
	 * @param f
	 *            function
	 * @param target
	 *            target function (ie sin or cos)
	 */
	public AlgoTrigCombine(Construction cons, String label,
			CasEvaluableFunction f, GeoFunction target) {
		super(cons, f, Commands.TrigCombine);
		this.target = target;
		setInputOutput();
		compute();
		g.setLabel(label);
	}

	@Override
	public void setInputOutput() {
		if (target != null) {
			input = new GeoElement[] { f.toGeoElement(), target };

		} else
			input = new GeoElement[] { f.toGeoElement() };
		setOnlyOutput(g);
		setDependencies();
	}

	private MyArbitraryConstant arbconst = new MyArbitraryConstant(this);

	@Override
	protected void applyCasCommand(StringTemplate tpl) {
		StringBuilder sb = new StringBuilder();
		sb.append("TrigCombine[%");
		if (target != null) {
			sb.append(',');
			sb.append(target.toValueString(tpl));
		}
		sb.append(']');
		g.setUsingCasCommand(sb.toString(), f, true, arbconst);
	}
}
