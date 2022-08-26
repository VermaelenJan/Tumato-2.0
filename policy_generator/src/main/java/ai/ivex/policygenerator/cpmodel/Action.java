package ai.ivex.policygenerator.cpmodel;

import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

/** @author Hoang Tung Dinh */
final class Action {
  private final Model model;

  private final BoolVar executingVar;
  private final IntVar costVar;
  private final ImmutableMap<String, Constraint> changeableStateVars;
  private final ArrayList<ImmutableMap<String, Constraint>> alternativeChangeableStateVars;

  private Action(Model model, int cost, Object2BooleanMap<String> changeableStateMap, List<Object2BooleanMap<String>> alternativeChangeableStateMap) {
    this.model = model;

    this.executingVar = model.boolVar();

    this.costVar = model.intVar(new int[] {0, cost});
    model.ifThenElse(executingVar, model.arithm(costVar, "=", cost), model.arithm(costVar, "=", 0));

    final ImmutableMap.Builder<String, Constraint> changeableStateVarsBuilder =
        ImmutableMap.builder();

    changeableStateMap.forEach(
        (state, changeable) -> {
          final Constraint changeableConstraint;
          if (changeable) {
            changeableConstraint = model.arithm(executingVar, "=", 1);
          } else {
            changeableConstraint = model.falseConstraint();
          }

          changeableStateVarsBuilder.put(state, changeableConstraint);
        });

    changeableStateVars = changeableStateVarsBuilder.build();
   
    
    alternativeChangeableStateVars = new ArrayList<ImmutableMap<String,Constraint>>(); 
    for (Object2BooleanMap<String> oneAltChangeableStateMap: alternativeChangeableStateMap) {
    	final ImmutableMap.Builder<String, Constraint> oneAltChangeableStateVarsBuilder = ImmutableMap.builder();

    	oneAltChangeableStateMap.forEach(
	        (state, changeable) -> {
	          final Constraint oneAltChangeableConstraint;
	          if (changeable) {
	        	  oneAltChangeableConstraint = model.arithm(executingVar, "=", 1);
	          } else {
	        	  oneAltChangeableConstraint = model.falseConstraint();
	          }

	          oneAltChangeableStateVarsBuilder.put(state, oneAltChangeableConstraint);
	        });

	    ImmutableMap<String, Constraint> oneAltChangeableStateVars = oneAltChangeableStateVarsBuilder.build();
    	
	    alternativeChangeableStateVars.add(oneAltChangeableStateVars);
    }
  }

  static Action create(Model model, int cost, Object2BooleanMap<String> changeableStateMap, List<Object2BooleanMap<String>> alternativeChangeableStateMap) {
    return new Action(model, cost, changeableStateMap, alternativeChangeableStateMap);
  }

  Constraint getExecutingConstraint() {
    return model.arithm(executingVar, "=", 1);
  }

  Constraint getNotExecutingConstraint() {
    return model.arithm(executingVar, "!=", 1);
  }

  IntVar getCostVar() {
    return costVar;
  }

  public BoolVar getExecutingVar() {
    return executingVar;
  }

  public ImmutableMap<String, Constraint> getChangeableStateVars() {
    return changeableStateVars;
  }
  
  public ArrayList<ImmutableMap<String, Constraint>> getAlternativeChangeableStateVars() {
	return alternativeChangeableStateVars;
  }
}
