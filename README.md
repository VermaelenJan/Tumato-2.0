# web-dsl

## Requirements

* JDK 8
* Python 3.6+ with pip and virtualenv


## Setup

### virtualenv setup

1. Create a Python 3.6+ virtualenv
```virtualenv venv```

	If your default Python interpreter is not a Python 3.5+ interpreter:
	```virtualenv -p [python3 executable] venv```

2. Activate virtualenv
	```. venv/bin/activate``` (Linux)
	```venv\Scripts\activate``` (Windows)

	`deactivate` to deactivate the virtualenv
3. Install Dependencies
```
cd dsl_webui
pip install -r requirements.txt
```


## Configuration

### Application Server
By default, the application (Flask) web server runs on port 5000. This means you should access the web interface at http://localhost:5000 when running locally.
This port can be changed in `dsl_webui/config.py` by changing the `SERVER_PORT` setting.

SSL can be enabled by chaning the `USE_SSL` setting in `dsl_webui/config.py` to `True`, as well as updating the `SSL_CERT` and `SSL_KEY` fields to point towards the certificate and private key file respectively.

### XTEXT Server
The Xtext server runs on port 8080 by default. You do not need to connect to this server directly, the application server will proxy that for you.
This port can be changed in `mission_dsl/ai.ivex.specdsl.web/src/main/java/ai/ivex/specdsl/web/ServerLauncher.xtend` in the first line of the main function.
If you change this port, you'll also need to update the `XTEXT_PORT` setting in `dsl_webuit/config.py` to match.
If the Xtext server is running on a different host, update `XTEXT_HOST` to the address of that host

### Policy Generator Server
The policy generator server runs on port 8085 by default. You do not need to connect to this server directly, it is only used by the application server.
The port can be changed in `policy_generator/src/main/java/ai/ivex/policygenerator/server/Server.java`
If you change this port, you'll also need to update the `POLICY_GENERATOR_PORT` setting in `dsl_webuit/config.py` to match.
If the policy generator server is running on a different host, update `POLICY_GENERATOR_HOST` to the address of that host

## Execution

### Xtext server
In the mission_dsl directory.
```./gradlew jettyRun```

### Application server
In the dsl_webui directory.
1. Activate the virtualenv: ```source venv/bin/activate``` (Linux)
2. Run the server: ```python application.py```
3. After the server exits, deactivate the virtualenv: ```deactivate```

### Policy Generator server
In the policy_generator directory.
```./gradlew startServer```

## License

This project is licensed under the Creative Commons Attribution-NonCommercial 4.0 International (CC BY-NC 4.0). You are free to:

- Share: Copy and redistribute the material in any medium or format.
- Adapt: Remix, transform, and build upon the material.

Under the following terms:

- **Attribution**: You must give appropriate credit, provide a link to the license, and indicate if changes were made.
- **NonCommercial**: You may not use the material for commercial purposes.
