import hashlib
import os
import random
import re
import string
import shutil

import requests
from flask import Flask, Response, send_from_directory, render_template, request, abort, jsonify

import policy_generator
from ServerSentEvent import ServerSentEvent
from config import *

app = Flask(__name__, template_folder='static')


@app.route("/policy/compile")
def compile_policy():
    def gen(args, cookies):
        spec_id = args['specId']
        force_fresh = args.get('force', 'false') == "true"
        policy_name = hash_spec(spec_id)
        existing_policy_message = policy_generator.get_policy_if_exists(policy_name)
        if not force_fresh and existing_policy_message:
            yield ServerSentEvent(existing_policy_message, "policy-update").encode()
            return
        args_copy = args.copy()
        args_copy['artifact'] = XTEXT_ARTIFACT
        url = XTEXT_URL_BASE + "/" + XTEXT_SERVICE_PREFIX + "/generate"
        resp = requests.request(
            method='GET',
            url=url,
            params=args_copy,
            cookies=cookies,
        )
        if resp.status_code != 200:
            return Response(resp.content, resp.status_code, get_headers(resp))

        # print(getjson(args, cookies))
        code = resp.content.decode(resp.encoding)
        generator = policy_generator.generate(code, policy_name, force_fresh)
        for message in generator:
            yield ServerSentEvent(message, "policy-update").encode()

    return Response(gen(request.args, request.cookies), mimetype="text/event-stream")


@app.route('/policy/<string:filename>/<string:downloadname>/download', methods=['GET'])
def download(filename, downloadname):
    return send_from_directory(directory=STORAGE_DIR,
                               filename=filename,
                               as_attachment=True,
                               attachment_filename=downloadname)


@app.route('/policy/<string:filename>/part/<int:start_line>/<int:amount>', methods=['GET'])
def policy_part(filename, start_line, amount):
    with open(os.path.join(STORAGE_DIR, filename), 'r') as policy_file:
        current_line = 0
        result_text = ""
        has_more = False
        for line in policy_file.readlines():
            if current_line >= start_line + amount:
                current_line += 1
                has_more = True
                break
            if current_line >= start_line:
                result_text += line
            current_line += 1
        result = {'text': result_text, 'next_line': current_line, 'has_more': has_more}
        return jsonify(result)


@app.route("/")
def list():
    return render_template("list.html")


@app.route("/editor")
def index():
    return render_template("editor.html", xtext_url=SERVER_URL_BASE + "/" + XTEXT_PROXY_PREFIX,
                           xtext_prefix=XTEXT_SERVICE_PREFIX)


@app.route("/" + XTEXT_PROXY_PREFIX + "/<path:target>", methods=['GET', 'PUT', 'POST', 'OPTIONS', 'HEAD', 'TRACE'])
def proxy(target):
    resp = requests.request(
        method=request.method,
        url=request.url.replace(SERVER_URL_BASE + "/" + XTEXT_PROXY_PREFIX, XTEXT_URL_BASE),
        headers={key: value for (key, value) in request.headers if key != 'Host'},
        data=request.get_data(),
        cookies=request.cookies,
        allow_redirects=False)

    response = Response(resp.content, resp.status_code, get_headers(resp))
    return response


def get_headers(resp):
    excluded_headers = ['content-encoding', 'content-length', 'transfer-encoding', 'connection']
    headers = [(name, value) for (name, value) in resp.raw.headers.items()
               if name.lower() not in excluded_headers]
    return headers


@app.route('/spec/<path:filename>', methods=['GET'])
def load_spec(filename):
    return send_from_directory(directory=SAVED_SPEC_DIR, path=filename)


@app.route('/spec/<path:filename>', methods=['POST'])
def save_spec(filename):
    path = os.path.join(os.getcwd(), SAVED_SPEC_DIR, filename)
    if os.path.exists(path):
        save_file(path, re.sub(r"\r\n", r"\n", request.form['text']))
        return '', 200
    else:
        abort(404)


@app.route('/create/spec', methods=['POST'])
def create_spec():
    random_name = ''.join(random.choices(string.ascii_uppercase + string.digits, k=SAVED_SPEC_FILE_NAME_LENGTH))
    path = os.path.join(os.getcwd(), SAVED_SPEC_DIR, random_name)
    use_tutorial = request.form['tutorial'] == 'true'
    if use_tutorial:
        shutil.copyfile(os.path.join(os.getcwd(), RESOURCES_DIR, 'tutorial_spec.tmt'), path)
    else:
        open(path, 'a').close()
    return random_name, 200


def save_file(name, code):
    f = open(name, "w+")
    try:
        f.write(code)
    finally:
        f.close()


def hash_spec(spec_id):
    with open(os.path.join(SAVED_SPEC_DIR, spec_id), 'r') as spec_file:
        code = spec_file.read()
        code = re.sub(r"/\*.*?\*/", "", code, flags=re.DOTALL)  # Removes /* block */ comments
        code = re.sub(r"^//.*$", "", code, flags=re.MULTILINE)  # Removes // single line comments
        code = re.sub(r"\s+", " ", code)
        code = code.strip()
        return hashlib.sha3_256(bytes(code, 'utf-8')).hexdigest()


if __name__ == "__main__":
    app.debug = True
    if USE_SSL:
        app.run(host=SERVER_HOST, port=SERVER_PORT, threaded=True, ssl_context=(SSL_CERT, SSL_KEY))
    else:
        app.run(host=SERVER_HOST, port=SERVER_PORT, threaded=True)
