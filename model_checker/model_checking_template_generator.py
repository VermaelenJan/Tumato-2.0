import json
import os
import subprocess
import re
import argparse


class JsonPolicy:
    def __init__(self, policy_file_path):
        with open(policy_file_path) as f:
            policy = json.load(f)
        self.states = policy['States']
        self.actions = policy['Actions']
        self.mappings = policy['Policy']
        self.name_prefix = os.path.splitext(os.path.basename(policy_file_path))[0]
        self.state_value_index_to_name = dict()
        for state, values in self.states.items():
            self.state_value_index_to_name[state] = dict()
            for value_name, value_index in values.items():
                self.state_value_index_to_name[state][value_index] = value_name


class Term:
    def __init__(self, state_name, state_value):
        self.state_name = state_name
        self.state_value = state_value


class Conjunction:
    def __init__(self):
        self.terms = []

    def add_term(self, term):
        self.terms.append(term)


class Disjunction:
    def __init__(self):
        self.conjunctions = []

    def add_conjunction(self, conjunction):
        self.conjunctions.append(conjunction)


class ReducedPolicy:
    def __init__(self, actions):
        self.actions = actions
        self.policy = dict()

    def add_action_rule(self, action, disjunction):
        self.policy[action] = disjunction


def read_policy(policy_file_path):
    with open(policy_file_path) as f:
        json_policy = json.load(f)

    return json_policy


def write_policy_to_csv(policy):
    csv_content = get_csv_header(policy.states, policy.actions)
    csv_content += '\n'
    for mapping in policy.mappings:
        csv_content += get_mapping_value(policy.actions, mapping, policy.states) + '\n'

    file_name = policy.name_prefix + '.csv'
    with open(file_name, 'w') as csv_file:
        csv_file.write(csv_content)
    return file_name


def get_csv_header(states, actions):
    header = ''

    for state_name, state_value in states.items():
        header += state_name + ','

    for action in actions:
        header += action + ','

    return header[:-1]


def get_mapping_value(actions, mapping, states):
    line = ''
    for state_name, state_values in states.items():
        line += str(state_values[mapping[state_name]]) + ','
    for action in actions:
        if action in mapping['Actions']:
            line += '1,'
        else:
            line += '0,'
    return line[:-1]


def get_reduced_policy(r_output_file, policy):
    with open(r_output_file) as f:
        r_output_content = f.readlines()

    reduced_policy = ReducedPolicy(policy.actions)

    for line in r_output_content:
        if re.match(r'^\s*$', line):
            continue
        cleaned_line = line.replace(' ', '').replace('\n', '')
        rule = cleaned_line.split('=')
        action = rule[0]
        dnf_rule = rule[1]

        disjunction = Disjunction()
        conjunct_text = filter(None, dnf_rule.split('+'))

        for w in conjunct_text:
            conjunction = Conjunction()
            states = w.split('*')
            for s in states:
                values = s.split('{')
                state_name = values[0]
                value_index = values[1][:-1]
                value_name = policy.state_value_index_to_name[state_name][int(value_index)]
                conjunction.add_term(Term(state_name, value_name))
            disjunction.add_conjunction(conjunction)

        reduced_policy.add_action_rule(action, disjunction)

    return reduced_policy


def write_idp_policy_theory(reduced_policy, name_prefix):
    content = 'theory PolicyTheory: V{\n'

    for action in reduced_policy.actions:
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '!t[Time]: ' + action + '(t).\n'
            else:
                content += '{\n'

                for conjunction in dnf.conjunctions:
                    content += '    !t[Time]: ' + action + '(t) <- '
                    for term in conjunction.terms:
                        content += term.state_name + '(t) = ' + term.state_value + '[:Type_' + \
                                   term.state_name + ']' + ' & '
                    content = content[:-3] + '.\n'
                content += '}\n'

        else:
            content += '!t[Time]: ~' + action + '(t).\n'

    content += '}'

    file = open(name_prefix + '-policy-theory.idp', 'w')
    file.write(content)
    file.close()

    with open(name_prefix + '-policy-theory.idp', 'w') as policy_theory_file:
        policy_theory_file.write(content)


