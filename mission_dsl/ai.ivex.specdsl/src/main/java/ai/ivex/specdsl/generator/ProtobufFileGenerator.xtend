/*
 * generated by Xtext 2.12.0 (modified 18/03: added state rules)
 */
package ai.ivex.specdsl.generator

import ai.ivex.specdsl.specLang.Action
import ai.ivex.specdsl.specLang.ActionExecution
import ai.ivex.specdsl.specLang.ActionResource
import ai.ivex.specdsl.specLang.AliasReference
import ai.ivex.specdsl.specLang.AndPredicate
import ai.ivex.specdsl.specLang.Assumption
import ai.ivex.specdsl.specLang.BOOLEAN
import ai.ivex.specdsl.specLang.ConditionalGoal
import ai.ivex.specdsl.specLang.Constant
import ai.ivex.specdsl.specLang.EquivalencePredicate
import ai.ivex.specdsl.specLang.Goal
import ai.ivex.specdsl.specLang.GoalExecutionType
import ai.ivex.specdsl.specLang.GoalsBlock
import ai.ivex.specdsl.specLang.ImplicationPredicate
import ai.ivex.specdsl.specLang.NestedGoalList
import ai.ivex.specdsl.specLang.NestedGoalListBody
import ai.ivex.specdsl.specLang.NotPredicate
import ai.ivex.specdsl.specLang.OrPredicate
import ai.ivex.specdsl.specLang.PlanLength
import ai.ivex.specdsl.specLang.Predicate
import ai.ivex.specdsl.specLang.PredicateAlias
import ai.ivex.specdsl.specLang.ReactionRule
import ai.ivex.specdsl.specLang.StateRule
import ai.ivex.specdsl.specLang.SimpleGoal
import ai.ivex.specdsl.specLang.StateVar
import ai.ivex.specdsl.specLang.ValueAssignment
import generated.ai.ivex.specdomain.GoalProtos
import generated.ai.ivex.specdomain.PredicateProtos
import generated.ai.ivex.specdomain.SpecificationProtos
import java.util.Optional
import org.eclipse.emf.ecore.resource.Resource

