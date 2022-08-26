package ai.ivex.policygenerator.protobufspec;

import ai.ivex.policygenerator.cpmodel.ActionSpec;
import ai.ivex.policygenerator.cpmodel.Assumption;
import ai.ivex.policygenerator.cpmodel.Effect;
import ai.ivex.policygenerator.cpmodel.Goals;
import ai.ivex.policygenerator.cpmodel.MutuallyExclusiveActions;
import ai.ivex.policygenerator.cpmodel.ReactionRule;
import ai.ivex.policygenerator.cpmodel.StateRule;
import ai.ivex.policygenerator.cpmodel.Specification;
import ai.ivex.policygenerator.cpmodel.StateSpec;
import ai.ivex.policygenerator.protobufspec.predicates.AndPredicate;
import ai.ivex.policygenerator.protobufspec.predicates.ConstantPredicate;
import ai.ivex.policygenerator.protobufspec.predicates.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.TextFormat;
import generated.ai.ivex.specdomain.PredicateProtos;
import generated.ai.ivex.specdomain.SpecificationProtos;
import generated.ai.ivex.specdomain.SpecificationProtos.AlternativeEffect;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public abstract class ProtobufSpecificationParser {

	public static Specification parseSpecAndBuildModel(String specString) throws Exception {
		SpecificationProtos.Specification protoSpec = parseStringToProtoSpec(specString);

		return buildModel(protoSpec);
	}

	static SpecificationProtos.Specification parseStringToProtoSpec(String specString)
			throws TextFormat.ParseException {
		SpecificationProtos.Specification.Builder specBuilder = SpecificationProtos.Specification.newBuilder();
		TextFormat.getParser().merge(specString, specBuilder);
		return specBuilder.build();
	}

	static Specification buildModel(SpecificationProtos.Specification specification) throws Exception {
		ImmutableMap<String, ImmutableSet<String>> stateVarMap = parseStateVector(specification.getStateVarBlock());
		ImmutableSet<StateSpec> stateSpecs = stateVarMap.entrySet().stream()
				.map(e -> StateSpec.create(e.getKey(), e.getValue())).collect(ImmutableSet.toImmutableSet());

		ImmutableSet<String> actionNames = parseActionNames(specification.getActionsBlock());

		ImmutableMap<String, Predicate> aliases = parseAliases(specification.getAliases(), stateVarMap, actionNames);

		ImmutableMap<String, Set<String>> mutableControlledResources = parseControlledResources(
				specification.getResourceBlock());

		ImmutableSet<ActionSpec> actionSpecs = parseActions(specification.getActionsBlock(), stateVarMap, actionNames,
				aliases, mutableControlledResources);

		ImmutableSet<MutuallyExclusiveActions> mutuallyExclusiveActions = mutableControlledResources.entrySet().stream()
				.map(Map.Entry::getValue).map(ImmutableSet::copyOf).map(MutuallyExclusiveActions::create)
				.collect(ImmutableSet.toImmutableSet());

		ImmutableSet<ReactionRule> reactionRules = parseReactionRules(specification.getReactionRulesBlock(),
				stateVarMap, actionNames, aliases);

		ImmutableSet<StateRule> stateRules = parseStateRules(specification.getStateRulesBlock(), stateVarMap,
				actionNames, aliases);

		ImmutableSet<Assumption> assumptions = parseAssumptions(specification.getAssumptionsBlock(), stateVarMap,
				actionNames, aliases);

		Goals goals = parseGoals(specification.getGoal(), stateVarMap, actionNames, aliases);

		int planLength = specification.getConfig().getPlanSizeMax();

		// TODO: validate constraints

		return Specification.create(planLength, actionSpecs, stateSpecs, mutuallyExclusiveActions, reactionRules,
				stateRules, assumptions, goals);
	}

	private static ImmutableMap<String, ImmutableSet<String>> parseStateVector(
			SpecificationProtos.StateVarBlock stateVarBlock) throws Exception {
		Set<String> knownStateVariables = new HashSet<>();
		ImmutableMap.Builder<String, ImmutableSet<String>> resultBuilder = ImmutableMap.builder();
		for (SpecificationProtos.StateVariable variable : stateVarBlock.getStateVariablesList()) {
			if (knownStateVariables.contains(variable.getName())) {
				throw new Exception("The state variable " + variable.getName() + " was declared more than once.");
			}
			if (variable.getValuesCount() < 1) {
				throw new Exception("The state variable " + variable.getName() + " needs at least one possible value.");
			}
			knownStateVariables.add(variable.getName());

			resultBuilder.put(variable.getName(), ImmutableSet.copyOf(variable.getValuesList()));
		}

		return resultBuilder.build();
	}

	private static ImmutableSet<String> parseActionNames(SpecificationProtos.ActionBlock actionBlock) throws Exception {
		Set<String> knownActions = new HashSet<>();

		for (SpecificationProtos.Action action : actionBlock.getActionsList()) {
			if (knownActions.contains(action.getName())) {
				throw new Exception("The action " + action.getName() + " was declared more than once.");
			}

			knownActions.add(action.getName());
		}

		return ImmutableSet.copyOf(knownActions);
	}

	private static ImmutableMap<String, Predicate> parseAliases(SpecificationProtos.AliasBlock aliasBlock,
			ImmutableMap<String, ImmutableSet<String>> stateVarMap, ImmutableSet<String> actionNames) throws Exception {
		Set<String> knownAliases = new HashSet<>();
		ImmutableMap.Builder<String, Predicate> resultBuilder = ImmutableMap.builder();

		for (SpecificationProtos.Alias alias : aliasBlock.getAliasesList()) {
			if (knownAliases.contains(alias.getName())) {
				throw new Exception("The alias " + alias.getName() + " was declared more than once.");
			}

			Predicate predicate = PredicateParser.parsePredicate(alias.getValue(), stateVarMap, actionNames,
					resultBuilder.build());
			knownAliases.add(alias.getName());

			resultBuilder.put(alias.getName(), predicate);
		}

		return resultBuilder.build();
	}

	private static ImmutableMap<String, Set<String>> parseControlledResources(
			SpecificationProtos.ResourceBlock resourceBlock) throws Exception {
		Set<String> knownResources = new HashSet<>();

		for (SpecificationProtos.Resource resource : resourceBlock.getResourcesList()) {
			if (knownResources.contains(resource.getName())) {
				throw new Exception("The resource " + resource.getName() + " was declared more than once.");
			}
			knownResources.add(resource.getName());
		}

		return knownResources.stream()
				.collect(ImmutableMap.toImmutableMap(Function.identity(), r -> new HashSet<String>()));
	}

	private static ImmutableSet<ActionSpec> parseActions(SpecificationProtos.ActionBlock actionBlock,
			ImmutableMap<String, ImmutableSet<String>> stateVarMap, ImmutableSet<String> actionNames,
			ImmutableMap<String, Predicate> aliases, ImmutableMap<String, Set<String>> controlledResources)
			throws Exception {

		ImmutableSet.Builder<ActionSpec> resultBuilder = ImmutableSet.builder();

		for (SpecificationProtos.Action action : actionBlock.getActionsList()) {
			for (String resource : action.getResourcesList()) {
				if (!controlledResources.containsKey(resource)) {
					throw new Exception(
							"The action " + action.getName() + " depends on an unknown resource: " + resource);
				}
				controlledResources.get(resource).add(action.getName()); // Add the action to the controller resource it
																			// belongs to
			}

			// Combine the (possibly) multiple preconditions into a single predicate
			Predicate precondition = new ConstantPredicate(true);
			for (PredicateProtos.Predicate predicate : action.getPreconditionsList()) {
				precondition = new AndPredicate(precondition,
						PredicateParser.parsePredicate(predicate, stateVarMap, actionNames, aliases));
			}

			// Combine the (possibly) multiple effects into a single predicate
			Predicate effect = new ConstantPredicate(true);
			for (PredicateProtos.Predicate predicate : action.getNominalEffectsList()) {
				effect = new AndPredicate(effect,
						PredicateParser.parsePredicate(predicate, stateVarMap, actionNames, aliases));
			}

			// Populate the changeables map
			Object2BooleanOpenHashMap<String> changeables = new Object2BooleanOpenHashMap<>();
			stateVarMap.keySet().forEach(variable -> changeables.put(variable, false));
			effect.getReferencedStateVariables().forEach(variable -> changeables.put(variable, true));

			// Populate the changeablesAlternative List and alternativeEffects List
			List<Object2BooleanMap<String>> changeablesAlternative = new ArrayList<Object2BooleanMap<String>>();
			List<Effect> alternativeEffects = new ArrayList<Effect>();

			for (AlternativeEffect alternativeEffect : action.getAlternativeEffectsList()) {
				// Combine the (possibly) multiple effects into a single predicate
				Predicate altEffect = new ConstantPredicate(true);
				for (PredicateProtos.Predicate predicate : alternativeEffect.getAlternativeEffectList()) {
					altEffect = new AndPredicate(altEffect,
							PredicateParser.parsePredicate(predicate, stateVarMap, actionNames, aliases));
				}
				alternativeEffects.add(altEffect);

				Object2BooleanOpenHashMap<String> oneAlternativeChangeables = new Object2BooleanOpenHashMap<>();
				stateVarMap.keySet().forEach(variable -> oneAlternativeChangeables.put(variable, false));
				altEffect.getReferencedStateVariables()
						.forEach(variable -> oneAlternativeChangeables.put(variable, true));
				changeablesAlternative.add(oneAlternativeChangeables);
			}

			resultBuilder.add(ActionSpec.create(action.getName(), action.getCost(), changeables, changeablesAlternative,
					precondition, effect, alternativeEffects));
		}

		return resultBuilder.build();
	}

	private static ImmutableSet<ReactionRule> parseReactionRules(
			SpecificationProtos.ReactionRulesBlock reactionRulesBlock,
			ImmutableMap<String, ImmutableSet<String>> stateVarMap, ImmutableSet<String> actionNames,
			ImmutableMap<String, Predicate> aliases) throws Exception {
		ImmutableSet.Builder<ReactionRule> resultBuilder = ImmutableSet.builder();

		for (PredicateProtos.Predicate predicate : reactionRulesBlock.getRulesList()) {
			resultBuilder.add(PredicateParser.parsePredicate(predicate, stateVarMap, actionNames, aliases));
		}

		return resultBuilder.build();
	}

	private static ImmutableSet<StateRule> parseStateRules(SpecificationProtos.StateRulesBlock stateRulesBlock,
			ImmutableMap<String, ImmutableSet<String>> stateVarMap, ImmutableSet<String> actionNames,
			ImmutableMap<String, Predicate> aliases) throws Exception {
		ImmutableSet.Builder<StateRule> resultBuilder = ImmutableSet.builder();

		for (PredicateProtos.Predicate predicate : stateRulesBlock.getRulesList()) {
			resultBuilder.add(PredicateParser.parsePredicate(predicate, stateVarMap, actionNames, aliases));
		}

		return resultBuilder.build();
	}

	private static ImmutableSet<Assumption> parseAssumptions(SpecificationProtos.AssumptionsBlock assumptionsBlock,
			ImmutableMap<String, ImmutableSet<String>> stateVarMap, ImmutableSet<String> actionNames,
			ImmutableMap<String, Predicate> aliases) throws Exception {
		ImmutableSet.Builder<Assumption> resultBuilder = ImmutableSet.builder();

		for (PredicateProtos.Predicate predicate : assumptionsBlock.getAssumptionsList()) {
			resultBuilder.add(PredicateParser.parsePredicate(predicate, stateVarMap, actionNames, aliases));
		}

		return resultBuilder.build();
	}

	private static Goals parseGoals(SpecificationProtos.GoalBlock goalBlock,
			ImmutableMap<String, ImmutableSet<String>> stateVarMap, ImmutableSet<String> actionNames,
			ImmutableMap<String, Predicate> aliases) throws Exception {

		return GoalParser.parseGoal(goalBlock.getGoals(), stateVarMap, actionNames, aliases);
	}
}