def write_idp_model_checking_template(policy):
    content = ''
    content += 'vocabulary V{\n' \
               'type Time isa nat\n' \
               'k: Time\n' \
               '\n' \
               'l_start: Time\n' \
               'Next(Time): Time\n' \
               'Loop(Time)\n\n'

    for state, values in policy.states.items():
        content += 'type Type_' + state + ' constructed from {'
        content += get_list_state_values(values)
        content += '}\n'

    content += '\n'

    for state, values in policy.states.items():
        content += state + '(Time) : Type_' + state + '\n'

    content += '\n'

    for action in policy.actions:
        content += action + '(Time)\n'

    content += '}\n\n'

    content += 'structure S:V{\n' \
               '    Time = {0..9}\n' \
               '}\n' \
               ' \n' \
               '\n' \
               'theory TimeTheory : V{\n' \
               '    k = MAX[:Time].\n' \
               '}\n' \
               '\n' \
               'theory SafetyProperty : V{    \n' \
               '\n' \
               '}\n' \
               '\n' \
               'theory LivenessProperty : V{\n' \
               '// definition of Next(Time) with a loop\n' \
               '{\n' \
               '    !t: Next(t) = t + 1 <- Time(t + 1).\n' \
               '    Next(k) = l_start.\n' \
               '}\n' \
               '{\n' \
               '    !t: Loop(t) <- l_start =< t =< k.\n' \
               '}\n' \
               '\n' \
               '}\n\n'

    content += 'include \"' + policy.name_prefix + '-policy-theory.idp\"\n\n'
    content += 'procedure main(){   \n' \
               '    stdoptions.verbosity.grounding = 1\n' \
               '    stdoptions.verbosity.solving = 1\n' \
               '    stdoptions.verbosity.propagation = 1\n' \
               '    stdoptions.verbosity.symmetrybreaking = 1\n' \
               '    stdoptions.verbosity.approxdef = 1\n' \
               '    stdoptions.verbosity.functiondetection = 1\n' \
               '    stdoptions.verbosity.calculatedefinitions = 1\n' \
               '    \n' \
               '    combined = merge(TimeTheory, PolicyTheory)\n' \
               '    //combined = merge(combined, SafetyProperty)\n' \
               '    //combined = merge(combined, LivenessProperty)\n' \
               '    \n' \
               '    S = calculatedefinitions(combined,S)\n' \
               '    \n' \
               '    printmodels(modelexpand(combined,S))\n' \
               '}'

    with open(policy.name_prefix + '-model-checker.idp', 'w') as template_file:
        template_file.write(content)


def get_list_state_values(values):
    content = ''
    for value_name, value_index in values.items():
        content += value_name + ','
    return content[:-1]


def write_promela_using_definition(reduced_policy, policy):
    content = ''
    for state, values in policy.states.items():
        content += 'mtype:' + state + ' = {'
        for val in values:
            content += val + '_' + state + ', '
        content = content[:-2] + '}\n'

    content += '\n'

    for state, values in policy.states.items():
        content += 'mtype:' + state + ' ' + state + '\n'

    content += '\n'

    for action in policy.actions:
        content += '#define ' + action + ' \\\n'
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '    true\n'
            else:
                content += '    (\\\n'
                for conjunction in dnf.conjunctions:
                    content += '    ('
                    for term in conjunction.terms:
                        content += term.state_name + ' == ' + term.state_value + '_' + \
                                   term.state_name + ' && '
                    content = content[:-4] + ') || \\\n'
                content = content[:-6] + '\\\n'
                content += '    )\n'
        else:
            content += '    false\n'

    content += '\n'

    content += 'bool running = false\n'
    content += 'bool init_state = false\n'

    content += '\n'

    for state, values in policy.states.items():
        content += 'proctype execute_' + state + '() {\n'
        content += '    do\n'

        for val in values:
            content += '    :: ' + state + ' = ' + val + '_' + state + '\n'

        content += '    od\n'
        content += '}\n\n'

    content += 'init {\n'
    content += '    atomic {\n'
    for state, values in policy.states.items():
        content += '        if\n'
        for val in values:
            content += '        :: ' + state + ' = ' + val + '_' + state + '\n'
        content += '        fi\n'

    content += '        running = true\n'
    content += '        init_state = true\n'
    content += '    }\n'
    content += '    init_state = false\n'

    for state, values in policy.states.items():
        content += '    run execute_' + state + '()\n'

    content += '}\n\n'

    with open(policy.name_prefix + '.pml', 'w') as template_file:
        template_file.write(content)


