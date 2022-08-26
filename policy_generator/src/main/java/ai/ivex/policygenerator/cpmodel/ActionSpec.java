package ai.ivex.policygenerator.cpmodel;

import java.util.List;

import com.google.auto.value.AutoValue;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

/** @author Hoang Tung Dinh */
@AutoValue
public abstract class ActionSpec {

	public static ActionSpec create(String name, int cost, Object2BooleanMap<String> changeableStateMap,
			List<Object2BooleanMap<String>> changeableStateMapAlternatives, Precondition precondition, Effect effect,
			List<Effect> alternativeEffects) {
		return new AutoValue_ActionSpec(name, cost, changeableStateMap, changeableStateMapAlternatives, precondition,
				effect, alternativeEffects);
	}

	abstract String name();

	abstract int cost();

	abstract Object2BooleanMap<String> changeableStateMap();

	abstract List<Object2BooleanMap<String>> changeableStateMapAlternatives();

	abstract Precondition precondition();

	abstract Effect effect();

	abstract List<Effect> alternativeEffects();
}
