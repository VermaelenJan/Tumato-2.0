SERVER_HOST = 'localhost'
SERVER_PORT = 5000
USE_SSL = False
SSL_CERT = None
SSL_KEY = None
SAVED_SPEC_DIR = 'saved_specs'
SAVED_SPEC_FILE_NAME_LENGTH = 24
STORAGE_DIR = "generated_policies"
RESOURCES_DIR = "resources"

XTEXT_HOST = '127.0.0.1'
XTEXT_PORT = 8080
XTEXT_USE_SSL = False
XTEXT_PROXY_PREFIX = 'proxy'
XTEXT_SERVICE_PREFIX = 'xtext-service'
XTEXT_ARTIFACT = 'spec.buf'

POLICY_GENERATOR_HOST = '127.0.0.1'
POLICY_GENERATOR_PORT = 8085

# Auto generated globals
if SERVER_PORT != 80:
    SERVER_HOSTNAME = SERVER_HOST + ":" + str(SERVER_PORT)
else:
    SERVER_HOSTNAME = SERVER_HOST

if USE_SSL:
    SERVER_URL_BASE = "https://" + SERVER_HOSTNAME
else:
    SERVER_URL_BASE = "http://" + SERVER_HOSTNAME

if XTEXT_USE_SSL:
    XTEXT_URL_BASE = "https://" + XTEXT_HOST + ":" + str(XTEXT_PORT)
else:
    XTEXT_URL_BASE = "http://" + XTEXT_HOST + ":" + str(XTEXT_PORT)

POLICY_GENERATOR_URL_BASE = "http://" + POLICY_GENERATOR_HOST + ":" + str(POLICY_GENERATOR_PORT)