def write_promela(reduced_policy, policy):
    content = ''
    for state, values in policy.states.items():
        content += 'mtype:' + state + ' = {'
        for val in values:
            content += val + '_' + state + ', '
        content = content[:-2] + '}\n'

    content += '\n'

    for action in policy.actions:
        content += 'bool ' + action + '\n'

    content += '\n'

    for state, values in policy.states.items():
        content += 'mtype:' + state + ' ' + state + '\n'

    content += '\n'

    content += 'bool running = false\n'
    content += 'bool init_state = false\n'

    content += '\n'

    for state, values in policy.states.items():
        content += 'inline change_' + state + '() {\n'
        content += '    if\n'

        for val in values:
            content += '    :: ' + state + ' = ' + val + '_' + state + '\n'

        content += '    fi\n'
        content += '}\n\n'

    content += 'init {\n'
    content += '    atomic {\n'
    for state, values in policy.states.items():
        content += '        change_' + state + '()\n'

    for action in reduced_policy.actions:
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '    ' + action + ' = true;\n'
            else:
                content += '    if\n'
                content += '    :: (\n'
                for conjunction in dnf.conjunctions:
                    content += '        ('
                    for term in conjunction.terms:
                        content += term.state_name + ' == ' + term.state_value + '_' + \
                                   term.state_name + ' && '
                    content = content[:-4] + ') ||\n'
                content = content[:-4] + '\n       ) -> ' + action + ' = true;\n'
                content += '    :: else -> ' + action + ' = false;\n'
                content += '    fi\n'
        else:
            content += '    ' + action + ' = false;\n'

    content += '        running = true\n'
    content += '        init_state = true\n'
    content += '    }\n'
    content += '    init_state = false\n'
    content += '    do\n'
    content += '    :: atomic {\n'
    for state, values in policy.states.items():
        content += '        change_' + state + '()\n'
    for action in reduced_policy.actions:
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '        ' + action + ' = true;\n'
            else:
                content += '        if\n'
                content += '        :: (\n'
                for conjunction in dnf.conjunctions:
                    content += '            ('
                    for term in conjunction.terms:
                        content += term.state_name + ' == ' + term.state_value + '_' + \
                                   term.state_name + ' && '
                    content = content[:-4] + ') ||\n'
                content = content[:-4] + '\n           ) -> ' + action + ' = true;\n'
                content += '        :: else -> ' + action + ' = false;\n'
                content += '        fi\n'
        else:
            content += '        ' + action + ' = false;\n'
    content += '    }\n'
    content += '    od\n'
    content += '}\n\n'

    with open(policy.name_prefix + '.pml', 'w') as template_file:
        template_file.write(content)