package class ProtobufFileGenerator {
	
	
	def compile(Resource resource){
		SpecificationProtos.Specification.newBuilder
		.setStateVarBlock(resource.buildStateVector)
		.setResourceBlock(resource.buildResources)
		.setActionsBlock(resource.buildActions)
		.setReactionRulesBlock(resource.buildReactionRules)
		.setStateRulesBlock(resource.buildStateRules)
		.setAssumptionsBlock(resource.buildAssumptions)
		.setAliases(resource.buildAliases)
		.setGoal(resource.buildGoals)
		.setConfig(resource.buildConfiguration)
		.build
	}
	
	
	def buildStateVector(Resource resource){
		val stateVarBlockBuilder = SpecificationProtos.StateVarBlock.newBuilder
		for(stateVar : resource.stateVars){
			val stateVarBuilder = SpecificationProtos.StateVariable.newBuilder
			stateVarBuilder.name = stateVar.name
			for(value : stateVar.values){
				stateVarBuilder.addValues(value.name)
			}
			stateVarBlockBuilder.addStateVariables(stateVarBuilder)
		}
		stateVarBlockBuilder.build
	}
	
	def buildResources(Resource resource){
		val resourceBlockBuilder = SpecificationProtos.ResourceBlock.newBuilder
		for(res : resource.actionResources){
			resourceBlockBuilder.addResources(SpecificationProtos.Resource.newBuilder.setName(res.name))
		}
		resourceBlockBuilder.build
	}
	
	def buildActions(Resource resource){
		val actionBlockBuilder = SpecificationProtos.ActionBlock.newBuilder
		for(action : resource.actions){
			val actionBuilder = SpecificationProtos.Action.newBuilder

			actionBuilder.name = action.name

			if(action.cost !== null){
				actionBuilder.cost = action.cost.value
			}			
			
			
			for(res : action.resources){
				actionBuilder.addResources(res.name)
			}
			
			if(action.preconditions !== null){
				for(precond : action.preconditions){
					actionBuilder.addPreconditions(precond.predicateToProtobuf)
				}
			}
			
			
			if(action.nominalEffects !== null){
				for(effect : action.nominalEffects){
					actionBuilder.addEffects(effect.predicateToProtobuf)
				}
			}
			
			//if(action.alternativeEffects !== null){
			//	for(effectList : action.alternativeEffects){
			//		actionBuilder.addEffects(effectList.predicateToProtobuf)
			//	}
			//} 
			
			actionBlockBuilder.addActions(actionBuilder)	
			
		}
		actionBlockBuilder.build
	}
	
	def buildReactionRules(Resource resource){
		val reactionRulesBlockBuilder = SpecificationProtos.ReactionRulesBlock.newBuilder
		for(rule : resource.reactionRules){
			reactionRulesBlockBuilder.addRules(rule.rule.predicateToProtobuf)
		}	
		reactionRulesBlockBuilder.build
	}
	
	def buildStateRules(Resource resource){
		val stateRulesBlockBuilder = SpecificationProtos.StateRulesBlock.newBuilder
		for(rule : resource.stateRules){
			stateRulesBlockBuilder.addRules(rule.rule.predicateToProtobuf)
		}	
		stateRulesBlockBuilder.build
	}
	
	def buildAssumptions(Resource resource){
		val assumptionsBlockBuilder = SpecificationProtos.AssumptionsBlock.newBuilder
		for(assumption : resource.assumptions){
			assumptionsBlockBuilder.addAssumptions(assumption.predicate.predicateToProtobuf)
		}
		assumptionsBlockBuilder.build
	}
	
	def buildAliases(Resource resource){
		val aliasBlockBuilder = SpecificationProtos.AliasBlock.newBuilder
		for(alias : resource.aliases){
			val aliasBuilder = SpecificationProtos.Alias.newBuilder
			aliasBuilder.name = alias.name
			aliasBuilder.value = alias.value.predicateToProtobuf
			
			aliasBlockBuilder.addAliases(aliasBuilder)
		}
		aliasBlockBuilder.build
	}
	
	def buildGoals(Resource resource){
		val goalBlockBuilder = SpecificationProtos.GoalBlock.newBuilder
		val goals =  resource.goals
		if(goals.isPresent){
			goalBlockBuilder.goals = goals.get.goalListBodyToProtobuf
		}
		goalBlockBuilder.build		
	}
	
	def buildConfiguration(Resource resource){
		val configBuilder = SpecificationProtos.Config.newBuilder
		configBuilder.planSizeMax = resource.planLength.max
		
		configBuilder.build
	}
	
	def stateVars(Resource resource){
		resource.allContents.toIterable.filter(StateVar)
	}
	
	def actions(Resource resource){
		resource.allContents.toIterable.filter(Action)
	}
	
	def actionResources(Resource resource){
		resource.allContents.toIterable.filter(ActionResource)
	}
	
	def reactionRules(Resource resource){
		resource.allContents.toIterable.filter(ReactionRule)
	}
	
	def stateRules(Resource resource){
		resource.allContents.toIterable.filter(StateRule)
	}
	
	def assumptions(Resource resource){
		resource.allContents.toIterable.filter(Assumption)
	}
	
	def goals(Resource resource){
		val goalBlocks = resource.allContents.toIterable.filter(GoalsBlock)
		val goalList = goalBlocks.toList
		if(goalList.empty){
			return Optional.empty()
		}else{
			return Optional.of(goalList.get(0).goalList)
		}
	}
	
	def aliases(Resource resource){
		resource.allContents.toIterable.filter(PredicateAlias)
	}
	
	def planLength(Resource resource){
		resource.allContents.toIterable.filter(PlanLength).get(0)
	}
	
	
	def PredicateProtos.Predicate predicateToProtobuf(Predicate predicate){
		val predicateBuilder = PredicateProtos.Predicate.newBuilder
		
		switch(predicate){
			ValueAssignment: predicateBuilder.valueComparisonPredicate = predicate.constraintValueAssignmentToProtobuf
			ActionExecution: predicateBuilder.actionExecutionPredicate = predicate.constraintActionExecutionToProtobuf
			AliasReference: predicateBuilder.aliasReferencePredicate = predicate.predicateAliasToProtobuf
			Constant: predicateBuilder.constantPredicate = predicate.constraintConstantToProtobuf
			ImplicationPredicate: predicateBuilder.implicationPredicate = predicate.constraintImplicationToProtobuf
			EquivalencePredicate: predicateBuilder.equivalencePredicate = predicate.constraintEquivalenceToProtobuf
			OrPredicate: predicateBuilder.orPredicate = predicate.constraintOrToProtobuf
			AndPredicate: predicateBuilder.andPredicate = predicate.constraintAndToProtobuf
			NotPredicate: predicateBuilder.notPredicate = predicate.constraintNotToProtobuf
		}
		
		predicateBuilder.build
	}
	
	def constraintValueAssignmentToProtobuf(ValueAssignment predicate){
		val valueComparisonPredicateBuilder = PredicateProtos.ValueComparisonPredicate.newBuilder
		valueComparisonPredicateBuilder.leftValue = predicate.valueAssignment.variable.name
		valueComparisonPredicateBuilder.rightValue = predicate.valueAssignment.value.name

		valueComparisonPredicateBuilder.build
	}
	
	def constraintActionExecutionToProtobuf(ActionExecution predicate){
		val actionExecutionPredicateBuilder = PredicateProtos.ActionExecutionPredicate.newBuilder
		actionExecutionPredicateBuilder.value = predicate.actionExecution.name
		
		actionExecutionPredicateBuilder.build
	}	
	
	def predicateAliasToProtobuf(AliasReference predicate){
		val aliasReferencePredicateBuilder = PredicateProtos.AliasReferencePredicate.newBuilder
		aliasReferencePredicateBuilder.value = predicate.predicateAlias.name
		
		aliasReferencePredicateBuilder.build
	}	
			
	def constraintImplicationToProtobuf(ImplicationPredicate predicate){
		val implicationPredicateBuilder = PredicateProtos.ImplicationPredicate.newBuilder
		implicationPredicateBuilder.leftValue = predicate.left.predicateToProtobuf
		implicationPredicateBuilder.rightValue = predicate.right.predicateToProtobuf
		
		implicationPredicateBuilder.build
	}
				
	def constraintEquivalenceToProtobuf(EquivalencePredicate predicate){
		val equivalencePredicateBuilder = PredicateProtos.EquivalencePredicate.newBuilder
		equivalencePredicateBuilder.leftValue = predicate.left.predicateToProtobuf
		equivalencePredicateBuilder.rightValue = predicate.right.predicateToProtobuf
		
		equivalencePredicateBuilder.build
	}
					
	def constraintOrToProtobuf(OrPredicate predicate){
		val orPredicateBuilder = PredicateProtos.OrPredicate.newBuilder
		orPredicateBuilder.leftValue = predicate.left.predicateToProtobuf
		orPredicateBuilder.rightValue = predicate.right.predicateToProtobuf
		
		orPredicateBuilder.build
	}
	
	def constraintAndToProtobuf(AndPredicate predicate){
		val andPredicateBuilder = PredicateProtos.AndPredicate.newBuilder
		andPredicateBuilder.leftValue = predicate.left.predicateToProtobuf
		andPredicateBuilder.rightValue = predicate.right.predicateToProtobuf
		
		andPredicateBuilder.build
	}	
	
	def constraintNotToProtobuf(NotPredicate predicate){
		val notPredicateBuilder = PredicateProtos.NotPredicate.newBuilder
		notPredicateBuilder.value = predicate.expression.predicateToProtobuf
		
		notPredicateBuilder.build
	}
	
	def constraintConstantToProtobuf(Constant predicate){
		val constantPredicateBuilder = PredicateProtos.ConstantPredicate.newBuilder
		constantPredicateBuilder.value = switch(predicate.value){
			case BOOLEAN.TRUE: true
			case BOOLEAN.FALSE: false
		}
		
		constantPredicateBuilder.build
	}	
	
	def GoalProtos.Goal goalToProtobuf(Goal goal){
		val goalBuilder = GoalProtos.Goal.newBuilder
		switch(goal){
			ConditionalGoal: goalBuilder.conditionalGoal = goal.conditionalGoalToProtobuf
			SimpleGoal: goalBuilder.simpleGoal = goal.simpleGoalToProtobuf
			NestedGoalList: goalBuilder.goalList = goal.goalListToProtobuf
		}
		
		goalBuilder.build
	}
	
	def GoalProtos.ConditionalGoal conditionalGoalToProtobuf(ConditionalGoal condGoal){
		val conditionalGoalBuilder = GoalProtos.ConditionalGoal.newBuilder
		conditionalGoalBuilder.condition = condGoal.condition.predicateToProtobuf
		conditionalGoalBuilder.goal = condGoal.goal.goalToProtobuf
		
		conditionalGoalBuilder.build
	}
	
	def GoalProtos.SimpleGoal simpleGoalToProtobuf(SimpleGoal goal){
		val simpleGoalBuilder = GoalProtos.SimpleGoal.newBuilder
		simpleGoalBuilder.body = goal.goalBody.predicateToProtobuf
		
		simpleGoalBuilder.build
	}
	
	def GoalProtos.GoalList goalListToProtobuf(NestedGoalList goal){
		goal.body.goalListBodyToProtobuf
	}
	
	def GoalProtos.GoalList goalListBodyToProtobuf(NestedGoalListBody body){
		val goalListBuilder = GoalProtos.GoalList.newBuilder
		switch(body.execution){
			case GoalExecutionType.PRIORITY: goalListBuilder.executionType = GoalProtos.GoalList.ExecutionType.PRIORITY
			case GoalExecutionType.CONSTRAINT: goalListBuilder.executionType = GoalProtos.GoalList.ExecutionType.CONSTRAINT
		}
		
		for(subGoal : body.goals){
			goalListBuilder.addGoals(subGoal.goalToProtobuf)
		}
		
		goalListBuilder.build
	}
	
}

