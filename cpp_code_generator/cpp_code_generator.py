import argparse
import json
import os


class JsonPolicy:
    def __init__(self, policy_file_path):
        with open(policy_file_path) as f:
            policy = json.load(f)
        self.states = policy['States']
        self.actions = policy['Actions']


def to_camel_case(snake_str):
    components = snake_str.split('_')
    # We capitalize the first letter of each component except the first one
    # with the 'title' method and join them together.
    return ''.join(x.title() for x in components)


def write_action_h(action, package_name):
    content = ''

    guard = package_name.upper() + '_ACTIONS_' + action.upper() + '_H_'
    content += '#ifndef ' + guard + '\n'
    content += '#define ' + guard + '\n'

    content += '\n#include "behavior_common/action.h"\n\n'
    content += 'namespace ' + package_name + ' {\n\n'
    content += 'namespace actions {\n\n'

    class_name = to_camel_case(action)

    content += 'class ' + class_name + ' : public behavior_common::Action {\n'
    content += ' public:\n'
    content += '  ' + class_name + '() = default;\n\n'

    content += '  void Start() override;\n'
    content += '  void Stop() override;\n'

    content += '};\n\n'

    content += '}  // namespace actions\n\n'
    content += '}  // namespace ' + package_name + '\n\n'

    content += '#endif  // ' + guard + '\n'

    with open(package_name + '/include/' + package_name + '/actions/' + action.lower() + '.h',
              'w') as file:
        file.write(content)


def write_action_cc(action, package_name):
    content = ''

    content += '#include "' + package_name + '/actions/' + action.lower() + '.h"\n\n'

    content += 'namespace ' + package_name + ' {\n\n'
    content += 'namespace actions {\n\n'

    class_name = to_camel_case(action)
    content += 'void ' + class_name + '::Start() {}\n'
    content += 'void ' + class_name + '::Stop() {}\n\n'

    content += '}  // namespace actions\n\n'
    content += '}  // namespace ' + package_name + '\n'

    with open(package_name + '/src/' + package_name + '/actions/' + action.lower() + '.cc',
              'w') as file:
        file.write(content)


def write_actions(json_policy, package_name):
    for action in json_policy.actions:
        write_action_h(action, package_name)
        write_action_cc(action, package_name)


def write_monitor_h(state, values, package_name):
    content = ''

    guard = package_name.upper() + '_MONITORS_' + state[2:].upper() + '_MONITOR_H_'
    content += '#ifndef ' + guard + '\n'
    content += '#define ' + guard + '\n\n'

    content += '#include "behavior_common/monitor.h"\n'
    content += '#include "behavior_common/monitor_state_manager.h"\n\n'

    content += 'namespace ' + package_name + ' {\n\n'
    content += 'namespace monitors {\n\n'

    class_name = to_camel_case(state[2:]) + 'Monitor'
    content += 'class ' + class_name + ' : public behavior_common::Monitor {\n'
    content += ' public:\n'
    content += '  static const struct {\n'
    content += '    using State = behavior_common::State;\n'
    for val, index in values.items():
        content += '    State ' + val + ' = State::FromIndex(' + str(index) + ');\n'
    content += '  } kState;\n\n'

    content += '  ' + class_name + '() = default;\n\n'

    content += '  behavior_common::State GetCurrentState() const override;\n'
    content += '  void RegisterCallback(\n'
    content += '      std::function<void(behavior_common::State)> callback) override;\n\n'

    content += ' private:\n'
    content += '  behavior_common::MonitorStateManager monitor_state_manager_{' \
               'kState.set_initial_value_here};\n'

    content += '};\n\n'

    content += '}  // namespace monitors\n\n'
    content += '}  // namespace ' + package_name + '\n\n'

    content += '#endif  // ' + guard + '\n'

    with open(package_name + '/include/' + package_name + '/monitors/' + state[
                                                                         2:].lower() + '_monitor.h',
              'w') as file:
        file.write(content)


def write_monitor_cc(state, package_name):
    content = ''

    content += '#include "' + package_name + '/monitors/' + state[2:].lower() + '_monitor.h"\n\n'

    content += 'namespace ' + package_name + ' {\n\n'
    content += 'namespace monitors {\n\n'

    class_name = to_camel_case(state[2:]) + 'Monitor'

    content += 'decltype(' + class_name + '::kState) ' + class_name + '::kState;\n\n'

    content += 'behavior_common::State ' + class_name + '::GetCurrentState() const {\n'
    content += '  return monitor_state_manager_.GetCurrentState();\n'
    content += '}\n\n'

    content += 'void ' + class_name + '::RegisterCallback(\n'
    content += '    std::function<void(behavior_common::State)> callback) {\n'
    content += '  monitor_state_manager_.RegisterCallback(callback);\n'
    content += '}\n\n'

    content += '}  // namespace monitors\n\n'
    content += '}  // namespace ' + package_name + '\n\n'

    with open(package_name + '/src/' + package_name + '/monitors/' + state[
                                                                     2:].lower() + '_monitor.cc',
              'w') as file:
        file.write(content)


def write_monitors(json_policy, package_name):
    for state, values in json_policy.states.items():
        write_monitor_h(state, values, package_name)
        write_monitor_cc(state, package_name)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--package_name')
    parser.add_argument('--policy')

    args = parser.parse_args()
    package_name = args.package_name
    json_policy = JsonPolicy(args.policy)

    os.makedirs(package_name + '/include/' + package_name + '/actions', exist_ok=True)
    os.makedirs(package_name + '/src/' + package_name + '/actions', exist_ok=True)
    os.makedirs(package_name + '/include/' + package_name + '/monitors', exist_ok=True)
    os.makedirs(package_name + '/src/' + package_name + '/monitors', exist_ok=True)

    write_actions(json_policy, package_name)
    write_monitors(json_policy, package_name)


if __name__ == '__main__':
    main()