def write_smv(reduced_policy, policy):
    content = 'MODULE main\n\n'
    content += 'VAR\n'

    for state, values in policy.states.items():
        content += '    ' + state + ': {'
        for val in values:
            content += val + ', '
        content = content[:-2] + '};\n'

    content += '\n'

    content += 'DEFINE\n'
    for action in reduced_policy.actions:
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '    ' + action + ' := TRUE;\n'
            else:
                content += '    ' + action + ' :=\n'
                for conjunction in dnf.conjunctions:
                    content += '        ('
                    for term in conjunction.terms:
                        content += term.state_name + ' = ' + term.state_value + ' & '
                    content = content[:-3] + ') | \n'
                content = content[:-4] + ';\n'
        else:
            content += '    ' + action + ' := FALSE;\n'

    with open(policy.name_prefix + '.smv', 'w') as template_file:
        template_file.write(content)


def write_tlaplus(reduced_policy, policy):
    content = '---- MODULE ' + policy.name_prefix + '_policy ----\n\n'
    content += 'VARIABLE\n'

    all_vars = []

    for state, values in policy.states.items():
        content += '    ' + state + ',\n'
        all_vars.append(state)

    for action in reduced_policy.actions:
        content += '    ' + action + ',\n'
        all_vars.append(action)

    content = content[:-2] + '\n\n'
    content += 'vars == <<'

    for var in all_vars:
        content += var + ', '

    content = content[:-2] + '>>\n\n'

    for state, values in policy.states.items():
        content += 'Dom_' + state + ' == {'
        for val in values:
            content += '"' + val + '", '
        content = content[:-2] + '}\n'

    content += '\n'
    content += 'TypeOK ==\n'

    for state, values in policy.states.items():
        content += '    /\ ' + state + ' \in Dom_' + state + '\n'

    for action in reduced_policy.actions:
        content += '    /\ ' + action + ' \in BOOLEAN\n'

    content += '\n'
    content += 'NextTypeOK ==\n'

    for state, values in policy.states.items():
        content += '    /\ ' + state + '\' \in Dom_' + state + '\n'

    for action in reduced_policy.actions:
        content += '    /\ ' + action + '\' \in BOOLEAN\n'

    content += '\n'
    content += 'Policy ==\n'

    for action in reduced_policy.actions:
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '    /\ ' + action + '\n'
            else:
                content += '    /\ IF\n'
                for conjunction in dnf.conjunctions:
                    content += '        \/\n'
                    for term in conjunction.terms:
                        content += '            /\ ' + term.state_name + ' = "' + \
                                   term.state_value + '"\n'
                content += '       THEN ' + action + ' ELSE ' + '~' + action + '\n'
        else:
            content += '    /\ ~' + action + '\n'

    content += '\n'
    content += 'NextPolicy == \n'

    for action in reduced_policy.actions:
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '    /\ ' + action + '\'\n'
            else:
                content += '    /\ IF\n'
                for conjunction in dnf.conjunctions:
                    content += '        \/\n'
                    for term in conjunction.terms:
                        content += '            /\ ' + term.state_name + '\' = "' + \
                                   term.state_value + '"\n'
                content += '       THEN ' + action + '\' ELSE ' + '~' + action + '\'\n'
        else:
            content += '    /\ ~' + action + '\'\n'

    content += '\n====\n'

    with open(policy.name_prefix + '_policy.tla', 'w') as template_file:
        template_file.write(content)

    content = '---- MODULE ' + policy.name_prefix + ' ----\n\n'
    content += 'EXTENDS ' + policy.name_prefix + '_policy\n'
    content += 'Assumption == \n    TRUE\n\n'
    content += 'Init == TypeOK /\ Policy\n\n'
    content += 'Next == \n    /\ NextTypeOK\n    /\ NextPolicy\n    /\ Assumption\n\n'
    content += 'Spec == Init /\ [][Next]_vars\n\n'
    content += '====\n'

    with open(policy.name_prefix + '.tla', 'w') as template_file:
        template_file.write(content)


