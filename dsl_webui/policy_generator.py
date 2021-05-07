import json
import os
from threading import Lock
import requests

from config import *


lock = Lock()


def get_policy_if_exists(policy_name):
    storage_name = policy_name
    if os.path.isfile(os.path.join(STORAGE_DIR, storage_name)):
        return progress_message("SUCCESS", "The policy was already processed", storage_name)
    else:
        return None


def generate(code, policy_name, force_fresh=False):
    yield progress_message('QUEUED', "The policy has been queued for processing")
    lock.acquire()
    try:
        existing_policy_message = get_policy_if_exists(policy_name)
        if not force_fresh and existing_policy_message:
            yield existing_policy_message
            return

        yield progress_message('STARTED', "The policy has started processing")

        resp = requests.request(
            method='POST',
            url=POLICY_GENERATOR_URL_BASE + "/generate",
            data=code)

        result = json.loads(resp.content)
        if resp.status_code == 200:
            if result['status'] == "succeeded":
                policy = result['generated_policy']
                storage_name = policy_name
                target_file = os.path.join(os.curdir, STORAGE_DIR, storage_name)
                delete_file_if_exists(target_file)
                create_file_with_data(target_file, json.dumps(policy, indent=2))
                yield progress_message('SUCCESS', "The policy has completed processing", storage_name)
                return
            elif result['status'] == "failed":
                failed_state = result['failed_state']
                yield progress_message('FAILURE', "The policy was infeasible",
                                       {'infeasible_model_failure': True,
                                        'model': failed_state}
                                       )
                return
            else:
                yield progress_message('FAILURE', "The solver returned an unknown status value: " + result['status'],
                                       {'infeasible_model_failure': False})
                return
        elif resp.status_code == 503:
            yield progress_message('FAILURE', "The solver is currently busy. Please try again later",
                                   {'infeasible_model_failure': False})
            return
        elif resp.status_code == 500:
            yield progress_message('FAILURE', "The solver encountered an error: " + result['error_message'],
                                   {'infeasible_model_failure': False})
            return
        else:
            yield progress_message('FAILURE', "The solver encountered an unknown error",
                                   {'infeasible_model_failure': False})
            return
    except Exception:
        yield progress_message('FAILURE', "An unknown error occured while trying to generate the policy",
                               {'infeasible_model_failure': False})
    finally:
        lock.release()


def create_dir_if_not_exists(path):
    if not os.path.exists(path):
        os.makedirs(path)


def delete_file_if_exists(name):
    if os.path.exists(name) and os.path.isfile(name):
        os.remove(name)


def create_file_with_data(name, data):
    f = open(name, "w+")
    try:
        f.write(data)
    finally:
        f.close()


def progress_message(tag, message, data=None):
    result = {
        'tag': tag,
        'message': message,
    }
    if data is not None:
        result['data'] = data
    return result
