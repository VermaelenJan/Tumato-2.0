/*
 * generated by Xtext 2.12.0 (edited on 14/03: added state rules)
 */
package ai.ivex.specdsl.generator

import ai.ivex.specdsl.specLang.Action
import ai.ivex.specdsl.specLang.ActionExecution
import ai.ivex.specdsl.specLang.ActionResource
import ai.ivex.specdsl.specLang.AliasReference
import ai.ivex.specdsl.specLang.AndPredicate
import ai.ivex.specdsl.specLang.Assumption
import ai.ivex.specdsl.specLang.ConditionalGoal
import ai.ivex.specdsl.specLang.Constant
import ai.ivex.specdsl.specLang.EquivalencePredicate
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
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.util.Optional
import org.eclipse.emf.ecore.resource.Resource
import ai.ivex.specdsl.specLang.AlternativeEffect

package class JsonFileGenerator implements ArtifactGenerator {
	
	
	override compile(Resource resource){
		val result = new JsonObject()
		result.addProperty("version", "2.0.0")
        result.add("state_vector", resource.buildStateVector)
        result.add("resources", resource.buildResources)
        result.add("actions", resource.buildActions)
        result.add("reaction_rules", resource.buildReactionRules)
        result.add("state_rules", resource.buildStateRules)
        result.add("assumptions", resource.buildAssumptions)
        result.add("aliases", resource.buildAliases)
        result.add("goals", resource.buildGoals)
        result.add("configuration", resource.buildConfiguration)
        
        val gson = new GsonBuilder().create()
        gson.toJson(result)
	}
	
	
	def buildStateVector(Resource resource){
		val result = new JsonObject()
		for(stateVar : resource.stateVars){
			val values = new JsonArray()
			for(value : stateVar.values){
				values.add(value.name)
			}
			result.add(stateVar.name, values)			
		}
		result
	}
	
	def buildResources(Resource resource){
		val result = new JsonArray()
		for(res : resource.actionResources){
			result.add(res.name)
		}
		result
	}
	
	def buildActions(Resource resource){
		val result = new JsonObject()
		for(action : resource.actions){
			val actionObject = new JsonObject()
			
			if(action.cost !== null){
				actionObject.addProperty("cost", action.cost.value)	
			}
			
			val resources = new JsonArray()
			for(res : action.resources){
				resources.add(res.name)
			}
			actionObject.add("resources", resources)
			
			var JsonArray preconditions = null;
			if(action.preconditions !== null){
				preconditions = new JsonArray()
				for(precond : action.preconditions){
					preconditions.add(precond.predicateToJson)
				}
			}
			actionObject.add("preconditions", preconditions)
			
			
			val nominalEffects = new JsonArray()
			for(effect : action.nominalEffects){
				nominalEffects.add(effect.predicateToJson)
			}
			actionObject.add("nominal effects", nominalEffects)
			
			
			val alternativeEffects = new JsonArray()
			for(altEffect : action.alternativeEffects){
				alternativeEffects.add(altEffect.altEffectToJson)
			}
			actionObject.add("alternative effects", alternativeEffects)
			
			result.add(action.name, actionObject)		
			
		}
		result
	}
	
	def JsonArray altEffectToJson(AlternativeEffect altEffect){
		val effects = new JsonArray()
		for(effect : altEffect.alternativeOutcome){
			effects.add(effect.predicateToJson)
		}
		effects
	}
	
	def buildReactionRules(Resource resource){
		val result = new JsonArray()
		for(rule : resource.reactionRules){
			result.add(rule.rule.predicateToJson)
		}
		result
	}
	
	def buildStateRules(Resource resource){
		val result = new JsonArray()
		for(rule : resource.stateRules){
			result.add(rule.rule.predicateToJson)
		}
		result
	}
	
	def buildAssumptions(Resource resource){
		val result = new JsonArray()
		for(assumption : resource.assumptions){
			result.add(assumption.predicate.predicateToJson)
		}
		result
	}
	
	def buildAliases(Resource resource){
		val result = new JsonArray()
		for(alias : resource.aliases){
			val aliasObject = new JsonObject()
			aliasObject.addProperty("name", alias.name)
			aliasObject.add("value", alias.value.predicateToJson)
			
			result.add(aliasObject)
		}
		result
	}
	
	def buildGoals(Resource resource){
		val goalsList = resource.goals
		if(goalsList.isPresent){
			return goalsList.get().goalListBodyToJson	
		}else{
			return emptyGoalListJson
		}
		
	}
	
	def buildConfiguration(Resource resource){
		val result = new JsonObject()
		result.addProperty("plan_size_max", resource.planLength.max)
		result
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
	
	
	def JsonElement predicateToJson(Predicate predicate){
		switch(predicate){
			ValueAssignment: predicate.constraintValueAssignmentToJson
			ActionExecution: predicate.constraintActionExecutionToJson
			AliasReference: predicate.predicateAliasToJson
			Constant: predicate.constraintConstantToJson
			ImplicationPredicate: predicate.constraintImplicationToJson
			EquivalencePredicate: predicate.constraintEquivalenceToJson
			OrPredicate: predicate.constraintOrToJson
			AndPredicate: predicate.constraintAndToJson
			NotPredicate: predicate.constraintNotToJson
		}
	}
	
	def constraintValueAssignmentToJson(ValueAssignment predicate){
		val result = new JsonObject()
		result.addProperty("operator", "value_comparison")
		result.addProperty("var_name", predicate.valueAssignment.variable.name)
		result.addProperty("value", predicate.valueAssignment.value.name)
		result
	}
	
	def constraintActionExecutionToJson(ActionExecution predicate){
		val result = new JsonObject()
		result.addProperty("operator", "has_action")
		result.addProperty("action_name", predicate.actionExecution.name)
		result
	}	
	
	def predicateAliasToJson(AliasReference predicate){
		val result = new JsonObject()
		result.addProperty("operator", "alias_reference")
		result.addProperty("name", predicate.predicateAlias.name.toString)
		result
	}	
			
	def constraintImplicationToJson(ImplicationPredicate predicate){
		val result = new JsonObject()
		result.addProperty("operator", "implication")
		result.add("left", predicate.left.predicateToJson)
		result.add("right", predicate.right.predicateToJson)
		result
	}
				
	def constraintEquivalenceToJson(EquivalencePredicate predicate){
		val result = new JsonObject()
		result.addProperty("operator", "equivalence")
		result.add("left", predicate.left.predicateToJson)
		result.add("right", predicate.right.predicateToJson)
		result
	}
					
	def constraintOrToJson(OrPredicate predicate){
		val result = new JsonObject()
		result.addProperty("operator", "or")
		result.add("left", predicate.left.predicateToJson)
		result.add("right", predicate.right.predicateToJson)
		result
	}
	
	def constraintAndToJson(AndPredicate predicate){
		val result = new JsonObject()
		result.addProperty("operator", "and")
		result.add("left", predicate.left.predicateToJson)
		result.add("right", predicate.right.predicateToJson)
		result
	}	
	
	def constraintNotToJson(NotPredicate predicate){
		val result = new JsonObject()
		result.addProperty("operator", "not")
		result.add("expression", predicate.expression.predicateToJson)
		result
	}
	
	def constraintConstantToJson(Constant predicate){
		val result = new JsonObject()
		result.addProperty("operator", "constant")
		result.addProperty("value", predicate.value.toString)
		result
	}	
	
	def dispatch JsonElement goalToJson(ConditionalGoal condGoal){
		val result = new JsonObject()
		result.addProperty("goal_type", "conditional_goal")
		result.add("condition", condGoal.condition.predicateToJson)
		result.add("goal", condGoal.goal.goalToJson)
		result
	}
	
	def dispatch JsonElement goalToJson(SimpleGoal goal){
		val result = new JsonObject()
		result.addProperty("goal_type", "simple_goal")
		result.add("body", goal.goalBody.predicateToJson)
		result
	}
	
	def dispatch JsonElement goalToJson(NestedGoalList goal){
		goal.body.goalListBodyToJson
	}
	
	def JsonElement goalListBodyToJson(NestedGoalListBody body){
		val result = new JsonObject()
		result.addProperty("goal_type", "goal_list")
		result.addProperty("list_type", body.execution.toString)
		
		val goals = new JsonArray()
		for(subGoal : body.goals){
			goals.add(subGoal.goalToJson)
		}
		result.add("goals", goals)
		result
	}
	
	def JsonElement emptyGoalListJson(){
		val result = new JsonObject()
		result.addProperty("goal_type", "goal_list")
		result.addProperty("list_type", "constraint")
		result.add("goals", new JsonArray())
		result
	}
	
}