def write_tlaplus_using_definition(reduced_policy, policy):
    content = '---- MODULE ' + policy.name_prefix + '_policy ----\n\n'
    content += 'VARIABLE\n'

    for state, values in policy.states.items():
        content += '    ' + state + ',\n'

    content = content[:-2] + '\n\n'
    content += 'vars == <<'

    for state, values in policy.states.items():
        content += state + ', '

    content = content[:-2] + '>>\n\n'

    for state, values in policy.states.items():
        content += 'Dom_' + state + ' == {'
        for val in values:
            content += '"' + val + '", '
        content = content[:-2] + '}\n'

    content += '\n'
    content += 'TypeOK ==\n'

    for state, values in policy.states.items():
        content += '    /\ ' + state + ' \in Dom_' + state + '\n'

    content += '\n'
    content += 'NextTypeOK ==\n'

    for state, values in policy.states.items():
        content += '    /\ ' + state + '\' \in Dom_' + state + '\n'

    content += '\n'

    for action in reduced_policy.actions:
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += action + ' == TRUE\n'
            else:
                content += action + ' ==\n'
                for conjunction in dnf.conjunctions:
                    content += '        \/\n'
                    for term in conjunction.terms:
                        content += '            /\ ' + term.state_name + ' = "' + \
                                   term.state_value + '"\n'
        else:
            content += action + ' == FALSE\n'

    content += '\n====\n'

    with open(policy.name_prefix + '_policy.tla', 'w') as template_file:
        template_file.write(content)

    content = '---- MODULE ' + policy.name_prefix + ' ----\n\n'
    content += 'EXTENDS ' + policy.name_prefix + '_policy\n'
    content += 'Assumption == \n    TRUE\n\n'
    content += 'Init == TypeOK\n\n'
    content += 'Next == \n    /\ NextTypeOK\n    /\ Assumption\n\n'
    content += 'Spec == Init /\ [][Next]_vars\n\n'
    content += '====\n'

    with open(policy.name_prefix + '.tla', 'w') as template_file:
        template_file.write(content)


def write_b_machine(reduced_policy, policy):
    content = 'MACHINE ' + policy.name_prefix + '\n\n'

    content += 'ABSTRACT_VARIABLES\n'

    for state, values in policy.states.items():
        content += '    ' + state + ',\n'

    for action in reduced_policy.actions:
        content += '    ' + action + ',\n'

    content = content[:-2] + '\n\n'

    content += 'DEFINITIONS\n'
    content += '    SET_PREF_MAX_INITIALISATIONS == enter_number_here;\n'
    content += '    SET_PREF_MAX_OPERATIONS == enter_number_here\n\n'

    content += 'SETS\n'
    for state, values in policy.states.items():
        content += '    Dom_' + state + ' = {'
        for val in values:
            content += val + '_' + state + ', '
        content = content[:-2] + '};\n'

    content = content[:-2] + '\n\n'

    content += 'INVARIANT\n'

    for state, values in policy.states.items():
        content += '    ' + state + ' : Dom_' + state + ' &\n'

    for action in reduced_policy.actions:
        content += '    ' + action + ' : ' + 'BOOL &\n'

    content = content[:-2] + '\n\n'

    content += 'INITIALISATION\n'

    for state, values in policy.states.items():
        content += '    ' + state + ' :: Dom_' + state + ';\n'

    for action in reduced_policy.actions:
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '    ' + action + ' := TRUE;\n'
            else:
                content += '    IF\n'
                for conjunction in dnf.conjunctions:
                    content += '        ('
                    for term in conjunction.terms:
                        content += term.state_name + ' = ' + term.state_value + '_' + term.state_name + ' & '
                    content = content[:-3] + ') or\n'
                content = content[:-4] + '\n'
                content += '    THEN ' + action + ' := TRUE ELSE ' + action + ' := FALSE END;\n'
        else:
            content += '    ' + action + ' := FALSE;\n'

    content = content[:-2] + '\n\n'
    content += 'OPERATIONS\n'

    content += '    update_state =\n'
    content += '        BEGIN\n'
    for state, values in policy.states.items():
        content += '            ' + state + ' :: Dom_' + state + ';\n'

    for action in reduced_policy.actions:
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '            ' + action + ' := TRUE;\n'
            else:
                content += '            IF\n'
                for conjunction in dnf.conjunctions:
                    content += '                ('
                    for term in conjunction.terms:
                        content += term.state_name + ' = ' + term.state_value + '_' + term.state_name + ' & '
                    content = content[:-3] + ') or\n'
                content = content[:-4] + '\n'
                content += '            THEN ' + action + ' := TRUE ELSE ' + action + ' := FALSE END;\n'
        else:
            content += '            ' + action + ' := FALSE;\n'

    content = content[:-2] + '\n        END\n'
    content += 'END\n'

    with open(policy.name_prefix + '.mch', 'w') as template_file:
        template_file.write(content)


def write_alloy(reduced_policy, policy):
    content = 'open util/ordering[StateVector]\n\n'

    for state, values in policy.states.items():
        content += 'abstract sig Value_' + state + ' {}\n'
        content += 'one sig '
        for val in values:
            content += val + '_' + state + ', '
        content = content[:-2] + ' extends ' + 'Value_' + state + ' {}\n\n'

    content += 'abstract sig Action {}\n'
    content += 'one sig '
    for action in reduced_policy.actions:
        content += action + ', '

    content = content[:-2] + ' extends Action {}\n\n'

    content += 'sig StateVector {\n'
    for state, values in policy.states.items():
        content += '    ' + state + ': Value_' + state + ',\n'
    content += '    Executing: set Action\n'
    content += '}\n\n'

    for action in reduced_policy.actions:
        content += 'fact {\n'
        content += '    all state: StateVector {\n'
        if action in reduced_policy.policy:
            dnf = reduced_policy.policy[action]
            if not dnf.conjunctions:
                content += '        ' + action + ' in state.Executing\n'
            else:
                content += '        ' + action + ' in state.Executing <=> (\n'
                for conjunction in dnf.conjunctions:
                    content += '            ('
                    for term in conjunction.terms:
                        content += 'state.' + term.state_name + ' = ' + term.state_value + '_' + \
                                   term.state_name + ' && '
                    content = content[:-4] + ') || \n'
                content = content[:-5] + '\n        )\n'
        else:
            content += '        ' + action + ' not in state.Executing\n'
        content += '    }\n'
        content += '}\n\n'

    with open(policy.name_prefix + '.als', 'w') as template_file:
        template_file.write(content)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--type')
    parser.add_argument('--file')

    args = parser.parse_args()

    input_path = args.file
    json_policy = JsonPolicy(input_path)
    csv_file_name = write_policy_to_csv(json_policy)
    r_output_file_name = json_policy.name_prefix + '-r-output.txt'
    num_state_vars = len(json_policy.states)
    num_actions = len(json_policy.actions)
    subprocess.call(
        'Rscript policy_reduction.r ' + csv_file_name + ' ' + r_output_file_name + ' ' + str(
            num_state_vars) + ' ' + str(num_actions), shell=True)
    reduced_policy = get_reduced_policy(r_output_file_name, json_policy)

    if args.type == 'idp':
        write_idp_policy_theory(reduced_policy, json_policy.name_prefix)
        write_idp_model_checking_template(json_policy)

    if args.type == 'promela':
        write_promela(reduced_policy, json_policy)

    if args.type == 'promela_def':
        write_promela_using_definition(reduced_policy, json_policy)

    if args.type == 'smv':
        write_smv(reduced_policy, json_policy)

    if args.type == 'tlaplus':
        write_tlaplus(reduced_policy, json_policy)

    if args.type == 'tlaplus_def':
        write_tlaplus_using_definition(reduced_policy, json_policy)

    if args.type == 'b':
        write_b_machine(reduced_policy, json_policy)

    if args.type == 'alloy':
        write_alloy(reduced_policy, json_policy)


if __name__ == '__main__':
    main()
